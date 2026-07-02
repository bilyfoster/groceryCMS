--liquibase formatted sql
--changeset cms-team:018-therapist-photos
--comment: No-op — therapist domain removed in Pickles Bodega conversion
SELECT 1;
