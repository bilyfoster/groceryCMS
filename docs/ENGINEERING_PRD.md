# Brazen Therapy — Engineering PRD & Build Plan

**Audience:** Development team
**Owner / Reviewer:** Senior Developer (reviews all PRs against this doc + coding guidelines)
**Status:** Ready for build kickoff
**Date:** 2026-06-15
**Demo target:** Client demo within 1 week of kickoff

> This is the **engineering** PRD. The product/business PRD lives in [`project.md`](project.md) and is the source of truth for *what* we are building and *why*. This document defines *how* we build it on top of the existing `site-cms` platform, what we reuse, what we build new, the coding-standard rulings that resolve conflicts, and what must be working for the client demo.

---

## 1. Source-of-truth documents

| Document | Role | Notes |
| --- | --- | --- |
| [`project.md`](project.md) | Product/business PRD | Scope, user types, feature list, non-goals |
| [`coding_guidelines.md`](coding_guidelines.md) | **Coding standard — Spring Boot Java** | Latest revision. Governing standard. See §3 for conflict rulings. |
| `site-cms/coding_guidelines.md` | **STALE — delete day 1** | Old Django/Python copy. Does not apply. |
| `site-cms/DEV_PLAN.md` | History of the base CMS build | Background only |
| `brazen_*_transfer/scrape/therapists*.md` | Source content for migration | Real Brazen content to seed pages + therapists |

**Day-1 action (Dev):** delete `site-cms/coding_guidelines.md`; the only guidelines file in play is the Spring Boot Java one in this folder.

**Note on the guidelines doc:** it contains copy-paste artifacts from another project — the package path `com/onelpro/gayphxapi` (§6) and the `gayphx-api-liquibase/...` changelog path (§20). These literal names are **not** Brazen-specific. Apply the *rule* (layer-first packages; a dedicated Liquibase module/dir) under our own package root `com.brochure.cms`.

---

## 2. Platform we are building on (current state)

The existing `site-cms` is a **mature, production-shaped CMS** — we extend it, not start over.

**Backend** — Spring Boot 3.3.5, Java 21, Gradle
- PostgreSQL 16; **Liquibase** (changesets 001–006); Flyway removed (see §3.1, ALIGN-1)
- Spring Data JPA, Spring Security, JWT + passwordless **magic-link** auth
- Multi-tenant (filter → `TenantContext`; every row carries `tenant_id`)
- OWASP HTML sanitizer + Apache Tika (upload validation), FreeMarker email, hCaptcha
- Tests: JUnit 5, Spring test, **Testcontainers (Postgres)**, H2 for fast tests
- Package layout: **package-by-feature** under `com.brochure.cms.domain.*`; enums already at `com.brochure.cms.enums` (matches guideline §6)
- **Build additions required by the new guidelines** (added in alignment phase): Lombok, JaCoCo (≥80%, enforced pre-launch), springdoc-openapi (Swagger), Liquibase

**Frontend** — Next.js 14 (App Router), React 18, TypeScript, Tailwind
- TanStack Query, react-hook-form + Zod, BlockNote rich-text editor, Playwright e2e
- Admin UI already shipped for: pages, blog, categories, media, menus, staff, faq, gallery, contacts, settings

### 2.1 Reuse map — exists vs. build

