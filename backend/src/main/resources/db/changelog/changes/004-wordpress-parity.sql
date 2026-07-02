--liquibase formatted sql

--changeset cms-team:ALIGN-1-004-wordpress-parity
--comment: WordPress parity schema (ENGINEERING_PRD.md Phase 8–9)

ALTER TABLE pages
    ADD COLUMN IF NOT EXISTS is_front_page BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS is_posts_page BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS page_password VARCHAR(255);

ALTER TABLE blog_posts
    ADD COLUMN IF NOT EXISTS is_sticky BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS allow_comments BOOLEAN NOT NULL DEFAULT TRUE;

CREATE TABLE IF NOT EXISTS categories (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id   UUID NOT NULL REFERENCES tenants(id),
    parent_id   UUID REFERENCES categories(id),
    name        VARCHAR(500) NOT NULL,
    slug        VARCHAR(500) NOT NULL,
    description TEXT,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at  TIMESTAMPTZ,
    UNIQUE (tenant_id, slug)
);

CREATE TABLE IF NOT EXISTS post_categories (
    post_id     UUID NOT NULL REFERENCES blog_posts(id) ON DELETE CASCADE,
    category_id UUID NOT NULL REFERENCES categories(id) ON DELETE CASCADE,
    PRIMARY KEY (post_id, category_id)
);

CREATE TABLE IF NOT EXISTS menus (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id   UUID NOT NULL REFERENCES tenants(id),
    name        VARCHAR(255) NOT NULL,
    location    VARCHAR(100) NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (tenant_id, location)
);

CREATE TABLE IF NOT EXISTS menu_items (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    menu_id     UUID NOT NULL REFERENCES menus(id) ON DELETE CASCADE,
    parent_id   UUID REFERENCES menu_items(id),
    label       VARCHAR(500) NOT NULL,
    url         VARCHAR(2000),
    page_id     UUID REFERENCES pages(id),
    target      VARCHAR(20) NOT NULL DEFAULT '_self',
    sort_order  INTEGER NOT NULL DEFAULT 0,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS revisions (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id   UUID NOT NULL REFERENCES tenants(id),
    entity_type VARCHAR(50) NOT NULL,
    entity_id   UUID NOT NULL,
    author_id   UUID REFERENCES users(id),
    snapshot    JSONB NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS block_patterns (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID REFERENCES tenants(id),
    name            VARCHAR(500) NOT NULL,
    category        VARCHAR(100) NOT NULL,
    thumbnail_url   VARCHAR(2000),
    blocks          JSONB NOT NULL,
    is_system       BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS idx_pages_front ON pages(tenant_id, is_front_page) WHERE is_front_page = TRUE;
CREATE INDEX IF NOT EXISTS idx_pages_posts ON pages(tenant_id, is_posts_page) WHERE is_posts_page = TRUE;
CREATE INDEX IF NOT EXISTS idx_blog_posts_sticky ON blog_posts(tenant_id, is_sticky DESC, published_at DESC) WHERE published = TRUE;
CREATE INDEX IF NOT EXISTS idx_blog_posts_scheduled ON blog_posts(published_at) WHERE published = FALSE AND published_at IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_categories_tenant ON categories(tenant_id) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_menu_items_menu ON menu_items(menu_id, sort_order);
CREATE INDEX IF NOT EXISTS idx_revisions_entity ON revisions(entity_type, entity_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_block_patterns_tenant ON block_patterns(tenant_id) WHERE deleted_at IS NULL;

-- Demo tenant: mark home as front page
UPDATE pages
SET is_front_page = TRUE
WHERE tenant_id = 'a0000000-0000-4000-8000-000000000001' AND slug = 'home';

-- Default 404 page
INSERT INTO pages (id, tenant_id, slug, title, page_type, layout, published, is_front_page, meta_title, meta_description)
VALUES (
    'b0000000-0000-4000-8000-000000000099',
    'a0000000-0000-4000-8000-000000000001',
    '404',
    'Page not found',
    '404',
    'default',
    TRUE,
    FALSE,
    'Page not found',
    'The page you requested could not be found.'
) ON CONFLICT (tenant_id, slug) DO NOTHING;

INSERT INTO content_blocks (page_id, block_type, sort_order, content)
SELECT 'b0000000-0000-4000-8000-000000000099', 'text', 0,
    '{"heading":"404","body":"<p>Sorry, that page does not exist.</p>","alignment":"center"}'::jsonb
WHERE NOT EXISTS (SELECT 1 FROM content_blocks WHERE page_id = 'b0000000-0000-4000-8000-000000000099');

INSERT INTO menus (id, tenant_id, name, location)
VALUES (
    'd0000000-0000-4000-8000-000000000001',
    'a0000000-0000-4000-8000-000000000001',
    'Header Nav',
    'header'
) ON CONFLICT (tenant_id, location) DO NOTHING;

INSERT INTO menu_items (id, menu_id, label, page_id, sort_order)
VALUES
    ('d0000000-0000-4000-8000-000000000011', 'd0000000-0000-4000-8000-000000000001', 'Home', 'b0000000-0000-4000-8000-000000000001', 0),
    ('d0000000-0000-4000-8000-000000000012', 'd0000000-0000-4000-8000-000000000001', 'Contact', 'b0000000-0000-4000-8000-000000000002', 10);
