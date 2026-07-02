--liquibase formatted sql

--changeset cms-team:ALIGN-1-005-seed-block-patterns
--comment: System block patterns (Phase 9)

INSERT INTO block_patterns (id, tenant_id, name, category, blocks, is_system)
VALUES
(
    'f0000000-0000-4000-8000-000000000001',
    NULL,
    'Hero Centered',
    'hero',
    '[{"blockType":"hero","content":{"heading":"Welcome","subheading":"Your tagline here","buttonText":"Get started","buttonUrl":"/contact","backgroundImage":"","overlay":true,"layout":"centered"}}]'::jsonb,
    TRUE
),
(
    'f0000000-0000-4000-8000-000000000002',
    NULL,
    'Three Features',
    'features',
    '[{"blockType":"columns","content":{"columns":[{"blocks":[{"blockType":"icon_text","content":{"icon":"star","heading":"Feature one","body":"Describe your first benefit."}}]},{"blocks":[{"blockType":"icon_text","content":{"icon":"zap","heading":"Feature two","body":"Describe your second benefit."}}]},{"blocks":[{"blockType":"icon_text","content":{"icon":"shield","heading":"Feature three","content":"Describe your third benefit."}}]}]}}]'::jsonb,
    TRUE
),
(
    'f0000000-0000-4000-8000-000000000003',
    NULL,
    'CTA Banner',
    'cta',
    '[{"blockType":"cta","content":{"heading":"Ready to get started?","body":"Contact us today.","primaryButton":{"text":"Contact us","url":"/contact"},"secondaryButton":{"text":"","url":""}}}]'::jsonb,
    TRUE
)
ON CONFLICT (id) DO NOTHING;