| Brazen requirement | Existing capability | Action |
| --- | --- | --- |
| Pages (home/about/services) + content blocks | `domain/page` (Page, ContentBlock, block patterns) | **Reuse.** Seed Brazen content. |
| SEO (title/meta/OG) | `metaTitle`, `metaDescription`, `ogImageUrl` on Page & Blog | **Reuse.** Add `canonicalUrl`. |
| Resources / Blog (categories, scheduled publish) | `domain/blog`, `domain/category`, `ScheduledPublishTask` | **Reuse.** |
| FAQ (create/edit/reorder/categorize) | `domain/faq` | **Reuse.** |
| Media library | `domain/media` (Tika-validated uploads) | **Reuse.** |
| Contact / intake storage | `domain/contact` (+ hCaptcha) | **Reuse** as base for intake. |
| Revisions / audit | `domain/revision` | **Reuse**; extend audit for admin actions. |
| Staff/team members | `domain/staff` (StaffMember) | **Do not overload** — see §4.1. Keep for generic team. |
| **Therapist structured profiles** | — | **BUILD** (`therapist` domain). |
| **Admin-managed taxonomies** | — | **BUILD** (`taxonomy` domain). |
| **Therapist directory** (search/filter/sort) | partial (`domain/search`) | **BUILD** therapist query API. |
| **Matching engine + intake questionnaire** | — | **BUILD** (`matching` domain). |
| **Practice Admin & Therapist self-service roles** | `UserRole {VIEWER, EDITOR, ADMIN}` | **EXTEND** (§5). |
| **Analytics events** | — | **BUILD** lightweight capture (§8). |

---

## 3. Coding standards & conflict rulings (READ FIRST)

All code follows [`coding_guidelines.md`](coding_guidelines.md). Non-negotiables the reviewer enforces on every PR:

- **UUID primary keys** on every entity. No sequential IDs in APIs.
- **DTOs at the boundary**, never expose entities. **All new payload DTOs end in `DTO`** (guideline §7) — e.g. `TherapistResponseDTO`, `IntakeRequestDTO`, `MatchResultDTO`.
- **Constructor injection only** — no field `@Autowired`. (Services/controllers: explicit constructors, **no Lombok** — guideline §9.)
- **Enums** in `com.brochure.cms.enums`, each with a unit test, and **`@Enumerated(EnumType.STRING)`** wherever persisted (guideline §3). Never `ORDINAL`.
- **Lombok** on entities & payload DTOs (guideline §9) — see §3.1 for the BaseEntity caveat.
- **No magic strings/numbers**, no `System.out`/`printStackTrace`, no swallowed exceptions, no raw SQL in code.
- **Global error handling** via existing `@ControllerAdvice` `GlobalExceptionHandler`.
- **Secrets via env/config only.**
- **Tests**: unit (JUnit 5 + Mockito), `@DataJpaTest` (repos), `@WebMvcTest` (controllers), **Testcontainers** integration. Integration classes use the **`IT`** suffix.

### 3.1 Conflict rulings (Senior Dev decisions)

The latest guidelines introduced rules that conflict with the existing codebase. These rulings are binding; if a reviewer flags a "violation," this section is the sanctioned reference.

| # | Conflict | **Ruling** | Timing |
| --- | --- | --- | --- |
| **ALIGN-1** | §20 mandates **Liquibase**; codebase used **Flyway** (V1–V6 applied) | **Switch to Liquibase.** ✅ Done — `V1`–`V6` ported to changesets `001`–`006` under `db/changelog/`, `db.changelog-master.yaml` wired, Flyway removed. Schema parity verified against live Postgres. New Brazen tables are the next changesets. | **Day 0–1, before feature work** |
| **ALIGN-2** | §9 mandates **Lombok everywhere**; codebase is hand-written; entities extend `BaseEntity` and §9 forbids `@Data` on subclasses | **Adopt Lombok across the codebase.** Use `@Getter/@Setter/@Builder/@NoArgsConstructor/@AllArgsConstructor` on entities (**not `@Data`** on `BaseEntity` subclasses — avoids broken `equals/hashCode`). Use `@Data/@Builder` on standalone payload DTOs. **Never** on services/controllers. Retrofit existing entities/DTOs mechanically in the alignment phase. | **Day 0–1** |
| **ALIGN-3** | §6 prescribes **package-by-layer** + interface/`impl`; codebase is **package-by-feature** | **New Brazen domains use layer-first** packages under `com.brochure.cms` (`controllers/`, `services/` + `services/impl/`, `models/`, `repositories/`, `dto/`, `enums/`). **Do NOT restructure existing `domain.*` packages this week** — that is a tracked post-demo refactor (ALIGN-3b). Temporary layout inconsistency is accepted for the demo; document it so devs know where new files go. | New code: now. Existing: **post-demo** |
| **ALIGN-4** | §5 mandates **interface + `impl`** for every service | **Yes for all new Brazen services** (`TherapistService` + `services/impl/TherapistServiceImpl`, etc.). The matching engine especially (`MatchingService` + `WeightedScoreMatchingServiceImpl`) — the algorithm will evolve (rule-based now, AI-assisted later per `project.md`). | New code: now |
| **ALIGN-5** | §12 **JaCoCo ≥80% enforced**; §19 **full WCAG 2.1 AA**; §20 **Swagger on every endpoint** | **Deferred from the demo gate to pre-launch.** For the demo: features work, security/RBAC enforced, no PHI, UI is responsive and keyboard-navigable with semantic HTML + alt text (baseline a11y). Before real launch: enforce 80% coverage in the build, complete the full WCAG 2.1 AA audit, and add OpenAPI annotations to all endpoints. New code should still add Swagger annotations and aim for coverage where cheap. | Baseline now, **full bars pre-launch** |

