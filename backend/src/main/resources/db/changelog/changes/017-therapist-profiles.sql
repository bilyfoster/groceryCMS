--liquibase formatted sql
--changeset cms-team:017-therapist-profiles
--comment: No-op — therapist domain removed in Pickles Bodega conversion
SELECT 1;
