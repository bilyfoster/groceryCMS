--liquibase formatted sql

--changeset cms-team:018-therapist-photos
--comment: Point published therapists at their team headshots (slug matches /images/team/<slug>.jpg)

UPDATE therapists
SET photo_url = '/images/team/' || slug || '.jpg'
WHERE tenant_id = 'a0000000-0000-4000-8000-000000000001'
  AND published = TRUE
  AND (photo_url IS NULL OR photo_url = '');
