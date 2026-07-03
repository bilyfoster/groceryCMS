--liquibase formatted sql

--changeset cms-team:036-fundraiser-design-fields
--comment: Add editable campaign hero eyebrow labels

WITH hero_labels(page_id, eyebrow) AS (
    VALUES
        ('b0000000-0000-4000-8000-000000000001'::uuid, 'Chef-led gluten-free food hub'),
        ('b0000000-0000-4000-8000-000000000003'::uuid, 'The story'),
        ('b0000000-0000-4000-8000-000000000004'::uuid, 'The concept'),
        ('b0000000-0000-4000-8000-000000000005'::uuid, 'Fund the launch')
)
UPDATE content_blocks cb
SET
    content = cb.content || jsonb_build_object('eyebrow', hero_labels.eyebrow),
    updated_at = NOW()
FROM hero_labels
WHERE cb.page_id = hero_labels.page_id
  AND cb.block_type = 'hero'
  AND cb.sort_order = 0;
