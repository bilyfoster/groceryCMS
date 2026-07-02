# Pickles Bodega Grocery CMS — Implementation Plan

## Goal
Convert the copied BrazenCMS codebase into a grocery-store CMS for Pickles Bodega where staff can manage food items, shoppers can filter by allergens/diet/category, and an intake questionnaire raises awareness about possible food allergies.

## What changed

### Backend
- Replaced `Therapist` domain with `Product` (food/grocery item):
  - `models/Product.java`, `repositories/ProductRepository.java`
  - `dto/Product{Request,Response,Summary}DTO.java`
  - `services/ProductService.java` + `services/impl/ProductServiceImpl.java`
  - `controllers/ProductController.java` + `controllers/AdminProductController.java`
- Replaced therapist taxonomy types with grocery taxonomy types:
  - `TaxonomyType`: `ALLERGY_TYPE`, `DIET_TYPE`, `PRODUCT_CATEGORY`
  - `IntakeCriterion`: same + `SYMPTOM` + `STORE_SECTION`
- Renamed `AvailabilityStatus` → `StockStatus` and `ServiceDelivery` → `StoreSection`.
- Replaced matching engine with `ProductRecommendationServiceImpl`:
  - Excludes products containing avoided allergens.
  - Scores by diet/category overlap and stock status.
  - Returns allergy-awareness notes based on symptom selections.
- Added Liquibase migration `033-products.sql` for `products` and `product_terms` tables.
- Updated seed taxonomy/demo data and nav menu to grocery terms (`/products`, `/intake`).
- Removed all therapist-specific backend classes and tests.

### Frontend
- Renamed `therapists` → `products` (public directory + admin).
- New product types (`types/product.ts`) with stock status, store section, price helpers.
- Updated API client for products.
- New product listing, detail, and admin CRUD pages.
- Updated intake (`/intake`) and match wizard (`/match`) for allergens, symptoms, diet, categories.
- Added `ProductRecommendationList` component with awareness notes.
- Updated admin sidebar, taxonomies page, service-page related products, and default metadata.

### Docs / metadata
- Rewrote `README.md` for Pickles Bodega.
- Updated `docs/project.md` overview.
- Removed Brazen-specific scrape/profile docs.

## Verification
- `cd backend && ./gradlew test` passes.
- `cd frontend && npm install && npm run build` succeeds.

## Out of scope
- Payment/checkout.
- Medical diagnosis (intake is informational only).
