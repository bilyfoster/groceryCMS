--liquibase formatted sql

--changeset cms-team:010-seed-demo-taxonomies
--comment: Seed starter taxonomy terms for the Pickles Bodega grocery tenant

INSERT INTO taxonomy_terms (id, tenant_id, type, label, slug, description, sort_order, active, created_at, updated_at)
SELECT gen_random_uuid(), t.id, 'ALLERGY_TYPE', terms.label, terms.slug, NULL, terms.sort_order, true, NOW(), NOW()
FROM tenants t
CROSS JOIN (VALUES
    ('Gluten-Free', 'gluten-free', 1),
    ('Dairy-Free', 'dairy-free', 2),
    ('Nut-Free', 'nut-free', 3),
    ('Soy-Free', 'soy-free', 4),
    ('Egg-Free', 'egg-free', 5),
    ('Shellfish-Free', 'shellfish-free', 6),
    ('Sesame-Free', 'sesame-free', 7)
) AS terms(label, slug, sort_order)
WHERE t.slug = 'demo'
  AND NOT EXISTS (
      SELECT 1 FROM taxonomy_terms tt
      WHERE tt.tenant_id = t.id AND tt.type = 'ALLERGY_TYPE' AND tt.slug = terms.slug
  );

INSERT INTO taxonomy_terms (id, tenant_id, type, label, slug, description, sort_order, active, created_at, updated_at)
SELECT gen_random_uuid(), t.id, 'DIET_TYPE', terms.label, terms.slug, NULL, terms.sort_order, true, NOW(), NOW()
FROM tenants t
CROSS JOIN (VALUES
    ('Vegan', 'vegan', 1),
    ('Vegetarian', 'vegetarian', 2),
    ('Keto', 'keto', 3),
    ('Paleo', 'paleo', 4),
    ('Organic', 'organic', 5),
    ('Low-FODMAP', 'low-fodmap', 6),
    ('Whole30', 'whole30', 7)
) AS terms(label, slug, sort_order)
WHERE t.slug = 'demo'
  AND NOT EXISTS (
      SELECT 1 FROM taxonomy_terms tt
      WHERE tt.tenant_id = t.id AND tt.type = 'DIET_TYPE' AND tt.slug = terms.slug
  );

INSERT INTO taxonomy_terms (id, tenant_id, type, label, slug, description, sort_order, active, created_at, updated_at)
SELECT gen_random_uuid(), t.id, 'PRODUCT_CATEGORY', terms.label, terms.slug, NULL, terms.sort_order, true, NOW(), NOW()
FROM tenants t
CROSS JOIN (VALUES
    ('Bakery', 'bakery', 1),
    ('Pantry', 'pantry', 2),
    ('Frozen', 'frozen', 3),
    ('Refrigerated', 'refrigerated', 4),
    ('Produce', 'produce', 5),
    ('Snacks', 'snacks', 6),
    ('Beverages', 'beverages', 7)
) AS terms(label, slug, sort_order)
WHERE t.slug = 'demo'
  AND NOT EXISTS (
      SELECT 1 FROM taxonomy_terms tt
      WHERE tt.tenant_id = t.id AND tt.type = 'PRODUCT_CATEGORY' AND tt.slug = terms.slug
  );
