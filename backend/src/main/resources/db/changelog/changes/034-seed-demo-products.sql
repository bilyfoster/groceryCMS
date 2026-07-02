--liquibase formatted sql

--changeset cms-team:034-seed-demo-products
--comment: Seed a small demo catalog of allergy-friendly grocery products for Pickles Bodega

WITH t AS (
    SELECT id AS tenant_id FROM tenants WHERE slug = 'demo'
),
new_products AS (
    INSERT INTO products (id, tenant_id, name, slug, brand, description, price, unit, stock_status, store_section, published, sort_order, created_at, updated_at)
    SELECT
        v.id,
        t.tenant_id,
        v.name,
        v.slug,
        v.brand,
        v.description,
        v.price,
        v.unit,
        v.stock_status,
        v.store_section,
        v.published,
        v.sort_order,
        NOW(),
        NOW()
    FROM (VALUES
        ('10000000-0000-4000-8000-000000000001'::uuid, 'Gluten-Free Sourdough Bread', 'gluten-free-sourdough-bread', 'Rise Above', 'Tangy gluten-free sourdough made with a blend of rice and sorghum flours.', 7.99, 'loaf', 'IN_STOCK', 'BAKERY', TRUE, 1),
        ('10000000-0000-4000-8000-000000000002'::uuid, 'Dairy-Free Cheddar Shreds', 'dairy-free-cheddar-shreds', 'Miyokos Creamery', 'Plant-based cheddar-style shreds that melt and stretch.', 5.49, '8 oz bag', 'IN_STOCK', 'REFRIGERATED', TRUE, 2),
        ('10000000-0000-4000-8000-000000000003'::uuid, 'Nut-Free Maple Granola', 'nut-free-maple-granola', 'Safe Snack Co', 'Oats, maple syrup, and sunflower seeds — completely nut-free.', 6.99, '12 oz bag', 'IN_STOCK', 'PANTRY', TRUE, 3),
        ('10000000-0000-4000-8000-000000000004'::uuid, 'Frozen Gluten-Free Pizza', 'frozen-gluten-free-pizza', 'Caulipower', 'Thin-crust frozen pizza with mozzarella-style topping.', 9.99, 'each', 'IN_STOCK', 'FROZEN', TRUE, 4),
        ('10000000-0000-4000-8000-000000000005'::uuid, 'Organic Gala Apples', 'organic-gala-apples', 'Flagstaff Organics', 'Crisp, locally sourced organic apples.', 3.99, 'lb', 'IN_STOCK', 'PRODUCE', TRUE, 5),
        ('10000000-0000-4000-8000-000000000006'::uuid, 'Rice Flour Tortillas', 'rice-flour-tortillas', 'Siete Foods', 'Soft tortillas made from rice flour — gluten-free and vegan.', 5.99, '8 count', 'IN_STOCK', 'BAKERY', TRUE, 6)
    ) AS v(id, name, slug, brand, description, price, unit, stock_status, store_section, published, sort_order)
    CROSS JOIN t
    WHERE NOT EXISTS (
        SELECT 1 FROM products p WHERE p.tenant_id = t.tenant_id AND p.slug = v.slug
    )
    RETURNING id, slug, tenant_id
)
INSERT INTO product_terms (product_id, term_id)
SELECT np.id, tt.id
FROM new_products np
CROSS JOIN (
    SELECT id FROM taxonomy_terms WHERE tenant_id = (SELECT tenant_id FROM new_products LIMIT 1) AND type = 'ALLERGY_TYPE' AND slug = 'gluten-free'
) tt
WHERE np.slug IN ('gluten-free-sourdough-bread', 'nut-free-maple-granola', 'frozen-gluten-free-pizza', 'rice-flour-tortillas')
UNION ALL
SELECT np.id, tt.id
FROM new_products np
CROSS JOIN (
    SELECT id FROM taxonomy_terms WHERE tenant_id = (SELECT tenant_id FROM new_products LIMIT 1) AND type = 'ALLERGY_TYPE' AND slug = 'dairy-free'
) tt
WHERE np.slug IN ('dairy-free-cheddar-shreds', 'frozen-gluten-free-pizza')
UNION ALL
SELECT np.id, tt.id
FROM new_products np
CROSS JOIN (
    SELECT id FROM taxonomy_terms WHERE tenant_id = (SELECT tenant_id FROM new_products LIMIT 1) AND type = 'ALLERGY_TYPE' AND slug = 'nut-free'
) tt
WHERE np.slug = 'nut-free-maple-granola'
UNION ALL
SELECT np.id, tt.id
FROM new_products np
CROSS JOIN (
    SELECT id FROM taxonomy_terms WHERE tenant_id = (SELECT tenant_id FROM new_products LIMIT 1) AND type = 'DIET_TYPE' AND slug = 'vegan'
) tt
WHERE np.slug IN ('dairy-free-cheddar-shreds', 'rice-flour-tortillas')
UNION ALL
SELECT np.id, tt.id
FROM new_products np
CROSS JOIN (
    SELECT id FROM taxonomy_terms WHERE tenant_id = (SELECT tenant_id FROM new_products LIMIT 1) AND type = 'DIET_TYPE' AND slug = 'organic'
) tt
WHERE np.slug = 'organic-gala-apples';
