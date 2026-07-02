--liquibase formatted sql

--changeset cms-team:010-seed-demo-taxonomies
--comment: Seed starter taxonomy terms for the demo tenant

INSERT INTO taxonomy_terms (id, tenant_id, type, label, slug, description, sort_order, active, created_at, updated_at)
SELECT gen_random_uuid(), t.id, 'FOCUS_AREA', terms.label, terms.slug, NULL, terms.sort_order, true, NOW(), NOW()
FROM tenants t
CROSS JOIN (VALUES
    ('Anxiety', 'anxiety', 1),
    ('Depression', 'depression', 2),
    ('Trauma', 'trauma', 3),
    ('PTSD', 'ptsd', 4),
    ('LGBTQ+', 'lgbtq', 5),
    ('Gender Identity', 'gender-identity', 6),
    ('Neurodivergence', 'neurodivergence', 7),
    ('Relationship Issues', 'relationship-issues', 8),
    ('Grief', 'grief', 9),
    ('Addiction', 'addiction', 10),
    ('Life Transitions', 'life-transitions', 11)
) AS terms(label, slug, sort_order)
WHERE t.slug = 'demo'
  AND NOT EXISTS (
      SELECT 1 FROM taxonomy_terms tt
      WHERE tt.tenant_id = t.id AND tt.type = 'FOCUS_AREA' AND tt.slug = terms.slug
  );

INSERT INTO taxonomy_terms (id, tenant_id, type, label, slug, description, sort_order, active, created_at, updated_at)
SELECT gen_random_uuid(), t.id, 'MODALITY', terms.label, terms.slug, NULL, terms.sort_order, true, NOW(), NOW()
FROM tenants t
CROSS JOIN (VALUES
    ('CBT', 'cbt', 1),
    ('DBT', 'dbt', 2),
    ('EMDR', 'emdr', 3),
    ('ACT', 'act', 4),
    ('Somatic Therapy', 'somatic-therapy', 5),
    ('Narrative Therapy', 'narrative-therapy', 6)
) AS terms(label, slug, sort_order)
WHERE t.slug = 'demo'
  AND NOT EXISTS (
      SELECT 1 FROM taxonomy_terms tt
      WHERE tt.tenant_id = t.id AND tt.type = 'MODALITY' AND tt.slug = terms.slug
  );

INSERT INTO taxonomy_terms (id, tenant_id, type, label, slug, description, sort_order, active, created_at, updated_at)
SELECT gen_random_uuid(), t.id, 'DEMOGRAPHIC', terms.label, terms.slug, NULL, terms.sort_order, true, NOW(), NOW()
FROM tenants t
CROSS JOIN (VALUES
    ('Children', 'children', 1),
    ('Adolescents', 'adolescents', 2),
    ('Adults', 'adults', 3),
    ('Seniors', 'seniors', 4),
    ('Couples', 'couples', 5),
    ('Families', 'families', 6)
) AS terms(label, slug, sort_order)
WHERE t.slug = 'demo'
  AND NOT EXISTS (
      SELECT 1 FROM taxonomy_terms tt
      WHERE tt.tenant_id = t.id AND tt.type = 'DEMOGRAPHIC' AND tt.slug = terms.slug
  );
