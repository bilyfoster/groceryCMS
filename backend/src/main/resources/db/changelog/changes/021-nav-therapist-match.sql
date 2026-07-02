--liquibase formatted sql

--changeset cms-team:021-nav-therapist-match
--comment: Add "Find a Therapist" (/therapists) and "Match Me" (/match) to the header nav

INSERT INTO menu_items (id, menu_id, parent_id, label, url, page_id, target, sort_order, created_at, updated_at)
SELECT 'd0000000-0000-4000-8000-000000000014', 'd0000000-0000-4000-8000-000000000001', NULL,
       'Find a Therapist', '/therapists', NULL, '_self', 4, now(), now()
WHERE EXISTS (SELECT 1 FROM menus WHERE id = 'd0000000-0000-4000-8000-000000000001')
  AND NOT EXISTS (SELECT 1 FROM menu_items WHERE id = 'd0000000-0000-4000-8000-000000000014');

INSERT INTO menu_items (id, menu_id, parent_id, label, url, page_id, target, sort_order, created_at, updated_at)
SELECT 'd0000000-0000-4000-8000-000000000015', 'd0000000-0000-4000-8000-000000000001', NULL,
       'Match Me', '/match', NULL, '_self', 5, now(), now()
WHERE EXISTS (SELECT 1 FROM menus WHERE id = 'd0000000-0000-4000-8000-000000000001')
  AND NOT EXISTS (SELECT 1 FROM menu_items WHERE id = 'd0000000-0000-4000-8000-000000000015');
