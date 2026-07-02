--liquibase formatted sql

--changeset cms-team:024-brand-green-and-logo
--comment: Pickles Bodega brand color (green) and text-logo fallback

UPDATE tenants
SET settings = settings || '{"primaryColor":"#10a048","logoUrl":""}'::jsonb
WHERE id = 'a0000000-0000-4000-8000-000000000001';
