--liquibase formatted sql

--changeset cms-team:013-seed-services-pricing
--comment: Pickles Bodega "Shop" content page + home page CTA

-- Shop page (not in nav; nav "Shop" points directly at /products)
INSERT INTO pages (id, tenant_id, slug, title, page_type, layout, nav_order, published, meta_title, meta_description)
VALUES (
    'b0000000-0000-4000-8000-000000000020',
    'a0000000-0000-4000-8000-000000000001',
    'shop',
    'Shop',
    'custom',
    'contained',
    0,
    TRUE,
    'Shop | Pickles Bodega',
    'How to shop gluten-free and allergy-friendly groceries at Pickles Bodega in Flagstaff.'
) ON CONFLICT (tenant_id, slug) DO NOTHING;

INSERT INTO content_blocks (page_id, block_type, sort_order, content)
SELECT 'b0000000-0000-4000-8000-000000000020', 'hero', 0,
    '{"heading":"Shop Pickles Bodega","subheading":"Browse gluten-free, dairy-free, nut-free, and other allergy-friendly foods.","buttonText":"Start shopping","buttonUrl":"/products","backgroundImage":"/images/smiling-woman-buying-organic-products-in-zero-wast-2026-01-08-22-29-57-utc.jpg","overlay":true}'::jsonb
WHERE EXISTS (SELECT 1 FROM pages WHERE id = 'b0000000-0000-4000-8000-000000000020')
  AND NOT EXISTS (SELECT 1 FROM content_blocks WHERE page_id = 'b0000000-0000-4000-8000-000000000020' AND sort_order = 0);

INSERT INTO content_blocks (page_id, block_type, sort_order, content)
SELECT 'b0000000-0000-4000-8000-000000000020', 'text', 1,
    '{"body":"<p>Everything on our shelves is labeled by the allergens it avoids. Filter by Gluten-Free, Dairy-Free, Nut-Free, Soy-Free, Egg-Free, Shellfish-Free, or Sesame-Free. You can also shop by category: Bakery, Pantry, Frozen, Refrigerated, Produce, Snacks, and Beverages.</p><p>Not sure what to avoid? Take the <a href=\"/intake\">Allergy Check</a> and we will suggest products that fit your needs.</p>","alignment":"left"}'::jsonb
WHERE EXISTS (SELECT 1 FROM pages WHERE id = 'b0000000-0000-4000-8000-000000000020')
  AND NOT EXISTS (SELECT 1 FROM content_blocks WHERE page_id = 'b0000000-0000-4000-8000-000000000020' AND sort_order = 1);

INSERT INTO content_blocks (page_id, block_type, sort_order, content)
SELECT 'b0000000-0000-4000-8000-000000000020', 'cta', 2,
    '{"heading":"Not sure what you can eat?","body":"Answer a few quick questions and we will suggest products that fit your needs.","primaryButton":{"text":"Check my allergies","url":"/intake"},"secondaryButton":{"text":"Browse products","url":"/products"}}'::jsonb
WHERE EXISTS (SELECT 1 FROM pages WHERE id = 'b0000000-0000-4000-8000-000000000020')
  AND NOT EXISTS (SELECT 1 FROM content_blocks WHERE page_id = 'b0000000-0000-4000-8000-000000000020' AND sort_order = 2);

-- Home page: grocery CTA (no therapy images)
INSERT INTO content_blocks (page_id, block_type, sort_order, content)
SELECT 'b0000000-0000-4000-8000-000000000001', 'cta', 1,
    '{"heading":"Find foods that fit your needs","body":"Filter by allergen, diet, or store section. Take the Allergy Check for personalized suggestions.","primaryButton":{"text":"Shop products","url":"/products"},"secondaryButton":{"text":"Allergy check","url":"/intake"}}'::jsonb
WHERE NOT EXISTS (SELECT 1 FROM content_blocks WHERE page_id = 'b0000000-0000-4000-8000-000000000001' AND sort_order = 1);
