--liquibase formatted sql

--changeset cms-team:ALIGN-1-003-seed-data
--comment: Demo tenant for local development (slug: demo, domain: localhost)

INSERT INTO tenants (id, slug, name, domain, active, settings)
VALUES (
    'a0000000-0000-4000-8000-000000000001',
    'demo',
    'Demo Client',
    'localhost',
    TRUE,
    '{"primaryColor":"#2563eb","secondaryColor":"#64748b","headingFont":"Inter","bodyFont":"Inter","borderRadius":"0.5rem"}'::jsonb
);

INSERT INTO pages (id, tenant_id, slug, title, page_type, layout, nav_order, published, meta_title, meta_description)
VALUES
    ('b0000000-0000-4000-8000-000000000001', 'a0000000-0000-4000-8000-000000000001', 'home', 'Home', 'home', 'hero-centered', 0, TRUE, 'Welcome', 'Demo brochure site'),
    ('b0000000-0000-4000-8000-000000000002', 'a0000000-0000-4000-8000-000000000001', 'contact', 'Contact', 'contact', 'centered', 10, TRUE, 'Contact Us', 'Get in touch');

INSERT INTO content_blocks (page_id, block_type, sort_order, content)
VALUES (
    'b0000000-0000-4000-8000-000000000001',
    'hero',
    0,
    '{"heading":"Welcome to Demo CMS","subheading":"Build beautiful brochure sites","buttonText":"Contact Us","buttonUrl":"/contact"}'::jsonb
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
