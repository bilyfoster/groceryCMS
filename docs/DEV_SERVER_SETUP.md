# Dev Server Setup & Runbook

How to stand up Brazen CMS on the **dev server** (not local machines) and start building.
Read [`ENGINEERING_PRD.md`](ENGINEERING_PRD.md) first for scope and the ALIGN rulings.

---

## 1. Prerequisites (on the dev server)

- **Docker Engine + Docker Compose v2** (`docker compose version`)
- **git**
- **Repo access**: each dev's GitHub account added as a collaborator on
  `bilyfoster/brazenCMS`, **or** a read-only **deploy key** on the server.
  The server authenticates to GitHub over **SSH** (HTTPS has no stored credentials).
- **Open inbound ports** (see §6): at minimum **3000** (frontend). Optionally **8080**
  (API + Swagger) for backend testing, **8025** (Mailpit) for magic-link emails.

Everything else (JDK 21, Node 20, Postgres) runs **inside containers** — no host installs needed.

---

## 2. Clone

```bash
# one-time: ensure the server's SSH key is registered with GitHub
ssh -T git@github.com          # expect: "Hi <user>! You've successfully authenticated"

git clone git@github.com:bilyfoster/brazenCMS.git
cd brazenCMS
```

---

## 3. Configure environment

The bundled `docker-compose.yml` is the **dev stack** (Postgres + Mailpit + backend + frontend)
and already supplies dev values inline. For a shared dev server, **override the two secrets** that
matter and confirm the frontend wiring.

Create a `.env` in the repo root (git-ignored):

```bash
cp .env.example .env
```

Then set, at minimum:

| Var | Value on dev server | Why |
| --- | --- | --- |
| `JWT_SECRET` | a fresh 64+ char hex string (`openssl rand -hex 48`) | dev default is public in the repo |
| `POSTGRES_PASSWORD` | a non-default password | dev default is public |
| `NEXT_PUBLIC_TENANT_SLUG` | `demo` | frontend sends this as the `X-Tenant-Slug` header |
| `BACKEND_URL` | `http://backend:8080` | **server-side** proxy target (container DNS name — leave as-is) |

> **You do NOT need to set a public API URL.** In the browser the frontend calls its own
> same-origin `/api` path, which Next.js proxies to `BACKEND_URL` internally. The browser only
> ever needs to reach the **frontend** (port 3000 / nginx), never the backend directly.

The provided `docker-compose.yml` reads these via its `environment:` block; if you change secrets,
update them there or switch to an `env_file: .env` reference.

---

## 4. Start the stack

```bash
docker compose up --build -d        # build images + start in background
docker compose logs -f backend      # watch boot; Liquibase runs changesets 001..006 automatically
```

Services and ports:

| Service | Container | Host port | Purpose |
| --- | --- | --- | --- |
| frontend | Next.js | **3000** | public site + `/admin` |
| backend | Spring Boot | **8080** | REST API (`/api`) + Swagger |
| db | Postgres 16 | 5433 | database (host-mapped for inspection) |
| mailpit | Mailpit | **8025** (UI), 1025 (SMTP) | catches magic-link emails in dev |

Migrations run on backend boot. Because `spring.jpa.hibernate.ddl-auto=validate`, the app will
**refuse to boot if an entity and its migration disagree** — that's intended; fix the mismatch.

---

## 5. Verify it's up

Replace `SERVER` with the dev server's host/IP.

```bash
# API is alive + tenant resolves (note the required tenant header)
curl -s http://SERVER:8080/api/taxonomies?type=FOCUS_AREA \
  -H 'X-Tenant-Slug: demo'
# -> {"success":true,"data":[], ...}   (empty list until terms are created)

# Swagger UI (springdoc)
open http://SERVER:8080/swagger-ui.html      # or /v3/api-docs for the raw spec
```

Frontend + admin login:

1. Browse `http://SERVER:3000`
2. Go to `http://SERVER:3000/auth/login`
3. **Seeded admin:** `admin@demo.local` / `password` (tenant `demo`)
4. Magic-link logins: trigger from the UI, then read the email in Mailpit at `http://SERVER:8025`.

> **`Tenant could not be resolved` on direct API calls?** You forgot the `X-Tenant-Slug: demo`
> header. The browser app sends it automatically; `curl`/Swagger/Postman must send it themselves.
> (Alternatively, point the `demo` tenant's `domain` at the server hostname so host-based
> resolution works — see `TenantFilter`.)

---

## 6. Exposing the server

