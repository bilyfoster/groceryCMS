--liquibase formatted sql

--changeset cms-team:032-ga-off-until-prod
--comment: Disable GA on the demo (keep the ID stashed for production). To re-enable, move analyticsIdProduction back to analyticsId.

UPDATE tenants
SET settings = (settings - 'analyticsId')
    || jsonb_build_object('analyticsIdProduction', settings->>'analyticsId')
WHERE id = 'a0000000-0000-4000-8000-000000000001'
  AND settings->>'analyticsId' IS NOT NULL;
