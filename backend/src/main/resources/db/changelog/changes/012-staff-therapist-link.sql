-- Link staff members to therapists and backfill existing licensed clinicians.

ALTER TABLE staff_members ADD COLUMN IF NOT EXISTS is_therapist BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE therapists ADD COLUMN IF NOT EXISTS staff_member_id UUID REFERENCES staff_members(id);
CREATE INDEX IF NOT EXISTS idx_therapists_staff_member_id ON therapists(staff_member_id);

-- Auto-create therapist profiles for staff who appear to be licensed clinicians.
DO $$
DECLARE
    rec record;
    v_display_name text;
    v_first_name text;
    v_last_name text;
    v_credentials text;
    v_slug text;
    v_counter int;
BEGIN
    FOR rec IN
        SELECT s.id, s.tenant_id, s.name, s.title, s.bio, s.photo_url, s.sort_order
        FROM staff_members s
        WHERE s.deleted_at IS NULL
          AND NOT s.is_therapist
          AND (
              lower(s.name) LIKE ANY(ARRAY['%lcs%', '%lpc%', '%lac%', '%lms%', '%lamft%', '%liac%'])
              OR lower(s.title) LIKE ANY(ARRAY['%lcs%', '%lpc%', '%lac%', '%lms%', '%lamft%', '%liac%'])
          )
          AND lower(coalesce(s.title, '')) NOT LIKE '%intern%'
          AND NOT EXISTS (
              SELECT 1 FROM therapists t WHERE t.staff_member_id = s.id AND t.deleted_at IS NULL
          )
    LOOP
        v_display_name := trim(split_part(rec.name, ',', 1));
        v_first_name := split_part(v_display_name, ' ', 1);
        v_last_name := trim(substring(v_display_name from length(v_first_name) + 1));
        IF v_last_name = '' THEN
            v_last_name := v_first_name;
        END IF;
        v_credentials := CASE
            WHEN position(',' in rec.name) > 0 THEN trim(substring(rec.name from position(',' in rec.name) + 1))
            ELSE rec.title
        END;
        v_slug := lower(regexp_replace(v_display_name, '[^a-zA-Z0-9]+', '-', 'g'));
        v_slug := regexp_replace(v_slug, '^-+|-+$', '', 'g');

        v_counter := 2;
        WHILE EXISTS (
            SELECT 1 FROM therapists t
            WHERE t.tenant_id = rec.tenant_id AND t.slug = v_slug AND t.deleted_at IS NULL
        ) LOOP
            v_slug := regexp_replace(v_slug, '-[a-f0-9]{6}$', '', 'g') || '-' || substr(md5(rec.id::text || v_counter::text), 1, 6);
            v_counter := v_counter + 1;
        END LOOP;

        INSERT INTO therapists (
            id, tenant_id, user_id, staff_member_id, first_name, last_name, credentials,
            photo_url, slug, bio, service_delivery, availability_status, published, sort_order,
            created_at, updated_at, deleted_at
        ) VALUES (
            gen_random_uuid(), rec.tenant_id, NULL, rec.id, v_first_name, v_last_name, v_credentials,
            rec.photo_url, v_slug, rec.bio, 'HYBRID', 'ACCEPTING', false, rec.sort_order,
            NOW(), NOW(), NULL
        );

        UPDATE staff_members SET is_therapist = true WHERE id = rec.id;
    END LOOP;
END $$;