- **Simplest (internal dev):** use `docker-compose.yml`. It exposes port **3000** for the frontend,
  optionally **8080** and **8025** for API/Swagger/Mailpit.
- **With a domain / TLS:** use `docker-compose.prod.yml`. On the shared `rue` server it connects to
  the existing **Traefik** reverse proxy (ports 80/443) which terminates TLS with Let's Encrypt and
  routes by `Host`. The demo tenant's domain is updated via Liquibase changeset `007-brazen-domain`
  so host-based tenant resolution works for `brazen.1lpro.com`.

---

## 7. Deploying to `brazen.1lpro.com`

The public test instance uses `docker-compose.prod.yml` and the existing **Traefik** reverse proxy
on `rue`. Traefik handles TLS with Let's Encrypt automatically.

### Prerequisites

1. DNS for `brazen.1lpro.com` routes to the server (via Cloudflare or directly to `rue`).
2. The `traefik` network already exists on the server:
   ```bash
   docker network ls | grep traefik
   # if missing: docker network create traefik
   ```
3. Inbound ports **80** and **443** are open.

### One-time setup

```bash
cd /home/jenkins/docker/brazen-cms

# 1. Create the production env file
cp .env.example .env
# Edit .env and set strong secrets plus the production values:
#   SPRING_PROFILES_ACTIVE=prod
#   JWT_SECRET=<64-char-hex-secret>
#   APP_BASE_URL=https://brazen.1lpro.com
#   CORS_ALLOWED_ORIGINS=https://brazen.1lpro.com
#   MAIL_FROM=noreply@brazen.1lpro.com
#   NEXT_PUBLIC_API_URL=https://brazen.1lpro.com/api
#   BACKEND_URL=http://backend:8080

# 2. Start the stack
docker compose -f docker-compose.prod.yml up --build -d
```

### Normal operation / updates

```bash
cd /home/jenkins/docker/brazen-cms
docker compose -f docker-compose.prod.yml up --build -d
```

Watch Traefik request the certificate:

```bash
docker logs -f traefik
```

### Verify

```bash
# HTTPS is up and tenant resolves by domain
curl -s https://brazen.1lpro.com/api/taxonomies?type=FOCUS_AREA
# -> {"success":true,"data":[],...}
```

Admin login: `https://brazen.1lpro.com/auth/login` with `admin@demo.local` / `password`.

---

## 8. Running tests on the server

```bash
cd backend
./gradlew test            # unit + integration; integration uses Testcontainers (needs Docker — present)
```

- Unit tests (enums, services) run without a database.
- Integration tests (`*IT`, `@DataJpaTest`) spin up Postgres via **Testcontainers** — the Docker
  socket must be reachable from the build (it is, on the dev server).
- Frontend e2e (Playwright) lives in `frontend/e2e`: `cd frontend && npx playwright test`.

---

## 9. Day-to-day workflow

1. Branch off `main` (`feature/<domain>`).
2. Build new backend domains by **copying the Taxonomy pattern** (see root `README.md`):
   layer-first packages, Lombok, enum + `@Enumerated(EnumType.STRING)`, service interface + impl,
   `…DTO` DTOs, RBAC via `@PreAuthorize`, Swagger annotations, tenant-scoped repos, tests.
3. Add DB changes as a **new Liquibase changeset** (`007-…`, `008-…`) — never edit an applied one.
4. `./gradlew test` green, no warnings, before opening a PR.
5. PR → **senior dev review** against the §11 Definition of Done in the PRD.

### ✅ ALIGN-1 (Flyway → Liquibase) — completed

Ported `V1`–`V6` to Liquibase changesets `001`–`006` under `backend/src/main/resources/db/changelog/`,
verified schema parity against a live Postgres, and removed Flyway. See PRD §3.1 / §10.

---

## 10. Common pitfalls

| Symptom | Cause / Fix |
| --- | --- |
| `Tenant could not be resolved` | Missing `X-Tenant-Slug: demo` on a direct (non-browser) API call |
| Browser can't load data | Reach the **frontend** (3000), not the backend; don't point the browser at `:8080` |
| Backend won't boot, "Schema validation" error | Entity/migration mismatch (`ddl-auto: validate`) — align columns |
| Magic-link login "works" but no email | Check **Mailpit** (`:8025`); dev SMTP goes there, not a real inbox |
| Port 5432 conflict | Dev db is host-mapped to **5433** on purpose; connect there for inspection |
| `git clone` asks for a password | Server SSH key not registered with GitHub (see §1/§2) |
