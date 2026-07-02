--liquibase formatted sql

--changeset cms-team:008-therapist
--comment: Therapist profiles and many-to-many taxonomy term relationships

CREATE TABLE therapists (
    id                     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id              UUID NOT NULL REFERENCES tenants(id),
    user_id                UUID REFERENCES users(id),
    first_name             VARCHAR(255) NOT NULL,
    last_name              VARCHAR(255) NOT NULL,
    credentials            VARCHAR(500),
    pronouns               VARCHAR(100),
    photo_url              VARCHAR(2000),
    slug                   VARCHAR(255) NOT NULL,
    bio                    TEXT,
    years_of_experience    INTEGER,
    education              TEXT,
    licensure              VARCHAR(500),
    service_delivery       VARCHAR(32) NOT NULL,
    availability_status    VARCHAR(32) NOT NULL,
    scheduling_url         VARCHAR(2000),
    booking_platform_ref   VARCHAR(100),
    meta_title             VARCHAR(500),
    meta_description       VARCHAR(1000),
    og_image_url           VARCHAR(2000),
    canonical_url          VARCHAR(2000),
    published              BOOLEAN NOT NULL DEFAULT FALSE,
    sort_order             INTEGER NOT NULL DEFAULT 0,
    created_at             TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at             TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at             TIMESTAMPTZ,
    UNIQUE (tenant_id, slug)
);

CREATE INDEX idx_therapists_tenant_published
    ON therapists (tenant_id, published)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_therapists_tenant_availability
    ON therapists (tenant_id, availability_status)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_therapists_tenant_delivery
    ON therapists (tenant_id, service_delivery)
    WHERE deleted_at IS NULL;

CREATE TABLE therapist_terms (
    therapist_id  UUID NOT NULL REFERENCES therapists(id) ON DELETE CASCADE,
    term_id       UUID NOT NULL REFERENCES taxonomy_terms(id) ON DELETE CASCADE,
    PRIMARY KEY (therapist_id, term_id)
);

CREATE INDEX idx_therapist_terms_term_id
    ON therapist_terms (term_id);
