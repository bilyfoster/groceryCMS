--liquibase formatted sql

--changeset cms-team:023-pricing-tier-photos
--comment: Add Brazen service photos to the pricing tier cards for visual life

UPDATE content_blocks
SET content = '{"heading":"Services & Pricing","intro":"At Brazen Therapy, we strive to provide affordable and accessible therapy services to all of our clients. We believe that everyone should have access to quality therapy and we are committed to making that a reality. Our transparent pricing per service helps our clients to plan and budget for their sessions with ease.","note":"Prices shown are for out-of-pocket payment (no insurance).","tiers":[{"name":"Individual Therapy","image":"/images/service-individual.jpg","imageAlt":"A person in a one-on-one therapy session","price":"Intake: $235\nSession: $180"},{"name":"Family Therapy","image":"/images/service-family.jpg","imageAlt":"A parent and child embracing","price":"Intake: $235\nSession: $200"},{"name":"Couples Therapy","image":"/images/service-couples.jpg","imageAlt":"A couple sitting together, smiling","price":"Intake: $235\nSession: $200"},{"name":"Intern Therapy (supervised)","featured":true,"image":"/images/service-intern.jpg","imageAlt":"A clinician smiling in a bright office","price":"Intake: $75\nIndividual Session: $30\nCouple/Family Session: $60"}],"insurance":["Blue Cross Blue Shield","Aetna","Cigna","United Healthcare / UMR / Optum","Tricare / Triwest"],"paymentNote":"Out-of-network: we provide a superbill for reimbursement. Payment via Stripe (Visa, Mastercard, Discover, Amex) and HSA/FSA cards accepted.","ctaText":"Book Now","ctaUrl":"https://brazentherapy.clientsecure.me/"}'::jsonb
WHERE page_id = '289093cd-c4bb-4f74-b2a3-ac357802a063'
  AND block_type = 'pricing';
