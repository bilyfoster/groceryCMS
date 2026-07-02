--liquibase formatted sql

--changeset cms-team:012-staff-therapist-link
--comment: Schema link between staff and therapists (no auto-created therapist profiles in the grocery domain)

ALTER TABLE staff_members ADD COLUMN IF NOT EXISTS is_therapist BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE therapists ADD COLUMN IF NOT EXISTS staff_member_id UUID REFERENCES staff_members(id);
CREATE INDEX IF NOT EXISTS idx_therapists_staff_member_id ON therapists(staff_member_id);

-- Therapist profiles are no longer seeded in the Pickles Bodega conversion.
SELECT 1;
