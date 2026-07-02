# Developer Directions — Brazen CMS

Read this top to bottom before writing code. It's your marching orders for the demo build.
The senior dev reviews every PR against the rules and Definition of Done below.

> **Hard rule, never violate:** this platform stores **no PHI** — no clinical records, session
> notes, scheduling, or billing. Scheduling is third-party via external links only. See
> [`ENGINEERING_PRD.md`](ENGINEERING_PRD.md) §9.

---

## Step 1 — Get oriented (in this order)

1. [`DEV_SERVER_SETUP.md`](DEV_SERVER_SETUP.md) — stand up the stack on the dev server.
2. [`ENGINEERING_PRD.md`](ENGINEERING_PRD.md) — the build plan. Pay attention to **§3.1 (ALIGN rulings)**,
   **§4 (data model)**, **§5 (roles)**, **§6 (matching)**, **§11 (Definition of Done)**.
3. [`coding_guidelines.md`](coding_guidelines.md) — the coding standard.
4. [`project.md`](project.md) — product context (what the client asked for).

You do **not** edit these docs. If something is wrong or ambiguous, raise it to the senior dev to
amend — don't reinterpret the spec yourself (see Step 6).

## Step 2 — Get the server running

Follow [`DEV_SERVER_SETUP.md`](DEV_SERVER_SETUP.md). You're ready when:
- `docker compose up --build -d` boots cleanly and Liquibase applies changesets `001`–`006`,
- you can log into `/admin` as `admin@demo.local` / `password`,
- `curl http://SERVER:8080/api/taxonomies?type=FOCUS_AREA -H 'X-Tenant-Slug: demo'` returns `success:true`.

## Step 3 — Learn the one pattern you'll copy

The **Taxonomy** domain is the reference implementation. Every new backend domain copies its shape:

```
enums/            TaxonomyType.java          enum + @Enumerated(EnumType.STRING) + unit test
models/           TaxonomyTerm.java          Lombok entity, extends BaseEntity, tenant_id, UUID PK
repositories/     TaxonomyTermRepository     tenant-scoped queries, soft-delete aware
dto/              TaxonomyTerm{Req,Resp}DTO   "DTO" suffix, Bean Validation, Lombok
services/         TaxonomyService            interface (the contract)
services/impl/    TaxonomyServiceImpl        impl, constructor injection, @Transactional
controllers/      Taxonomy / AdminTaxonomy   public + admin, @PreAuthorize RBAC, Swagger @Operation
db/changelog/     006-taxonomy.sql           new Liquibase changeset per feature (never edit applied ones)
test/...          enum + service tests       unit (Mockito) + repo (@DataJpaTest) + integration (IT)
```

New Brazen code is **layer-first** under `com.brochure.cms`. Existing `domain.*` packages stay as-is
until the post-demo restructure (PRD ALIGN-3). When in doubt, open the Taxonomy files and mirror them.

## Step 4 — The non-negotiables (PR-blocking)

From the coding guidelines + ALIGN rulings — the reviewer checks these every time:

- **UUID** primary keys; never expose sequential IDs.
- **DTOs at the boundary**, names end in `DTO`; never return entities from controllers.
- **Constructor injection** only; no field `@Autowired`. No Lombok on services/controllers.
- **Lombok** on entities (`@Getter/@Setter/@Builder`, **not** `@Data` on BaseEntity subclasses) and DTOs.
- **Enums** in `enums/`, each with a unit test, persisted with `@Enumerated(EnumType.STRING)`.
- **Tenant-scoped** every query (`TenantIds.current()`); a repo method must never leak across tenants.
- **Authorization** enforced server-side; therapist self-service is **ownership-scoped** (own record only).
- **New Liquibase changeset per change** (`007-…`, `008-…`); never edit an applied changeset.
- No `System.out`/`printStackTrace`, no swallowed exceptions, no magic strings/numbers, no raw SQL.
- `./gradlew test` green, **zero warnings**, no `TODO`/temp mocks committed.

## Step 5 — The work queue (build in this order)

Branch per task (`feature/<name>`), PR to `main`, senior-dev review. Sequence matters — later tasks
depend on earlier ones. Detailed specs are in the PRD sections noted.

| # | Task | Spec | Notes |
| --- | --- | --- | --- |
| **0a** | **ALIGN-1: Flyway → Liquibase** | PRD §3.1, §10 | **Done.** Ported `V1`–`V6` to changesets `001`–`006`, proved schema parity, removed Flyway. |
| **0b** | **ALIGN-2: Lombok retrofit** of existing entities/DTOs | PRD §3.1 | Mechanical; keep tests green. `@Getter/@Setter` on entities, not `@Data`. |
| **1** | **Taxonomy admin UI + filter data** | PRD §4.2, §8 | Backend is done (reference slice). Build `/admin/taxonomies` screen + wire public terms into filters/intake. |
| **2** | **Therapist domain** | PRD §4.1, §5, §7, §8 | Entity + M2M to taxonomy terms; admin CRUD + publish; **self-service `THERAPIST`** (own record); profile page + `/admin/therapists`. |
| **3** | **Directory** | PRD §7, §8 | `GET /api/therapists` filter/sort/search; `/therapists` list + `/therapists/[slug]` profile. |
| **4** | **Matching engine** | PRD §4.4, §6 | `MatchingService` interface + `WeightedScoreMatchingServiceImpl`; `POST /api/match`; intake form + results page. **Heaviest test coverage here.** |
| **5** | **Services + marketing pages** | PRD §4.3, §8 | `PageType.SERVICE` + related-therapists via focus area; seed home/about/services from `content/`. |
| **6** | **Analytics events** | PRD §8 | `POST /api/events` + capture profile views, searches, match start/complete, scheduling clicks. No clinical data. |
| **7** | **Seed real content + audit logging + polish** | PRD §9, §10 | Seed therapists from `content/`; audit admin mutations; responsive + baseline a11y pass. |

If days slip, the cut order is: **6 → 4 polish → 5 polish**. Tasks 1–3 + marketing pages are the
protected demo floor (PRD §10).

## Step 6 — Definition of Done & when to escalate

**Before you open a PR**, self-check against PRD §11 (the demo-gate checklist): migration added,
entity/DTO/enum conventions, RBAC + tenant scoping, unit + repo + integration tests, `./gradlew test`
green, Javadoc on public APIs, frontend responsive + keyboard-navigable.

**Escalate to the senior dev (don't guess) when:**
- the spec is ambiguous or looks wrong → ask for a PRD amendment;
- you need a breaking change (schema you can't add-only, an API contract change);
- a change would cross tenants or touch auth/security boundaries;
- you're tempted to deviate from a non-negotiable in Step 4.

The deferred quality bars — JaCoCo 80% enforcement, full WCAG 2.1 AA audit, Swagger on every
endpoint — are **pre-launch**, not demo gates (PRD ALIGN-5). Add Swagger annotations and aim for
coverage as you go, but they don't block the demo.
