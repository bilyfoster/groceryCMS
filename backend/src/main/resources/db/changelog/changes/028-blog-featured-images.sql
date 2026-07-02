--liquibase formatted sql

--changeset cms-team:028-blog-featured-images
--comment: No-op — blog posts are not seeded in the Pickles Bodega conversion
SELECT 1;
