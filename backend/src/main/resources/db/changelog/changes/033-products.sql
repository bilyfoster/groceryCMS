--liquibase formatted sql

--changeset cms-team:033-products
--comment: Product catalog for the grocery store tenant

CREATE TABLE products (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id        UUID NOT NULL REFERENCES tenants(id),
    name             VARCHAR(255) NOT NULL,
    slug             VARCHAR(255) NOT NULL,
    brand            VARCHAR(255),
    description      TEXT,
    price            NUMERIC(10, 2),
    unit             VARCHAR(50),
    photo_url        VARCHAR(2000),
    stock_status     VARCHAR(32) NOT NULL,
    store_section    VARCHAR(32) NOT NULL,
    meta_title       VARCHAR(500),
    meta_description VARCHAR(1000),
    og_image_url     VARCHAR(2000),
    canonical_url    VARCHAR(2000),
    published        BOOLEAN NOT NULL DEFAULT FALSE,
    sort_order       INTEGER NOT NULL DEFAULT 0,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at       TIMESTAMPTZ,
    UNIQUE (tenant_id, slug)
);

CREATE INDEX idx_products_tenant_published
    ON products (tenant_id, published)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_products_tenant_sort
    ON products (tenant_id, sort_order, name)
    WHERE deleted_at IS NULL;

CREATE TABLE product_terms (
    product_id   UUID NOT NULL REFERENCES products(id),
    term_id      UUID NOT NULL REFERENCES taxonomy_terms(id),
    PRIMARY KEY (product_id, term_id)
);

CREATE INDEX idx_product_terms_term
    ON product_terms (term_id);
