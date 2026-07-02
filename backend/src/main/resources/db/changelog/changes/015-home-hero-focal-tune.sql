--liquibase formatted sql

--changeset cms-team:015-home-hero-focal-tune
--comment: Tune home hero focal point to center 10% (both faces fully in frame)

UPDATE content_blocks
SET content = content || '{"objectPosition":"center 10%"}'::jsonb
WHERE block_type = 'hero'
  AND page_id IN (
    SELECT id FROM pages
    WHERE slug = 'home' AND tenant_id = 'a0000000-0000-4000-8000-000000000001'
  );
