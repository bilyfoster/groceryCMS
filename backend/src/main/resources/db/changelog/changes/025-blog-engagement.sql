--liquibase formatted sql

--changeset cms-team:025-blog-engagement
--comment: Add view_count and like_count to blog posts (views + heart likes; comments unused)

ALTER TABLE blog_posts ADD COLUMN IF NOT EXISTS view_count INTEGER NOT NULL DEFAULT 0;
ALTER TABLE blog_posts ADD COLUMN IF NOT EXISTS like_count INTEGER NOT NULL DEFAULT 0;
