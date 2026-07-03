--liquibase formatted sql

--changeset cms-team:035-fundraiser-content
--comment: Add Pickles Bodega fundraiser pages and navigation

UPDATE tenants
SET settings = settings || '{
    "siteName":"Pickles Bodega",
    "primaryColor":"#148a4a",
    "secondaryColor":"#334155"
}'::jsonb
WHERE id = 'a0000000-0000-4000-8000-000000000001';

UPDATE pages
SET
    title = 'Help Pickles Bodega reach its GoFundMe goal',
    layout = 'hero-centered',
    meta_title = 'Help Pickles Bodega reach its GoFundMe goal',
    meta_description = 'Support Chef Sam Gutierrez as she builds Pickles Bodega, Flagstaff''s first dedicated gluten-free and allergy-aware food hub.',
    og_image_url = '/images/shop-seller-presents-products-2026-03-19-01-47-13-utc.jpg',
    updated_at = NOW()
WHERE id = 'b0000000-0000-4000-8000-000000000001';

DELETE FROM content_blocks
WHERE page_id = 'b0000000-0000-4000-8000-000000000001';

INSERT INTO content_blocks (page_id, block_type, sort_order, content)
VALUES
    (
        'b0000000-0000-4000-8000-000000000001',
        'hero',
        0,
        $${
            "heading":"Help Chef Sam build Flagstaff's first gluten-free food hub",
            "subheading":"Pickles Bodega is a dedicated gluten-free, allergy-aware bodega and deli concept built around safe food, local producers, and the peace of mind most grocery stores cannot provide.",
            "buttonText":"Contribute on GoFundMe",
            "buttonUrl":"https://www.gofundme.com/f/help-chef-sam-bring-glutenfree-options-to-flagstaff",
            "backgroundImage":"/images/shop-seller-presents-products-2026-03-19-01-47-13-utc.jpg",
            "objectPosition":"center",
            "overlay":true
        }$$::jsonb
    ),
    (
        'b0000000-0000-4000-8000-000000000001',
        'text',
        10,
        $${
            "heading":"Born from a real diagnosis",
            "body":"<p>Chef Sam Gutierrez has lived the problem Pickles Bodega is solving. After her celiac diagnosis, she learned that eating safely is not only about avoiding bread. Gluten can show up through cross-contamination, product placement, vitamins, beauty products, shared kitchens, and the everyday systems most stores were never designed to question.</p>",
            "alignment":"left",
            "icon":"leaf"
        }$$::jsonb
    ),
    (
        'b0000000-0000-4000-8000-000000000001',
        'text',
        20,
        $${
            "heading":"A clear market gap",
            "body":"<p>The GoFundMe lays out the opportunity plainly: Flagstaff lacks dedicated gluten-free dining and shopping options. Pickles Bodega answers that gap with a 100% gluten-free, allergy-aware retail environment, locally sourced produce and goods, and ready-to-eat meals prepared with strict safety protocols.</p>",
            "alignment":"left",
            "icon":"heart"
        }$$::jsonb
    ),
    (
        'b0000000-0000-4000-8000-000000000001',
        'text',
        30,
        $${
            "heading":"A practical first step",
            "body":"<p>Sam is raising support to prove and launch the concept, from a mini version at the farmers market to working capital for a storefront. Funds help cover deposit, rent, fixtures, equipment, inventory, permits, insurance, launch marketing, and the safe setup a gluten-free space requires.</p>",
            "alignment":"left",
            "icon":"sparkles"
        }$$::jsonb
    ),
    (
        'b0000000-0000-4000-8000-000000000001',
        'image',
        40,
        $${
            "url":"/images/exhibition-of-organic-vegetables-and-fruits-in-mar-2026-01-07-01-34-45-utc.jpg",
            "altText":"Colorful produce arranged at a neighborhood market",
            "caption":"The goal is simple: make safe, exciting grocery choices easier to find close to home.",
            "width":1200,
            "alignment":"center"
        }$$::jsonb
    ),
    (
        'b0000000-0000-4000-8000-000000000001',
        'cta',
        50,
        $${
            "heading":"Help prove the concept in Flagstaff",
            "body":"Every contribution helps turn a chef-led idea into a safe, useful, locally rooted food destination for people who need one.",
            "primaryButton":{"text":"Donate on GoFundMe","url":"https://www.gofundme.com/f/help-chef-sam-bring-glutenfree-options-to-flagstaff"},
            "secondaryButton":{"text":"Read the story","url":"/our-story"}
        }$$::jsonb
    );

