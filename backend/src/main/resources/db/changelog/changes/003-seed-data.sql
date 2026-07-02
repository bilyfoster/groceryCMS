--liquibase formatted sql

--changeset cms-team:ALIGN-1-003-seed-data
--comment: Seed the Pickles Bodega grocery tenant and home page

INSERT INTO tenants (id, slug, name, domain, active, settings)
VALUES (
    'a0000000-0000-4000-8000-000000000001',
    'demo',
    'Pickles Bodega',
    'localhost',
    TRUE,
    '{"primaryColor":"#10a048","secondaryColor":"#64748b","headingFont":"Montserrat","bodyFont":"Barlow","borderRadius":"0.5rem"}'::jsonb
);

INSERT INTO pages (id, tenant_id, slug, title, page_type, layout, nav_order, published, meta_title, meta_description)
VALUES
    ('b0000000-0000-4000-8000-000000000001', 'a0000000-0000-4000-8000-000000000001', 'home', 'Pickles Bodega', 'home', 'hero-centered', 0, TRUE, 'Pickles Bodega', 'Gluten-free and allergy-friendly groceries in Flagstaff, Arizona.'),
    ('b0000000-0000-4000-8000-000000000002', 'a0000000-0000-4000-8000-000000000001', 'contact', 'Contact', 'contact', 'centered', 10, TRUE, 'Contact Us', 'Get in touch with Pickles Bodega.');

INSERT INTO content_blocks (page_id, block_type, sort_order, content)
VALUES (
    'b0000000-0000-4000-8000-000000000001',
    'hero',
    0,
    '{"heading":"Pickles Bodega","subheading":"Gluten-free and allergy-friendly groceries in Flagstaff, Arizona.","buttonText":"Shop now","buttonUrl":"/products","backgroundImage":"/images/shoppers-buying-fresh-fruit-and-vegetables-in-sust-2026-01-05-06-35-46-utc.jpg","overlay":true}'::jsonb
);

-- Admin user: password is "password" (BCrypt)
INSERT INTO users (id, tenant_id, email, password_hash, display_name, role, email_verified, active)
VALUES (
    'c0000000-0000-4000-8000-000000000001',
    'a0000000-0000-4000-8000-000000000001',
    'admin@demo.local',
    '$2b$10$afHglzRETFy6CmBmlyUzVuMIF8bIS2Qrq5NPjuHY.Dx/GSLR5MUOm',
    'Demo Admin',
    'admin',
    TRUE,
    TRUE
);
