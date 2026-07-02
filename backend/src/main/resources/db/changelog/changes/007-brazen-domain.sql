--liquibase formatted sql

--changeset cms-team:ALIGN-1-007-brazen-domain
--comment: Point the seeded demo tenant at the public test domain

UPDATE tenants
SET domain = 'brazen.1lpro.com'
WHERE slug = 'demo';