INSERT INTO pages (id, tenant_id, slug, title, page_type, layout, nav_order, published, meta_title, meta_description, og_image_url)
VALUES
    (
        'b0000000-0000-4000-8000-000000000003',
        'a0000000-0000-4000-8000-000000000001',
        'our-story',
        'Our Story',
        'custom',
        'full-width',
        1,
        TRUE,
        'Chef Sam''s story behind Pickles Bodega',
        'Learn how Chef Sam Gutierrez turned a celiac diagnosis into a mission for safer gluten-free food in Flagstaff.',
        '/images/smiling-woman-buying-organic-products-in-zero-wast-2026-01-08-22-29-57-utc.jpg'
    ),
    (
        'b0000000-0000-4000-8000-000000000004',
        'a0000000-0000-4000-8000-000000000001',
        'the-vision',
        'The Concept',
        'custom',
        'full-width',
        2,
        TRUE,
        'The Pickles Bodega concept',
        'See why Pickles Bodega can become Flagstaff''s dedicated gluten-free, allergy-aware food hub.',
        '/images/shoppers-buying-fresh-fruit-and-vegetables-in-sust-2026-01-05-06-35-46-utc.jpg'
    ),
    (
        'b0000000-0000-4000-8000-000000000005',
        'a0000000-0000-4000-8000-000000000001',
        'ways-to-help',
        'Fund the Launch',
        'custom',
        'full-width',
        3,
        TRUE,
        'Fund the Pickles Bodega launch',
        'See how donations help Chef Sam launch a gluten-free food hub in Flagstaff.',
        '/images/black-couple-on-grocery-shopping-posing-with-shop-2026-01-08-23-28-31-utc.jpg'
    )
ON CONFLICT (tenant_id, slug) DO UPDATE
SET
    title = EXCLUDED.title,
    page_type = EXCLUDED.page_type,
    layout = EXCLUDED.layout,
    nav_order = EXCLUDED.nav_order,
    published = EXCLUDED.published,
    meta_title = EXCLUDED.meta_title,
    meta_description = EXCLUDED.meta_description,
    og_image_url = EXCLUDED.og_image_url,
    updated_at = NOW();

DELETE FROM content_blocks
WHERE page_id IN (
    'b0000000-0000-4000-8000-000000000003',
    'b0000000-0000-4000-8000-000000000004',
    'b0000000-0000-4000-8000-000000000005'
);

