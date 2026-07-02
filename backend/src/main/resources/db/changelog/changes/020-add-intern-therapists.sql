--liquibase formatted sql
--changeset cms-team:020-add-intern-therapists
--comment: No-op — therapist domain removed in Pickles Bodega conversion
SELECT 1;
