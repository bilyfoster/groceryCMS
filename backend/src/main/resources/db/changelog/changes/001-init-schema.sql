--liquibase formatted sql

--changeset cms-team:ALIGN-1-001-init-schema
--comment: BrochureCMS initial schema (see ENGINEERING_PRD.md Section 4)

CREATE TABLE tenants (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    slug          VARCHAR(100) UNIQUE NOT NULL,
    name          VARCHAR(255) NOT NULL,
    domain        VARCHAR(255),
    active        BOOLEAN NOT NULL DEFAULT TRUE,
    settings      JSONB NOT NULL DEFAULT '{}',
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at    TIMESTAMPTZ
);

CREATE TABLE users (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL REFERENCES tenants(id),
    email           VARCHAR(320) NOT NULL,
    password_hash   VARCHAR(255),
    display_name    VARCHAR(255),
    role            VARCHAR(50) NOT NULL DEFAULT 'viewer',
    email_verified  BOOLEAN NOT NULL DEFAULT FALSE,
    active          BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMPTZ,
    UNIQUE (tenant_id, email)
);

CREATE TABLE auth_tokens (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID NOT NULL REFERENCES users(id),
    token_hash  VARCHAR(255) NOT NULL UNIQUE,
    purpose     VARCHAR(50) NOT NULL,
    expires_at  TIMESTAMPTZ NOT NULL,
    used_at     TIMESTAMPTZ,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE pages (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id        UUID NOT NULL REFERENCES tenants(id),
    slug             VARCHAR(255) NOT NULL,
    title            VARCHAR(500) NOT NULL,
    page_type        VARCHAR(100) NOT NULL,
    layout           VARCHAR(100) NOT NULL DEFAULT 'default',
    nav_order        INTEGER,
    published        BOOLEAN NOT NULL DEFAULT FALSE,
    meta_title       VARCHAR(500),
    meta_description VARCHAR(1000),
    og_image_url     VARCHAR(2000),
    config           JSONB NOT NULL DEFAULT '{}',
    created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at       TIMESTAMPTZ,
    UNIQUE (tenant_id, slug)
);

CREATE TABLE content_blocks (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    page_id     UUID NOT NULL REFERENCES pages(id),
    block_type  VARCHAR(100) NOT NULL,
    sort_order  INTEGER NOT NULL DEFAULT 0,
    content     JSONB NOT NULL DEFAULT '{}',
    published   BOOLEAN NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE blog_posts (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id        UUID NOT NULL REFERENCES tenants(id),
    page_id          UUID NOT NULL REFERENCES pages(id),
    author_id        UUID REFERENCES users(id),
    slug             VARCHAR(500) NOT NULL,
    title            VARCHAR(1000) NOT NULL,
    excerpt          TEXT,
    body             TEXT NOT NULL,
    featured_image   VARCHAR(2000),
    published        BOOLEAN NOT NULL DEFAULT FALSE,
    published_at     TIMESTAMPTZ,
    tags             TEXT[] DEFAULT '{}',
    meta_title       VARCHAR(500),
    meta_description VARCHAR(1000),
    created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at       TIMESTAMPTZ,
    UNIQUE (tenant_id, slug)
);

CREATE TABLE comments (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id    UUID NOT NULL REFERENCES tenants(id),
    post_id      UUID NOT NULL REFERENCES blog_posts(id),
    parent_id    UUID REFERENCES comments(id),
    author_name  VARCHAR(255),
    author_email VARCHAR(320),
    body         TEXT NOT NULL,
    approved     BOOLEAN NOT NULL DEFAULT FALSE,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at   TIMESTAMPTZ
);

CREATE TABLE faq_items (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id   UUID NOT NULL REFERENCES tenants(id),
    page_id     UUID NOT NULL REFERENCES pages(id),
    question    VARCHAR(2000) NOT NULL,
    answer      TEXT NOT NULL,
    sort_order  INTEGER NOT NULL DEFAULT 0,
    published   BOOLEAN NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at  TIMESTAMPTZ
);

CREATE TABLE staff_members (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id    UUID NOT NULL REFERENCES tenants(id),
    page_id      UUID NOT NULL REFERENCES pages(id),
    name         VARCHAR(500) NOT NULL,
    title        VARCHAR(500),
    bio          TEXT,
    photo_url    VARCHAR(2000),
    email        VARCHAR(320),
    sort_order   INTEGER NOT NULL DEFAULT 0,
    published    BOOLEAN NOT NULL DEFAULT TRUE,
    social_links JSONB NOT NULL DEFAULT '{}',
    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at   TIMESTAMPTZ
);

CREATE TABLE gallery_images (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id   UUID NOT NULL REFERENCES tenants(id),
    page_id     UUID NOT NULL REFERENCES pages(id),
    url         VARCHAR(2000) NOT NULL,
    alt_text    VARCHAR(1000),
    caption     VARCHAR(2000),
    sort_order  INTEGER NOT NULL DEFAULT 0,
    published   BOOLEAN NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at   TIMESTAMPTZ
);

CREATE TABLE contact_submissions (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id   UUID NOT NULL REFERENCES tenants(id),
    name        VARCHAR(500) NOT NULL,
    email       VARCHAR(320) NOT NULL,
    phone       VARCHAR(50),
    subject     VARCHAR(1000),
    message     TEXT NOT NULL,
    ip_address  INET,
    read_at     TIMESTAMPTZ,
    replied_at  TIMESTAMPTZ,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE media_files (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id    UUID NOT NULL REFERENCES tenants(id),
    uploader_id  UUID REFERENCES users(id),
    filename     VARCHAR(500) NOT NULL,
    storage_path VARCHAR(2000) NOT NULL,
    mime_type    VARCHAR(255) NOT NULL,
    size_bytes   BIGINT NOT NULL,
    alt_text     VARCHAR(1000),
    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at   TIMESTAMPTZ
);