INSERT INTO content_blocks (page_id, block_type, sort_order, content)
VALUES
    (
        'b0000000-0000-4000-8000-000000000003',
        'hero',
        0,
        $${
            "heading":"Chef Sam knows why this needs to exist",
            "subheading":"Pickles Bodega comes from lived experience, culinary training, and a simple belief: people with celiac disease and food allergies deserve to shop without fear.",
            "buttonText":"Help fund the opening",
            "buttonUrl":"https://www.gofundme.com/f/help-chef-sam-bring-glutenfree-options-to-flagstaff",
            "backgroundImage":"/images/smiling-woman-buying-organic-products-in-zero-wast-2026-01-08-22-29-57-utc.jpg",
            "objectPosition":"center",
            "overlay":true
        }$$::jsonb
    ),
    (
        'b0000000-0000-4000-8000-000000000003',
        'text',
        10,
        $${
            "heading":"The hidden cost of unsafe food",
            "body":"<p>Sam described grocery shopping with celiac disease as exhausting because danger can hide in ingredients, flour dust, shelf placement, shared equipment, and casual assumptions that cross-contamination is not serious. For sensitive shoppers, one mistake can mean getting sick, missing work, or ending up in the hospital.</p>",
            "alignment":"left",
            "icon":"compass"
        }$$::jsonb
    ),
    (
        'b0000000-0000-4000-8000-000000000003',
        'text',
        20,
        $${
            "heading":"The chef advantage",
            "body":"<p>Sam is not approaching this as a trend. She has worked across restaurants and hotels, moving from back of house to front of house to food and beverage leadership. That experience gives Pickles Bodega a stronger foundation for sourcing, safety protocols, customer service, and eventually prepared foods.</p>",
            "alignment":"left",
            "icon":"heart"
        }$$::jsonb
    ),
    (
        'b0000000-0000-4000-8000-000000000003',
        'text',
        30,
        $${
            "heading":"The community vision",
            "body":"<p>The first version is a bodega: safe groceries, local goods, and meals to go. The larger vision includes a gluten-free deli, food hall partnerships, and eventually a workplace readiness center where survivors of domestic violence can train, heal, and build careers.</p>",
            "alignment":"left",
            "icon":"hand-heart"
        }$$::jsonb
    ),
    (
        'b0000000-0000-4000-8000-000000000003',
        'image',
        40,
        $${
            "url":"/images/by-the-shelves-with-products-gorgeous-woman-is-in-2026-01-08-08-11-25-utc.JPG",
            "altText":"A shopper browsing grocery shelves",
            "caption":"A better shopping experience begins with shelves that reflect real community needs.",
            "width":1200,
            "alignment":"center"
        }$$::jsonb
    ),
    (
        'b0000000-0000-4000-8000-000000000003',
        'cta',
        50,
        $${
            "heading":"Back the person who can build it",
            "body":"Chef Sam has the lived experience, food background, vendor relationships, and community drive. The missing piece is launch capital.",
            "primaryButton":{"text":"Donate on GoFundMe","url":"https://www.gofundme.com/f/help-chef-sam-bring-glutenfree-options-to-flagstaff"},
            "secondaryButton":{"text":"See the vision","url":"/the-vision"}
        }$$::jsonb
    ),
    (
        'b0000000-0000-4000-8000-000000000004',
        'hero',
        0,
        $${
            "heading":"A safer food model Flagstaff can lead",
            "subheading":"Pickles Bodega is designed as a trusted destination for restricted diets, local sourcing, prepared meals, and a more inclusive food culture.",
            "buttonText":"Support the goal",
            "buttonUrl":"https://www.gofundme.com/f/help-chef-sam-bring-glutenfree-options-to-flagstaff",
            "backgroundImage":"/images/shoppers-buying-fresh-fruit-and-vegetables-in-sust-2026-01-05-06-35-46-utc.jpg",
            "objectPosition":"center",
            "overlay":true
        }$$::jsonb
    ),
    (
        'b0000000-0000-4000-8000-000000000004',
        'text',
        10,
        $${
            "heading":"100% gluten-free by design",
            "body":"<p>Instead of placing gluten-free flour under wheat flour or gluten-free bread beneath wheat bread, Pickles Bodega starts from a safer premise: a dedicated environment where shoppers do not have to inspect every shelf for preventable risk.</p>",
            "alignment":"left",
            "icon":"shield"
        }$$::jsonb
    ),
    (
        'b0000000-0000-4000-8000-000000000004',
        'text',
        20,
        $${
            "heading":"Local vendors, real food",
            "body":"<p>Sam is lining up local and regional partners for coffee, granola, produce, and specialty goods. The goal is not just packaged substitutes. It is fresh, local, delicious food that keeps more money circulating in the community.</p>",
            "alignment":"left",
            "icon":"users"
        }$$::jsonb
    ),
    (
        'b0000000-0000-4000-8000-000000000004',
        'text',
        30,
        $${
            "heading":"A scalable concept",
            "body":"<p>The GoFundMe frames Pickles Bodega as a mission-driven retail model with expansion potential. The broader idea can inspire other communities: safe grocery, trusted prepared food, and allergy-aware hospitality built into the business from day one.</p>",
            "alignment":"left",
            "icon":"star"
        }$$::jsonb
    ),
    (
        'b0000000-0000-4000-8000-000000000004',
        'columns',
        40,
        $${
            "columns":[
                {"blocks":[{"id":"vision-1","blockType":"image","sortOrder":0,"published":true,"content":{"url":"/images/male-s-hand-taking-an-artichoke-from-a-grocery-sho-2026-01-07-06-07-47-utc.jpg","altText":"A hand choosing fresh produce","width":800,"alignment":"center"}}]},
                {"blocks":[{"id":"vision-2","blockType":"image","sortOrder":0,"published":true,"content":{"url":"/images/signage-word-of-vegan-food-in-supermarket-grocery-2026-01-09-00-38-58-utc.jpg","altText":"Vegan grocery aisle signage","width":800,"alignment":"center"}}]}
            ]
        }$$::jsonb
    ),
    (
        'b0000000-0000-4000-8000-000000000004',
        'cta',
        50,
        $${
            "heading":"This is a business case and a care case",
            "body":"Pickles Bodega meets a real market gap while serving people who are often left to solve food safety alone.",
            "primaryButton":{"text":"Contribute now","url":"https://www.gofundme.com/f/help-chef-sam-bring-glutenfree-options-to-flagstaff"},
            "secondaryButton":{"text":"Ways to help","url":"/ways-to-help"}
        }$$::jsonb
    ),
    (
        'b0000000-0000-4000-8000-000000000005',
        'hero',
        0,
        $${
            "heading":"Fund the launch path",
            "subheading":"The GoFundMe helps Chef Sam move from vision to proof: a farmers market mini-launch, then the working capital needed for a storefront.",
            "buttonText":"Donate on GoFundMe",
            "buttonUrl":"https://www.gofundme.com/f/help-chef-sam-bring-glutenfree-options-to-flagstaff",
            "backgroundImage":"/images/black-couple-on-grocery-shopping-posing-with-shop-2026-01-08-23-28-31-utc.jpg",
            "objectPosition":"center",
            "overlay":true
        }$$::jsonb
    ),
    (
        'b0000000-0000-4000-8000-000000000005',
        'text',
        10,
        $${
            "heading":"1. Help cover startup costs",
            "body":"<p>The campaign identifies concrete needs: security deposit, first months of rent, equipment and fixtures, initial inventory, permits, insurance, setup, marketing, and launch expenses.</p>",
            "alignment":"left",
            "icon":"hand-heart"
        }$$::jsonb
    ),
    (
        'b0000000-0000-4000-8000-000000000005',
        'text',
        20,
        $${
            "heading":"2. Build momentum",
            "body":"<p>Sam named attention as one of the biggest hurdles. Sharing the campaign with local leaders, food networks, parents, teachers, healthcare providers, and allergy-aware communities can help the concept reach the people with power to move it forward.</p>",
            "alignment":"left",
            "icon":"message"
        }$$::jsonb
    ),
    (
        'b0000000-0000-4000-8000-000000000005',
        'text',
        30,
        $${
            "heading":"3. Open doors",
            "body":"<p>Introductions to landlords, small business funders, city leaders, local vendors, commercial equipment sources, and community partners can stretch the fundraiser further than dollars alone.</p>",
            "alignment":"left",
            "icon":"handshake"
        }$$::jsonb
    ),
    (
        'b0000000-0000-4000-8000-000000000005',
        'cta',
        40,
        $${
            "heading":"Help Chef Sam make the safe option the easy option",
            "body":"Open the GoFundMe, make a contribution if you can, and share the concept with someone who can help Pickles Bodega reach its next milestone.",
            "primaryButton":{"text":"Open GoFundMe","url":"https://www.gofundme.com/f/help-chef-sam-bring-glutenfree-options-to-flagstaff"},
            "secondaryButton":{"text":"Read the story","url":"/our-story"}
        }$$::jsonb
    );

