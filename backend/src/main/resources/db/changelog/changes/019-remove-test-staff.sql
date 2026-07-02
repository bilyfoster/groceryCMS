--liquibase formatted sql

--changeset cms-team:019-remove-test-staff
--comment: Soft-delete the leftover "Test Person Updated" dev record so it stops showing on the team page

UPDATE staff_members
SET deleted_at = now()
WHERE tenant_id = 'a0000000-0000-4000-8000-000000000001'
  AND name = 'Test Person Updated, LPC'
  AND deleted_at IS NULL;
