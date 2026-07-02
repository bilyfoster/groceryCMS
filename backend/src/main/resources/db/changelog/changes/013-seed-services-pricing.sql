--liquibase formatted sql

--changeset cms-team:013-seed-services-pricing
--comment: Brazen Services & Pricing page (tiers mirror brazentherapy.org/services-7) + photo accents

-- Services & Pricing page (demo tenant)
INSERT INTO pages (id, tenant_id, slug, title, page_type, layout, nav_order, published, meta_title, meta_description)
VALUES (
    'b0000000-0000-4000-8000-000000000020',
    'a0000000-0000-4000-8000-000000000001',
    'services',
    'Services & Pricing',
    'service',
    'full-width',
    5,
    TRUE,
    'Services & Pricing | Brazen Therapy',
    'Affordable, accessible therapy. Individual, family, couples, and reduced-rate intern sessions. Most major insurance accepted.'
) ON CONFLICT (tenant_id, slug) DO NOTHING;

-- Blocks for the Services page.
-- Guards: only run when the demo Services page (…020) actually exists (fresh demo seed).
-- On environments where a different "services" page already exists (e.g. production),
-- the page INSERT above is skipped by ON CONFLICT, so these become safe no-ops.
INSERT INTO content_blocks (page_id, block_type, sort_order, content)
SELECT 'b0000000-0000-4000-8000-000000000020', 'hero', 0,
    '{"heading":"Services & Pricing","subheading":"Affordable, accessible therapy for individuals, families, and couples.","backgroundImage":"/images/service-individual.jpg","overlay":true,"buttonText":"Find your therapist","buttonUrl":"/match"}'::jsonb
WHERE EXISTS (SELECT 1 FROM pages WHERE id = 'b0000000-0000-4000-8000-000000000020')
  AND NOT EXISTS (SELECT 1 FROM content_blocks WHERE page_id = 'b0000000-0000-4000-8000-000000000020' AND sort_order = 0);

INSERT INTO content_blocks (page_id, block_type, sort_order, content)
SELECT 'b0000000-0000-4000-8000-000000000020', 'pricing', 1,
    '{
      "heading":"Our Services",
      "intro":"At Brazen Therapy, we strive to provide affordable and accessible therapy services to all of our clients.",
      "note":"Prices listed are for out-of-pocket clients not using insurance benefits.",
      "tiers":[
        {"name":"Individual Therapy","image":"/images/service-individual.jpg","imageAlt":"A person sitting with hands clasped during a one-on-one session","description":"One-on-one treatment addressing emotional, psychological, or behavioral concerns.","prices":[{"label":"Intake","amount":"$235"},{"label":"Session","amount":"$180"}],"cta":{"text":"Get started","url":"/intake"}},
        {"name":"Family Therapy","image":"/images/service-family.jpg","imageAlt":"A parent and child embracing","description":"Working with family members to improve communication and resolve conflicts.","prices":[{"label":"Intake","amount":"$235"},{"label":"Session","amount":"$200"}],"cta":{"text":"Get started","url":"/intake"}},
        {"name":"Couples Therapy","image":"/images/service-couples.jpg","imageAlt":"A couple sitting together, smiling","description":"Helping couples address relationship challenges and deepen connection.","prices":[{"label":"Intake","amount":"$235"},{"label":"Session","amount":"$200"}],"cta":{"text":"Get started","url":"/intake"}},
        {"name":"Intern Therapy","featured":true,"image":"/images/service-intern.jpg","imageAlt":"A masters-level clinician smiling in a bright office","description":"Masters-level clinicians under weekly supervision, offered at reduced rates.","prices":[{"label":"Intake","amount":"$75"},{"label":"Individual","amount":"$30"},{"label":"Couple / Family","amount":"$60"}],"cta":{"text":"Get started","url":"/intake"}}
      ],
      "paymentNote":"A credit card is required on file via Stripe (Visa, Mastercard, Discover, American Express, and HSA/FSA cards accepted).",
      "insurance":["Blue Cross Blue Shield","Aetna","Cigna","United Healthcare / UMR / Optum","CHAMPVA","Tricare / Triwest"]
    }'::jsonb
WHERE EXISTS (SELECT 1 FROM pages WHERE id = 'b0000000-0000-4000-8000-000000000020')
  AND NOT EXISTS (SELECT 1 FROM content_blocks WHERE page_id = 'b0000000-0000-4000-8000-000000000020' AND sort_order = 1);

INSERT INTO content_blocks (page_id, block_type, sort_order, content)
SELECT 'b0000000-0000-4000-8000-000000000020', 'cta', 3,
    '{"heading":"Not sure where to start?","body":"Answer a few quick questions and we will match you with therapists who fit your needs.","primaryButton":{"text":"Take the matching quiz","url":"/match"},"secondaryButton":{"text":"Browse therapists","url":"/therapists"}}'::jsonb
WHERE EXISTS (SELECT 1 FROM pages WHERE id = 'b0000000-0000-4000-8000-000000000020')
  AND NOT EXISTS (SELECT 1 FROM content_blocks WHERE page_id = 'b0000000-0000-4000-8000-000000000020' AND sort_order = 3);

-- Add "Services" to the header menu (only when the demo page + menu exist)
INSERT INTO menu_items (id, menu_id, label, page_id, sort_order)
SELECT 'd0000000-0000-4000-8000-000000000013', 'd0000000-0000-4000-8000-000000000001', 'Services',
       'b0000000-0000-4000-8000-000000000020', 5
WHERE EXISTS (SELECT 1 FROM pages WHERE id = 'b0000000-0000-4000-8000-000000000020')
  AND EXISTS (SELECT 1 FROM menus WHERE id = 'd0000000-0000-4000-8000-000000000001')
  AND NOT EXISTS (SELECT 1 FROM menu_items WHERE id = 'd0000000-0000-4000-8000-000000000013');

-- Add "life" to the home page: warm hero background + a community band + CTA
UPDATE content_blocks
SET content = content || '{"backgroundImage":"/images/consultation.jpg","overlay":true}'::jsonb
WHERE page_id = 'b0000000-0000-4000-8000-000000000001'
  AND block_type = 'hero'
  AND content->>'backgroundImage' IS NULL;

INSERT INTO content_blocks (page_id, block_type, sort_order, content)
SELECT 'b0000000-0000-4000-8000-000000000001', 'image', 1,
    '{"url":"/images/community.jpg","altText":"A support group sitting together in a circle, holding hands","caption":"You do not have to do this alone.","width":1600,"alignment":"center"}'::jsonb
WHERE NOT EXISTS (SELECT 1 FROM content_blocks WHERE page_id = 'b0000000-0000-4000-8000-000000000001' AND sort_order = 1);

INSERT INTO content_blocks (page_id, block_type, sort_order, content)
SELECT 'b0000000-0000-4000-8000-000000000001', 'cta', 2,
    '{"heading":"Find the right therapist for you","body":"Answer a few quick questions and we will match you with therapists who fit your needs.","primaryButton":{"text":"Take the matching quiz","url":"/match"},"secondaryButton":{"text":"Browse therapists","url":"/therapists"}}'::jsonb
WHERE NOT EXISTS (SELECT 1 FROM content_blocks WHERE page_id = 'b0000000-0000-4000-8000-000000000001' AND sort_order = 2);
