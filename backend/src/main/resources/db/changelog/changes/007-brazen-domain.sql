--liquibase formatted sql

--changeset cms-team:ALIGN-1-007-brazen-domain
--comment: Point the seeded demo tenant at the public Pickles Bodega domain

UPDATE tenants
SET domain = 'pickles.1lpro.com'
WHERE slug = 'demo';
