# Brazen Therapy — Website & Therapist Matching Platform

Modern website + therapist discovery/matching platform replacing Brazen Therapy's Wix site.
Built on the `site-cms` foundation (Spring Boot + Next.js, multi-tenant; Brazen is tenant #1).

> **No PHI, ever.** No EHR/EMR, scheduling, billing, or session notes. Scheduling stays third-party
> via external links. See [`docs/ENGINEERING_PRD.md`](docs/ENGINEERING_PRD.md) §9.

## Repository layout

```
brazenCMS/
├── backend/      Spring Boot 3.3.5 / Java 21 / Gradle — REST API, auth, matching engine
├── frontend/     Next.js 14 / React 18 / TypeScript / Tailwind — public site + admin
├── docker-compose.prod.yml   production compose (connects to Traefik on rue)
├── docker-compose.yml
└── docs/
    ├── ENGINEERING_PRD.md   ← build plan, conflict rulings, 1-week demo plan  (READ FIRST)
    ├── coding_guidelines.md ← governing Spring Boot Java standard
    ├── project.md           ← product/business PRD
    └── content/             ← Brazen source content for migration
```

## Start here (devs)

**Read [`docs/DEV_DIRECTIONS.md`](docs/DEV_DIRECTIONS.md) first — it's the marching orders** and links
everything below in order:

1. **Stand up the dev server:** [`docs/DEV_SERVER_SETUP.md`](docs/DEV_SERVER_SETUP.md).
2. Read [`docs/ENGINEERING_PRD.md`](docs/ENGINEERING_PRD.md) — especially **§3.1 Conflict Rulings (ALIGN-1…5)**.
3. Read [`docs/coding_guidelines.md`](docs/coding_guidelines.md).
4. Use the **Taxonomy domain** as the reference pattern for all new backend work (see below).

## Reference pattern — the Taxonomy slice

`Taxonomy` is the **copy-me template** for new backend domains. It demonstrates the full target
pattern, end to end, per the coding guidelines and the ALIGN rulings:

| Layer | File |
| --- | --- |
| Enum (`@Enumerated(EnumType.STRING)`, Javadoc, unit test) | `backend/.../enums/TaxonomyType.java` |
| Entity (Lombok, extends `BaseEntity`, tenant-scoped, UUID PK) | `backend/.../models/TaxonomyTerm.java` |
| Repository (Spring Data, tenant-scoped queries) | `backend/.../repositories/TaxonomyTermRepository.java` |
| DTOs (`…DTO` suffix, Lombok, Bean Validation) | `backend/.../dto/TaxonomyTerm{Request,Response}DTO.java` |
| Service **interface + impl** | `backend/.../services/TaxonomyService.java` + `services/impl/TaxonomyServiceImpl.java` |
| Controllers (public + admin, RBAC via `@PreAuthorize`, Swagger `@Operation`) | `backend/.../controllers/{TaxonomyController,AdminTaxonomyController}.java` |
| Migration | `backend/.../db/changelog/changes/006-taxonomy.sql` |
| Tests | `enums/TaxonomyTypeTest`, `services/impl/TaxonomyServiceImplTest`, plus repo/integration tests |

New Brazen code uses **layer-first packages** (`controllers/`, `services/`, `models/`, …) under
`com.brochure.cms`, per ALIGN-3. Existing `domain.*` packages are left as-is until the post-demo
restructure (ALIGN-3b).

## Local development

```bash
docker compose up --build      # db (Postgres 5433), mailpit, backend (8080), frontend (3000)
```

Backend only:
```bash
cd backend && ./gradlew bootRun
cd backend && ./gradlew test     # unit + integration (integration needs Docker for Testcontainers)
```

## Deploying the public test instance

See [`docs/DEV_SERVER_SETUP.md`](docs/DEV_SERVER_SETUP.md) §7 for deploying to `https://brazen.1lpro.com`
via the existing Traefik reverse proxy (Let's Encrypt TLS handled automatically).

## ✅ ALIGN-1 (Flyway → Liquibase) — completed

Migrations now live in `backend/src/main/resources/db/changelog/` as Liquibase changesets
`001`–`006`. Flyway has been removed. Schema parity was verified against a live Postgres and the
app boots with `spring.jpa.hibernate.ddl-auto=validate`. New Brazen tables are added as the next
changesets (`007-…`). See `docs/ENGINEERING_PRD.md` §3.1 / §10 for the original ruling.
