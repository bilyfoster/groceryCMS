--liquibase formatted sql

--changeset cms-team:026-story-mission-pages
--comment: Page parity — add Our Story and Mission & Values pages; add them + Blog to the header nav

-- Our Story page
INSERT INTO pages (id, tenant_id, slug, title, page_type, layout, nav_order, published, meta_title, meta_description)
VALUES ('b0000000-0000-4000-8000-000000000030','a0000000-0000-4000-8000-000000000001','our-story','Our Story','custom','contained',0,TRUE,'Our Story | Brazen Therapy','How Brazen Therapy began and what we stand for.')
ON CONFLICT (tenant_id, slug) DO NOTHING;
INSERT INTO content_blocks (page_id, block_type, sort_order, content, published)
SELECT 'b0000000-0000-4000-8000-000000000030','hero',0,'{"heading": "Our Story", "subheading": "We believe connection is strongest when we embrace difference. At Brazen, we meet you where you are, affirm who you are, and walk beside you as you create change — on your terms.", "backgroundImage": "/images/consultation.jpg", "objectPosition": "center 15%", "overlay": true, "buttonText": "Book a free consult", "buttonUrl": "https://brazentherapy.clientsecure.me/"}'::jsonb,TRUE
WHERE EXISTS (SELECT 1 FROM pages WHERE id='b0000000-0000-4000-8000-000000000030')
  AND NOT EXISTS (SELECT 1 FROM content_blocks WHERE page_id='b0000000-0000-4000-8000-000000000030' AND sort_order=0);
INSERT INTO content_blocks (page_id, block_type, sort_order, content, published)
SELECT 'b0000000-0000-4000-8000-000000000030','text',1,'{"body": "<p>Brazen Therapy was founded in 2021 by Clarke Scott, LPC, with a vision of creating a radically inclusive and ethically grounded space for healing.</p><p>Brazen&rsquo;s team of compassionate and skilled therapists are committed to showing up not just as mental health professionals, but as advocates and safe harbors for those navigating life&rsquo;s challenges. We use evidence-based practices within a framework that centers client autonomy, prioritizes emotional safety, and honors each person&rsquo;s lived experience.</p>", "alignment": "left"}'::jsonb,TRUE
WHERE EXISTS (SELECT 1 FROM pages WHERE id='b0000000-0000-4000-8000-000000000030')
  AND NOT EXISTS (SELECT 1 FROM content_blocks WHERE page_id='b0000000-0000-4000-8000-000000000030' AND sort_order=1);
INSERT INTO content_blocks (page_id, block_type, sort_order, content, published)
SELECT 'b0000000-0000-4000-8000-000000000030','cta',2,'{"heading": "Meet the people behind Brazen", "body": "Our therapists bring warmth, expertise, and lived experience to every session.", "primaryButton": {"text": "Meet our therapists", "url": "/our-team"}, "secondaryButton": {"text": "Find your match", "url": "/match"}}'::jsonb,TRUE
WHERE EXISTS (SELECT 1 FROM pages WHERE id='b0000000-0000-4000-8000-000000000030')
  AND NOT EXISTS (SELECT 1 FROM content_blocks WHERE page_id='b0000000-0000-4000-8000-000000000030' AND sort_order=2);

-- Mission & Values page
INSERT INTO pages (id, tenant_id, slug, title, page_type, layout, nav_order, published, meta_title, meta_description)
VALUES ('b0000000-0000-4000-8000-000000000031','a0000000-0000-4000-8000-000000000001','mission','Mission & Values','custom','contained',0,TRUE,'Mission & Values | Brazen Therapy','Our mission and the values that guide our care.')
ON CONFLICT (tenant_id, slug) DO NOTHING;
INSERT INTO content_blocks (page_id, block_type, sort_order, content, published)
SELECT 'b0000000-0000-4000-8000-000000000031','hero',0,'{"heading": "Mission & Values", "subheading": "Brazen Therapy exists to provide accessible, affirming, and trauma-informed mental health care that honors people&rsquo;s full identities, lived experiences, and relationships.", "backgroundImage": "/images/community.jpg", "overlay": true, "buttonText": "Book an appointment", "buttonUrl": "https://brazentherapy.clientsecure.me/"}'::jsonb,TRUE
WHERE EXISTS (SELECT 1 FROM pages WHERE id='b0000000-0000-4000-8000-000000000031')
  AND NOT EXISTS (SELECT 1 FROM content_blocks WHERE page_id='b0000000-0000-4000-8000-000000000031' AND sort_order=0);
