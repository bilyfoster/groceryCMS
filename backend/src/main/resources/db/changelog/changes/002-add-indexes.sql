--liquibase formatted sql

--changeset cms-team:ALIGN-1-002-add-indexes
--comment: Partial indexes for tenant-scoped queries and token lookups

CREATE INDEX idx_pages_tenant ON pages(tenant_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_blog_posts_tenant ON blog_posts(tenant_id, published_at DESC) WHERE deleted_at IS NULL;
CREATE INDEX idx_comments_post ON comments(post_id, approved) WHERE deleted_at IS NULL;
CREATE INDEX idx_users_tenant_email ON users(tenant_id, email) WHERE deleted_at IS NULL;
CREATE INDEX idx_auth_tokens_hash ON auth_tokens(token_hash) WHERE used_at IS NULL;
