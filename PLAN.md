# Brazen CMS — Session Recovery & Completion Plan

> Recovered after connection loss. This plan is based on the repo state plus the source-of-truth docs:
> `docs/project.md`, `docs/ENGINEERING_PRD.md`, `docs/DEV_DIRECTIONS.md`, `docs/coding_guidelines.md`.

## Current state (as of 2026-06-17)

### ✅ Done
- **ALIGN-1 (Liquibase port):** `V1`–`V6` ported to changesets `001`–`006`; Flyway removed; `db.changelog-master.yaml` wired.
- **Taxonomy backend (reference slice):** layer-first under `com.brochure.cms` — `TaxonomyTerm`, `TaxonomyType`, DTOs, repo, `TaxonomyService`/`TaxonomyServiceImpl`, public + admin controllers, changeset `006-taxonomy.sql`.
- **Roles:** `UserRole.THERAPIST` added.
- **Build deps:** Lombok + springdoc already on classpath.
- **Working tree:** only one unstaged change — `frontend/playwright.prod.config.ts` was deleted.

### ❌ Not done
- **ALIGN-2 (Lombok retrofit):** existing `domain.*` entities/DTOs still use hand-written getters/setters (e.g. `BlogPost`).
- **Admin taxonomy UI:** no `frontend/src/app/admin/taxonomies/page.tsx` and no public taxonomy filter wiring.
- **Therapist domain:** no schema, entity, repository, service, controller, or frontend pages.
- **Directory & matching:** no `/therapists`, `/match`, `MatchingService`, or `IntakeSubmission`.
- **Service pages:** `PageType.SERVICE` and related-therapist wiring not added.
- **Analytics:** no `analytics_events` table/endpoint.
- **JaCoCo 80% / full WCAG / Swagger everywhere:** deferred pre-launch per ALIGN-5.

## Completion plan

Work in the order below. Each task is a branch/PR; the reviewer checks against PRD §11 (Demo DoD).

### Task 0 — Housekeeping
1. Decide on `frontend/playwright.prod.config.ts`:
   - If it was intentionally removed, `git rm` and commit.
   - If accidental, `git restore frontend/playwright.prod.config.ts`.
2. Run `./gradlew test` and the frontend build to confirm baseline green before feature work.

### Task 1 — ALIGN-2: Lombok retrofit of existing entities/DTOs
- Convert existing `domain.*` entities and DTOs to Lombok (`@Getter/@Setter/@Builder/@NoArgsConstructor/@AllArgsConstructor`, **not** `@Data` on `BaseEntity` subclasses).
- Keep tests green. No logic changes.
- This is mechanical and should be done before scaling the feature set so new code follows one convention.

### Task 2 — Taxonomy admin UI + public filter wiring
- Build `frontend/src/app/admin/taxonomies/page.tsx` mirroring `/admin/staff` patterns.
- Add taxonomy CRUD API client functions in `frontend/src/lib/api.ts`.
- Wire public taxonomy terms into directory filter UI and match intake form.

### Task 3 — Therapist domain
- Add Liquibase changeset `008-therapist.sql` (therapists, therapist_terms join, indexes).
- Add enums: `ServiceDelivery`, `AvailabilityStatus` (with unit tests, `@Enumerated(EnumType.STRING)`).
- Add `models/Therapist`, `repositories/TherapistRepository`, DTOs (`TherapistRequestDTO`, `TherapistResponseDTO`, `TherapistSummaryDTO`), `services/TherapistService` + `impl`, `controllers/TherapistController` + `AdminTherapistController`, and `MeTherapistController` for self-service.
- Enforce row-level ownership for `THERAPIST` role.
- Admin UI: `/admin/therapists` (list + editor).
- Public UI: `/therapists/[slug]` profile page.

### Task 4 — Directory
- Backend: `GET /api/therapists` with filter/sort/search + `PagedResponse<TherapistSummaryDTO>`.
- Frontend: `/therapists` directory with taxonomy-driven filters, search, sort, cards.
- Verify tenant scoping and N+1 queries.

### Task 5 — Matching engine
- Add `models/IntakeSubmission` and DTOs (`IntakeRequestDTO`, `MatchResultDTO`).
- Add `MatchingService` interface + `WeightedScoreMatchingServiceImpl` with configurable weights and match explanations.
- Add `POST /api/match` (hCaptcha-protected).
- Frontend: multi-step `/match` intake form + results page with ranked cards + explanations + scheduling CTAs.
- Heavy unit-test coverage for the scoring algorithm.

### Task 6 — Services + marketing pages
- Extend `PageType` enum with `SERVICE`.
- Model each service as a `Page` of type `SERVICE` linked to a focus-area taxonomy term for related therapists.
- Seed home/about/services content from `docs/content/`.

### Task 7 — Analytics events
- Add `analytics_events` table via Liquibase.
- Add `POST /api/events` endpoint.
- Capture page view, profile view, directory search, match start/complete, scheduling-link click. No clinical data.

### Task 8 — Seed real content + audit + polish
- Seed therapist profiles from `docs/content/`.
- Add audit logging on all admin mutations.
- Responsive pass, baseline a11y (semantic HTML, labels, alt text, keyboard nav), empty/error states.
- Final `./gradlew test` green + Playwright e2e green.

## Cut order if schedule slips
Protected demo floor (PRD §10): directory + profiles, matching questionnaire + results, admin manages therapists + taxonomies, public marketing pages.

If time runs short, cut in this order:
1. Analytics events (Task 7)
2. Matching polish/explanations (Task 5 detail work)
3. Service-page polish (Task 6 detail work)

## Non-negotiables for every PR
- UUID PKs; DTOs at boundary with `DTO` suffix; no entity exposure.
- Constructor injection only; no field `@Autowired`; no Lombok on services/controllers.
- Enums in `com.brochure.cms.enums`, each with unit test, persisted `@Enumerated(EnumType.STRING)`.
- Tenant-scoped queries via `TenantIds.current()`.
- Server-side RBAC; therapist self-service ownership-scoped.
- New Liquibase changeset per schema change; never edit an applied changeset.
- No `System.out`/`printStackTrace`, no swallowed exceptions, no magic strings/numbers, no raw SQL.
- `./gradlew test` green, zero warnings, no `TODO`/temp mocks committed.

## Next action
Pick the starting task:
- **A)** I’ll handle Task 0 (playwright config + baseline test run) and then Task 1 (Lombok retrofit).
- **B)** Skip Lombok retrofit for now and start Task 2 (admin taxonomy UI).
- **C)** Jump straight to Task 3 (Therapist domain).
