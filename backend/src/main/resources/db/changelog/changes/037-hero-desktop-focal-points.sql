--liquibase formatted sql

--changeset cms-team:037-hero-desktop-focal-points
--comment: Tune fundraiser hero desktop focal points so people are fully framed

UPDATE content_blocks
SET
    content = content || '{"desktopObjectPosition":"center 24%"}'::jsonb,
    updated_at = NOW()
WHERE block_type = 'hero'
  AND content->>'backgroundImage' = '/images/shop-seller-presents-products-2026-03-19-01-47-13-utc.jpg';

UPDATE content_blocks
SET
    content = content || '{"desktopObjectPosition":"center 24%"}'::jsonb,
    updated_at = NOW()
WHERE block_type = 'hero'
  AND content->>'backgroundImage' = '/images/smiling-woman-buying-organic-products-in-zero-wast-2026-01-08-22-29-57-utc.jpg';

UPDATE content_blocks
SET
    content = content || '{"desktopObjectPosition":"center 22%"}'::jsonb,
    updated_at = NOW()
WHERE block_type = 'hero'
  AND content->>'backgroundImage' = '/images/shoppers-buying-fresh-fruit-and-vegetables-in-sust-2026-01-05-06-35-46-utc.jpg';

UPDATE content_blocks
SET
    content = content || '{"desktopObjectPosition":"center 24%"}'::jsonb,
    updated_at = NOW()
WHERE block_type = 'hero'
  AND content->>'backgroundImage' = '/images/black-couple-on-grocery-shopping-posing-with-shop-2026-01-08-23-28-31-utc.jpg';
