--liquibase formatted sql

--changeset cms-team:029-autistic-burnout-image
--comment: Add an inline image to the Autistic Burnout blog post

UPDATE blog_posts
SET body = REPLACE(
    body,
    '<blockquote>"Burnout is not a character flaw. It is a physiological event, no different than catching a cold."</blockquote>\n\n<h2>Symptoms & Signs</h2>',
    '<blockquote>"Burnout is not a character flaw. It is a physiological event, no different than catching a cold."</blockquote>\n\n<figure><img src="/images/support-group.jpg" alt="A calm, supportive group setting" /><figcaption>Rest and connection are core to recovering from autistic burnout.</figcaption></figure>\n\n<h2>Symptoms & Signs</h2>'
),
    updated_at = NOW()
WHERE slug = 'what-is-autistic-burnout'
  AND body LIKE '%Burnout is not a character flaw%'
  AND body NOT LIKE '%support-group.jpg%';
