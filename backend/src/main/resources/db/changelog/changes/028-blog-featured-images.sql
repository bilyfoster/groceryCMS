--liquibase formatted sql

--changeset cms-team:028-publish-insurance-post
--comment: Publish the pre-existing "Navigating Insurance" post and give it its real featured image (3rd post parity)

UPDATE blog_posts
SET published = TRUE,
    published_at = COALESCE(published_at, now()),
    featured_image = '/images/blog/insurance-guide.jpg'
WHERE tenant_id = 'a0000000-0000-4000-8000-000000000001'
  AND slug = 'navigating-insurance-for-mental-health-care'
  AND deleted_at IS NULL;
