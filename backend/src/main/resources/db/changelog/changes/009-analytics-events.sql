--liquibase formatted sql

--changeset cms-team:009-analytics-events
--comment: Lightweight analytics event capture for public visitor actions

CREATE TABLE analytics_events (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id   UUID NOT NULL REFERENCES tenants(id),
    event_type  VARCHAR(100) NOT NULL,
    payload     JSONB,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at  TIMESTAMPTZ
);

CREATE INDEX idx_analytics_events_tenant_type_created
    ON analytics_events (tenant_id, event_type, created_at);
