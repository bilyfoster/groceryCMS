--liquibase formatted sql

--changeset cms-team:014-home-hero-focal
--comment: Bias the home hero photo focal point upward so faces are visible (not just bodies)

UPDATE content_blocks
SET content = content || '{"objectPosition":"center 20%"}'::jsonb
WHERE block_type = 'hero'
  AND page_id IN (
    SELECT id FROM pages
    WHERE slug = 'home' AND tenant_id = 'a0000000-0000-4000-8000-000000000001'
  );
