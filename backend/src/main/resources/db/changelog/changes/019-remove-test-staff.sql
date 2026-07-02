--liquibase formatted sql

--changeset cms-team:019-remove-test-staff
--comment: No-op — no test staff records are seeded in the Pickles Bodega conversion
SELECT 1;
