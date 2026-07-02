--liquibase formatted sql

--changeset cms-team:ALIGN-1-006-taxonomy
--comment: Brazen admin-managed taxonomy terms (focus areas, modalities, demographics)

CREATE TABLE taxonomy_terms (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id    UUID NOT NULL REFERENCES tenants(id),
    type         VARCHAR(32) NOT NULL,
    label        VARCHAR(255) NOT NULL,
    slug         VARCHAR(255) NOT NULL,
    description  TEXT,
    sort_order   INTEGER NOT NULL DEFAULT 0,
    active       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at   TIMESTAMPTZ,
    UNIQUE (tenant_id, type, slug)
);

CREATE INDEX idx_taxonomy_terms_tenant_type
    ON taxonomy_terms (tenant_id, type)
    WHERE deleted_at IS NULL;
