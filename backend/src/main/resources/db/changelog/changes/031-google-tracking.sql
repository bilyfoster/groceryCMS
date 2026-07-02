--liquibase formatted sql

--changeset cms-team:031-google-tracking
--comment: No-op — analytics IDs are not seeded in the Pickles Bodega conversion
SELECT 1;
