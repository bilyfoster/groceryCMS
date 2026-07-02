--liquibase formatted sql

--changeset cms-team:022-pricing-page-redesign
--comment: Replace the loose text/button blocks on the Services & Pricing page with one structured, CMS-editable pricing block (same copy). Guarded to the prod page id.

-- Remove the old loose blocks (no-op where the page does not exist).
DELETE FROM content_blocks WHERE page_id = '289093cd-c4bb-4f74-b2a3-ac357802a063';

-- Add the structured pricing block, only if the page exists and no pricing block is present yet.
INSERT INTO content_blocks (page_id, block_type, sort_order, content, published)
SELECT '289093cd-c4bb-4f74-b2a3-ac357802a063', 'pricing', 0,
    '{"heading":"Services & Pricing","intro":"At Brazen Therapy, we strive to provide affordable and accessible therapy services to all of our clients. We believe that everyone should have access to quality therapy and we are committed to making that a reality. Our transparent pricing per service helps our clients to plan and budget for their sessions with ease.","note":"Prices shown are for out-of-pocket payment (no insurance).","tiers":[{"name":"Individual Therapy","price":"Intake: $235\nSession: $180"},{"name":"Family Therapy","price":"Intake: $235\nSession: $200"},{"name":"Couples Therapy","price":"Intake: $235\nSession: $200"},{"name":"Intern Therapy (supervised)","featured":true,"price":"Intake: $75\nIndividual Session: $30\nCouple/Family Session: $60"}],"insurance":["Blue Cross Blue Shield","Aetna","Cigna","United Healthcare / UMR / Optum","Tricare / Triwest"],"paymentNote":"Out-of-network: we provide a superbill for reimbursement. Payment via Stripe (Visa, Mastercard, Discover, Amex) and HSA/FSA cards accepted.","ctaText":"Book Now","ctaUrl":"https://brazentherapy.clientsecure.me/"}'::jsonb,
    TRUE
WHERE EXISTS (SELECT 1 FROM pages WHERE id = '289093cd-c4bb-4f74-b2a3-ac357802a063')
  AND NOT EXISTS (SELECT 1 FROM content_blocks WHERE page_id = '289093cd-c4bb-4f74-b2a3-ac357802a063' AND block_type = 'pricing');
