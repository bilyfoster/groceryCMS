# Pickles Bodega — Gluten-Free & Allergy-Friendly Grocery CMS

A grocery catalog and content CMS for **Pickles Bodega** in Flagstaff, Arizona.
Staff can add food items, tag them by allergens they avoid, diet types, and
product categories, and shoppers can filter the catalog or use an
allergy-awareness intake to discover suitable products.

Built on the same Spring Boot + Next.js multi-tenant foundation as BrazenCMS.

## Repository layout

```
groceryCMS/
├── backend/      Spring Boot 3.3.5 / Java 21 / Gradle — REST API, auth, product catalog
├── frontend/     Next.js 14 / React 18 / TypeScript / Tailwind — public site + admin
├── docker-compose.prod.yml   production compose
├── docker-compose.yml
└── docs/         project documentation (inherited from BrazenCMS base)
```

## Key features

- **Product catalog** — name, brand, price, unit, photo, stock status, store section.
- **Allergy & diet filtering** — powered by admin-managed taxonomy terms:
  - `ALLERGY_TYPE` (e.g. Gluten-Free, Dairy-Free, Nut-Free)
  - `DIET_TYPE` (e.g. Vegan, Keto, Paleo, Organic)
  - `PRODUCT_CATEGORY` (e.g. Bakery, Pantry, Frozen)
- **Allergy-awareness intake** (`/intake` and `/match`) — asks about avoided
  allergens, symptoms, diet, and categories, then recommends products and
  surfaces non-clinical notes about when to talk to a healthcare provider.
- **CMS basics** — pages, blog, menus, media, staff, settings remain available.

## Local development

```bash
docker compose up --build      # db (Postgres 5433), mailpit, backend (8080), frontend (3000)
```

Backend only:
```bash
cd backend && ./gradlew bootRun
cd backend && ./gradlew test
```

Frontend only:
```bash
cd frontend && npm install && npm run build
```

## Notes

- No payment/checkout is implemented; this is a catalog + inquiry site.
- The intake/matching tool does **not** diagnose allergies. It only helps
  shoppers spot possible concerns to discuss with a clinician.
- Multi-tenant plumbing is unchanged; Pickles Bodega operates as tenant #1.
