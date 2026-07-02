--liquibase formatted sql

--changeset cms-team:030-mission-value-icons
--comment: Add icons to the Mission value cards for visual polish

UPDATE content_blocks SET content = content || '{"icon":"sparkles"}'::jsonb
  WHERE page_id='b0000000-0000-4000-8000-000000000031' AND block_type='text' AND sort_order=1;
UPDATE content_blocks SET content = content || '{"icon":"users"}'::jsonb
  WHERE page_id='b0000000-0000-4000-8000-000000000031' AND block_type='text' AND sort_order=2;
UPDATE content_blocks SET content = content || '{"icon":"message"}'::jsonb
  WHERE page_id='b0000000-0000-4000-8000-000000000031' AND block_type='text' AND sort_order=3;
UPDATE content_blocks SET content = content || '{"icon":"hand-heart"}'::jsonb
  WHERE page_id='b0000000-0000-4000-8000-000000000031' AND block_type='text' AND sort_order=4;
UPDATE content_blocks SET content = content || '{"icon":"shield"}'::jsonb
  WHERE page_id='b0000000-0000-4000-8000-000000000031' AND block_type='text' AND sort_order=5;
