--liquibase formatted sql

--changeset cms-team:024-brand-green-and-logo
--comment: Set the Brazen brand green as the primary color and wire the logo into the header

UPDATE tenants
SET settings = settings || '{"primaryColor":"#10a048","logoUrl":"/images/brazen-logo.png"}'::jsonb
WHERE id = 'a0000000-0000-4000-8000-000000000001';