---

## 4. Data model (new)

All new entities: extend `BaseEntity` (UUID id, timestamps), carry `tenant_id`, ship with a **Liquibase changeset**, use **Lombok** (`@Getter/@Setter/@Builder/@NoArgsConstructor/@AllArgsConstructor`, not `@Data`), and persist enums with **`@Enumerated(EnumType.STRING)`**.

### 4.1 Therapist — `models/Therapist`, `repositories/TherapistRepository`, `services/TherapistService`(+`impl`)

A **dedicated entity**, not an extension of `StaffMember`. Matching needs structured, queryable taxonomy relationships and availability state that don't belong on a generic team record. `StaffMember` remains for non-clinical "our team" content.

Fields: `tenantId`, `userId?` (optional link to `User` for self-service), `firstName`, `lastName`, `credentials`, `pronouns`, `photoUrl`, `slug` (unique per tenant), `bio`, `yearsOfExperience?`, `education`, `licensure`, `serviceDelivery: ServiceDelivery`, `availabilityStatus: AvailabilityStatus`, `schedulingUrl?` (external; **no appointment data stored**), `bookingPlatformRef?`, SEO (`metaTitle`, `metaDescription`, `ogImageUrl`, `canonicalUrl`), `published`, `sortOrder`.
Relationship: many-to-many → `TaxonomyTerm` via `therapist_terms` join (focus areas, modalities, demographics).

`ServiceDelivery` (`VIRTUAL`, `IN_PERSON`, `HYBRID`) and `AvailabilityStatus` (`ACCEPTING`, `LIMITED`, `WAITLIST`, `NOT_ACCEPTING`) → `enums/`, each `@Enumerated(EnumType.STRING)` on the entity, each with a unit test.

### 4.2 Taxonomies — `models/TaxonomyTerm`

Generic, admin-editable lists — satisfies the hard requirement that **admins add focus areas/modalities without a developer**.

`TaxonomyTerm`: `tenantId`, `type: TaxonomyType` (`FOCUS_AREA`, `MODALITY`, `DEMOGRAPHIC`), `label`, `slug` (unique per tenant+type), `description?`, `sortOrder`, `active`.

`TaxonomyType` → `enums/` with unit test, `@Enumerated(EnumType.STRING)`. (`ServiceDelivery`/`AvailabilityStatus` stay fixed enums, not taxonomy terms — matching + UI depend on their fixed semantics.)

### 4.3 Services content type — reuse `Page`

Model each Service (Individual/Couples/Family/Trauma) as a `Page` of `PageType.SERVICE` (extend the existing `PageType` enum) + a `FOCUS_AREA` taxonomy link to derive related therapists. **Zero new tables** for the demo. Promote to a dedicated entity post-demo only if richer metadata is needed.