INSERT INTO menu_items (id, menu_id, label, page_id, url, target, sort_order)
VALUES
    ('d0000000-0000-4000-8000-000000000013', 'd0000000-0000-4000-8000-000000000001', 'Our Story', 'b0000000-0000-4000-8000-000000000003', NULL, '_self', 1),
    ('d0000000-0000-4000-8000-000000000014', 'd0000000-0000-4000-8000-000000000001', 'The Concept', 'b0000000-0000-4000-8000-000000000004', NULL, '_self', 2),
    ('d0000000-0000-4000-8000-000000000015', 'd0000000-0000-4000-8000-000000000001', 'Fund the Launch', 'b0000000-0000-4000-8000-000000000005', NULL, '_self', 3),
    ('d0000000-0000-4000-8000-000000000016', 'd0000000-0000-4000-8000-000000000001', 'Donate', NULL, 'https://www.gofundme.com/f/help-chef-sam-bring-glutenfree-options-to-flagstaff', '_blank', 4)
ON CONFLICT (id) DO UPDATE
SET
    label = EXCLUDED.label,
    page_id = EXCLUDED.page_id,
    url = EXCLUDED.url,
    target = EXCLUDED.target,
    sort_order = EXCLUDED.sort_order,
    updated_at = NOW();

UPDATE menu_items
SET sort_order = 5, updated_at = NOW()
WHERE id = 'd0000000-0000-4000-8000-000000000012';