INSERT INTO content_blocks (page_id, block_type, sort_order, content, published)
SELECT 'b0000000-0000-4000-8000-000000000031','text',1,'{"body": "<p>We are committed to creating an environment where clients experience safety, feel understood amid complexity, and gain empowerment through clarity and connection.</p><h3>Be Bold Without Shame</h3><p>We support you in showing up authentically and speaking truthfully. We never stigmatize identity, emotion, or difference.</p><h3>Affirmation &amp; Inclusion</h3><p>We welcome diverse communities and intentionally prioritize marginalized populations, including LGBTQIA+ and neurodivergent people. You should never have to defend or diminish who you are to receive care.</p><h3>Authenticity &amp; Directness</h3><p>Genuine dialogue, openness, and candid communication are essential to meaningful change.</p><h3>Accessibility, Connection &amp; Community Impact</h3><p>Care should be accessible and sustainable. We widen access through trainee clinicians, sliding-scale fees, insurance participation, and community collaboration.</p><h3>Trauma-Informed &amp; Ethical Care</h3><p>Clinical excellence, oversight, and professional standards guide our work. Confidentiality, transparency, informed consent, and moral responsibility are foundational.</p>", "alignment": "left"}'::jsonb,TRUE
WHERE EXISTS (SELECT 1 FROM pages WHERE id='b0000000-0000-4000-8000-000000000031')
  AND NOT EXISTS (SELECT 1 FROM content_blocks WHERE page_id='b0000000-0000-4000-8000-000000000031' AND sort_order=1);

-- Header nav: add the pages and order them sensibly
INSERT INTO menu_items (id, menu_id, parent_id, label, url, page_id, target, sort_order, created_at, updated_at)
SELECT 'd0000000-0000-4000-8000-000000000016','d0000000-0000-4000-8000-000000000001',NULL,'Our Story',NULL,'b0000000-0000-4000-8000-000000000030','_self',1,now(),now()
WHERE EXISTS (SELECT 1 FROM menus WHERE id='d0000000-0000-4000-8000-000000000001')
  AND EXISTS (SELECT 1 FROM pages WHERE id='b0000000-0000-4000-8000-000000000030')
  AND NOT EXISTS (SELECT 1 FROM menu_items WHERE id='d0000000-0000-4000-8000-000000000016');
INSERT INTO menu_items (id, menu_id, parent_id, label, url, page_id, target, sort_order, created_at, updated_at)
SELECT 'd0000000-0000-4000-8000-000000000017','d0000000-0000-4000-8000-000000000001',NULL,'Mission & Values',NULL,'b0000000-0000-4000-8000-000000000031','_self',2,now(),now()
WHERE EXISTS (SELECT 1 FROM menus WHERE id='d0000000-0000-4000-8000-000000000001')
  AND EXISTS (SELECT 1 FROM pages WHERE id='b0000000-0000-4000-8000-000000000031')
  AND NOT EXISTS (SELECT 1 FROM menu_items WHERE id='d0000000-0000-4000-8000-000000000017');
INSERT INTO menu_items (id, menu_id, parent_id, label, url, page_id, target, sort_order, created_at, updated_at)
SELECT 'd0000000-0000-4000-8000-000000000018','d0000000-0000-4000-8000-000000000001',NULL,'Blog',NULL,'f16b79dc-50f8-4022-8de8-66bd15b8a529','_self',5,now(),now()
WHERE EXISTS (SELECT 1 FROM menus WHERE id='d0000000-0000-4000-8000-000000000001')
  AND EXISTS (SELECT 1 FROM pages WHERE id='f16b79dc-50f8-4022-8de8-66bd15b8a529')
  AND NOT EXISTS (SELECT 1 FROM menu_items WHERE id='d0000000-0000-4000-8000-000000000018');
UPDATE menu_items SET sort_order=0 WHERE menu_id='d0000000-0000-4000-8000-000000000001' AND label='Home';
UPDATE menu_items SET sort_order=3 WHERE menu_id='d0000000-0000-4000-8000-000000000001' AND label='Pricing';
UPDATE menu_items SET sort_order=4 WHERE menu_id='d0000000-0000-4000-8000-000000000001' AND label='Team';
UPDATE menu_items SET sort_order=6 WHERE menu_id='d0000000-0000-4000-8000-000000000001' AND label='Contact';
UPDATE menu_items SET sort_order=7 WHERE menu_id='d0000000-0000-4000-8000-000000000001' AND label='Find a Therapist';
UPDATE menu_items SET sort_order=8 WHERE menu_id='d0000000-0000-4000-8000-000000000001' AND label='Match Me';
