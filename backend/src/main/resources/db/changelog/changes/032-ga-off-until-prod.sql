--liquibase formatted sql

--changeset cms-team:032-ga-off-until-prod
--comment: No-op — analytics configuration is not seeded in the Pickles Bodega conversion
SELECT 1;