### 4.4 Intake + matching — `models/IntakeSubmission`, `matching` services

`IntakeSubmission`: `tenantId`, `areasOfConcern: List<UUID>` (FOCUS_AREA term ids, stored `jsonb`), `preferredDelivery: ServiceDelivery?`, `clientDemographic: UUID?`, `preferredModalities: List<UUID>?`, optional preference fields (therapist gender/identity — nullable, don't over-model), `contactEmail?` (opt-in only — **no PHI**, §9), `createdAt`.

`MatchResultDTO` — **computed on demand, not persisted** for the demo. Persist later only if analytics requires it, storing therapist id + score + rank, never questionnaire content tied to identity.

---

## 5. Roles & authorization

Current: `UserRole {VIEWER, EDITOR, ADMIN}`. Map the PRD's four actors:

| PRD actor | Role | Capabilities |
| --- | --- | --- |
| Public Visitor | (unauthenticated) | Browse, search, intake, view matches, follow scheduling links |
| Content Administrator | `EDITOR` (reuse) | Pages, services, blog, FAQ, therapist profiles, publish/unpublish |
| Practice Administrator | `ADMIN` (reuse) | All content + taxonomies + availability + view intake + matching config |
| Therapist (self-service) | **NEW `THERAPIST`** | Edit **own** profile only (bio, photo, availability, scheduling URL) |

**Build:** add `THERAPIST` to `UserRole` (+ unit test). Enforce **row-level ownership** — a `THERAPIST` user may mutate only the `Therapist` whose `userId` matches their own. Taxonomy management and availability overrides are `ADMIN`-only. The reviewer specifically checks this boundary.

---

## 6. Matching engine (rule-based weighted scoring)

Deterministic, explainable, testable. No AI dependency for MVP (AI matching is explicit future scope in `project.md`). Behind `MatchingService` interface → `WeightedScoreMatchingServiceImpl` (ALIGN-4).

```
availability gate:  NOT_ACCEPTING  → excluded
score = w_focus      * jaccardOverlap(intake.areasOfConcern, therapist.focusAreas)
      + w_modality    * jaccardOverlap(intake.preferredModalities, therapist.modalities)
      + w_demographic * matches(intake.clientDemographic, therapist.demographics)
      + w_delivery    * matches(intake.preferredDelivery, therapist.serviceDelivery)
      + availabilityBonus(therapist.availabilityStatus)
```

- Default weights — **in config, not magic numbers**: `w_focus=0.45`, `w_modality=0.20`, `w_demographic=0.15`, `w_delivery=0.10`, availability bonus ≤`0.10` (`ACCEPTING` > `LIMITED` > `WAITLIST`).
- Return **top N (default 5)** ranked therapists.
- **Match explanation** per result: matched terms per dimension (e.g. "Specializes in Anxiety, Trauma — 2 of your 3 areas") so the UI renders *why*.
- **Highest test scrutiny** here: empty intake, no matches, ties, availability gating, weight boundaries.

---

## 7. API surface (new)

DTOs end in `DTO`; OpenAPI annotations added where cheap (full Swagger coverage is pre-launch, ALIGN-5).

Public (unauthenticated), under `/api`:
- `GET  /api/therapists` — directory; params `focusArea`, `modality`, `demographic`, `delivery`, `availability`, `q`, `sort`, pagination → `PagedResponse<TherapistSummaryDTO>`
- `GET  /api/therapists/{slug}` → `TherapistResponseDTO`
- `GET  /api/taxonomies?type=FOCUS_AREA` → `List<TaxonomyTermDTO>` (filter UI + intake form)
- `POST /api/match` — body `IntakeRequestDTO` → `MatchResultDTO` (ranked + explanations). hCaptcha-protected like contact.

Admin (`EDITOR`/`ADMIN`), under `/api/admin`:
- `CRUD /api/admin/therapists` (+ publish/unpublish)
- `CRUD /api/admin/taxonomies` (`ADMIN` only for create/delete)
- `GET  /api/admin/intake-submissions` (Practice Admin — `ADMIN`)

Therapist self-service (`THERAPIST`):
- `GET/PUT /api/me/therapist` — own profile only (ownership-enforced)

All inputs validated (`spring-boot-starter-validation`); all admin mutations audit-logged.

---

## 8. Frontend surface (Next.js)

Reuse existing admin shell + TanStack Query + react-hook-form/Zod patterns. **Baseline accessibility is a demo requirement** (semantic HTML, labelled form inputs, alt text, keyboard navigation, responsive layout); the full WCAG 2.1 AA audit is pre-launch (ALIGN-5).

Public:
- `/therapists` — directory with taxonomy-driven filter sidebar, search, sort, cards
- `/therapists/[slug]` — profile (bio, credentials, specialties, modalities, demographics, availability badge, **scheduling CTA**)
- `/match` — multi-step intake questionnaire → results page with ranked cards + match explanation + profile/scheduling links
- Marketing pages (home/about/services) via existing `[slug]` renderer

Admin:
- `/admin/therapists` (list + editor, mirrors `/admin/staff` & `/admin/pages`)
- `/admin/taxonomies` (manage focus areas / modalities / demographics)
- `/admin/intake-submissions` (Practice Admin)

**Analytics (lightweight):** `POST /api/events` → `analytics_events` table capturing page view, profile view, directory search, match start/complete, scheduling-link click. **Never store clinical info.** Dashboards post-demo.

---

## 9. Security & compliance (hard constraints)

- **No PHI, ever.** No EHR/EMR, session notes, clinical records, appointment data, billing, insurance. Intake captures *preferences only*. Release blocker if violated.
- Intake `contactEmail` is opt-in only, treated as ordinary contact data.
- RBAC enforced server-side on every endpoint; therapist self-service ownership-scoped.
- HTTPS-only; secrets via env; HTML sanitized on input (OWASP); uploads validated via Tika.
- **Audit logging** for all admin mutations (extend revision/audit pattern).
- **Multi-tenant isolation:** every new query tenant-scoped via `TenantContext`. Reviewer checks no new repo method can leak across tenants. (Brazen is tenant #1.)

---

## 10. One-week demo plan

Demo must-haves (all four, per kickoff): **directory + profiles, matching questionnaire + results, admin manages therapists + taxonomies, public marketing pages.**

> **Schedule risk:** ALIGN-1 (Liquibase port) and ALIGN-2 (Lombok retrofit) consume Day 0–1 before feature work starts. This is a deliberate trade — foundations now, less rework later — but it compresses the feature window. If days slip, matching polish + analytics are cut first; the directory/profiles/admin/marketing floor is protected.

| Day | Backend | Frontend | Reviewer gate |
| --- | --- | --- | --- |
| 0–1 | **Platform Alignment:** delete stale guidelines; add Lombok/JaCoCo/springdoc/Liquibase deps; **✅ port Flyway V1–V6 → Liquibase changesets 001–006** and verify schema parity; retrofit existing entities/DTOs to Lombok; add `THERAPIST` role. | — | Schema parity proven (Liquibase = current DB); app boots; existing tests green. |
| 1 | Brazen changesets: therapist, taxonomy, therapist_terms, analytics_events. New enums + tests (`@Enumerated(EnumType.STRING)`). | Scaffold `/therapists`, `/match`, admin therapist/taxonomy routes. | Schema + enums reviewed before service code. |
| 2 | Taxonomy CRUD (admin) + public read. Therapist CRUD + publish. DTOs (`…DTO`) + validation. Service interfaces + `impl`. | Admin taxonomy + therapist editor screens. | Tenant scoping + DTO boundary + interface/impl check. |
| 3 | Public directory query API (filter/sort/search) + therapist detail. | Directory list + filters + profile page. | N+1 query check; `@DataJpaTest` coverage. |
| 4 | `MatchingService` + weighted impl + **exhaustive unit tests**; `POST /api/match`. | Multi-step intake form (react-hook-form + Zod). | Matching algorithm + test review (highest scrutiny). |
| 5 | Match results wiring + explanations; analytics event endpoint. | Results page + scheduling CTAs; marketing pages from migrated content. | `@WebMvcTest` + matching flow `IT`. |
| 6 | Seed real Brazen content (pages, therapists from `brazen_*` docs); audit logging on admin mutations. | Polish; responsive + baseline a11y pass; empty/error states. | Full `./gradlew test` + Playwright e2e green. |
| 7 | **Demo dry-run + buffer**; fix dry-run issues. | Demo dry-run. | Go/no-go vs §11 DoD. |

---

## 11. Definition of Done (per feature)

**Demo gate (this week):**
- [ ] **Liquibase changeset** written (new file, sequential id; never edits an applied changeset), applies cleanly, idempotent
- [ ] Entities: UUID PKs, tenant-scoped, extend `BaseEntity`, **Lombok** (`@Getter/@Setter/@Builder/@No-/@AllArgsConstructor`, not `@Data`), persisted enums `@Enumerated(EnumType.STRING)`
- [ ] Enums in `enums/` with unit tests
- [ ] **DTOs at the boundary, names end in `DTO`**; no entity exposure
- [ ] Service **interface + `impl`**; constructor injection; no field `@Autowired`; no magic strings/numbers
- [ ] Authorization enforced server-side (ownership-scoped for therapist self-service)
- [ ] Unit tests (services + Mockito), `@DataJpaTest` (repos), `@WebMvcTest` (controllers), `IT` integration for critical workflow (Testcontainers Postgres)
- [ ] `./gradlew test` green; no compiler/IDE warnings; no `TODO`/temp mocks committed
- [ ] Public APIs documented with Javadoc
- [ ] Frontend: responsive, semantic HTML, labelled inputs, alt text, keyboard-navigable (baseline a11y)
- [ ] Reviewer (senior dev) approval

**Pre-launch gate (after demo, ALIGN-5):**
- [ ] JaCoCo ≥80% enforced in the Gradle build
- [ ] Full WCAG 2.1 AA audit (axe/Lighthouse/Pa11y + manual screen-reader + contrast) passed
- [ ] OpenAPI/Swagger annotations on every endpoint
- [ ] Existing `domain.*` packages restructured to layer-first (ALIGN-3b)

---

## 12. Risks & mitigations

| Risk | Impact | Mitigation |
| --- | --- | --- |
| ALIGN-1/2 (Liquibase port + Lombok retrofit) eat Day 0–1 | Less feature time | Mechanical, low-logic changes done first; schema parity proven before features; matching/analytics cut first if slipping |
| Liquibase port diverges from current schema | Broken DB before demo | Diff ported schema against live DB; keep existing tests green as the oracle |
| Matching engine novel + highest-risk | Demo centerpiece weak | Rule-based + heavily unit-tested; interface enables later swap; first thing simplified if time slips |
| Mixed package layouts (new layer-first vs old feature-first) confuse devs | Misplaced files | §3.1 ALIGN-3 documents exactly where new files go; full unification tracked post-demo |
| Real Brazen content not ready | Demo looks empty | Seed from `brazen_*` scrape docs; placeholder fixtures ready |
| PHI creep via intake free-text | Compliance breach | Structured preferences only; opt-in email the only PII; §9 review gate |
| Tenant leakage in new queries | Cross-client exposure | Every new repo method tenant-scoped; reviewer checks each |
| Deferred a11y/coverage bars forgotten | Launch slips/quality gap | Pre-launch DoD in §11 + tracked ALIGN-5 |

---

*End of Engineering PRD.*
