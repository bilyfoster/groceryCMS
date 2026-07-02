--liquibase formatted sql

--changeset cms-team:031-google-tracking
--comment: Carry over Brazen's Google Analytics (GA4) + Search Console / Bing site verification

UPDATE tenants
SET settings = settings || '{"analyticsId":"G-XK4SE4RCVM","googleSiteVerification":"V2bvDWqPrQxoT9SoSqpXkzM8-kPsgcvpWBqFWUxdT9Y","bingVerification":"D3AB08BBBF334678FB2DFE631BB3E6C6"}'::jsonb
WHERE id = 'a0000000-0000-4000-8000-000000000001';
