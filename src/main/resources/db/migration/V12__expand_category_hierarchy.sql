-- Expanded category tree (idempotent). Category names are globally unique (see V1 schema).
-- V11 seeds Elektronik > Handphone > Smartphone; this migration adds the rest + Other catch-all.

-- ========== Elektronik (extend existing V11 branch) ==========
INSERT INTO categories (name, parent_id)
SELECT 'Tablet', parent.id
FROM categories parent
WHERE parent.name = 'Handphone'
  AND NOT EXISTS (SELECT 1 FROM categories c WHERE c.name = 'Tablet');

INSERT INTO categories (name, parent_id)
SELECT 'Komputer', parent.id
FROM categories parent
WHERE parent.name = 'Elektronik' AND parent.parent_id IS NULL
  AND NOT EXISTS (SELECT 1 FROM categories c WHERE c.name = 'Komputer');

INSERT INTO categories (name, parent_id)
SELECT 'Laptop', parent.id
FROM categories parent
WHERE parent.name = 'Komputer'
  AND NOT EXISTS (SELECT 1 FROM categories c WHERE c.name = 'Laptop');

INSERT INTO categories (name, parent_id)
SELECT 'Desktop PC', parent.id
FROM categories parent
WHERE parent.name = 'Komputer'
  AND NOT EXISTS (SELECT 1 FROM categories c WHERE c.name = 'Desktop PC');

INSERT INTO categories (name, parent_id)
SELECT 'PC Components', parent.id
FROM categories parent
WHERE parent.name = 'Komputer'
  AND NOT EXISTS (SELECT 1 FROM categories c WHERE c.name = 'PC Components');

INSERT INTO categories (name, parent_id)
SELECT 'Audio & Video', parent.id
FROM categories parent
WHERE parent.name = 'Elektronik' AND parent.parent_id IS NULL
  AND NOT EXISTS (SELECT 1 FROM categories c WHERE c.name = 'Audio & Video');

INSERT INTO categories (name, parent_id)
SELECT 'Headphones', parent.id
FROM categories parent
WHERE parent.name = 'Audio & Video'
  AND NOT EXISTS (SELECT 1 FROM categories c WHERE c.name = 'Headphones');

INSERT INTO categories (name, parent_id)
SELECT 'Speakers', parent.id
FROM categories parent
WHERE parent.name = 'Audio & Video'
  AND NOT EXISTS (SELECT 1 FROM categories c WHERE c.name = 'Speakers');

INSERT INTO categories (name, parent_id)
SELECT 'Televisions', parent.id
FROM categories parent
WHERE parent.name = 'Audio & Video'
  AND NOT EXISTS (SELECT 1 FROM categories c WHERE c.name = 'Televisions');

INSERT INTO categories (name, parent_id)
SELECT 'Cameras', parent.id
FROM categories parent
WHERE parent.name = 'Elektronik' AND parent.parent_id IS NULL
  AND NOT EXISTS (SELECT 1 FROM categories c WHERE c.name = 'Cameras');

INSERT INTO categories (name, parent_id)
SELECT 'Gaming Consoles', parent.id
FROM categories parent
WHERE parent.name = 'Elektronik' AND parent.parent_id IS NULL
  AND NOT EXISTS (SELECT 1 FROM categories c WHERE c.name = 'Gaming Consoles');

-- ========== English roots (align with frontend catalogue) ==========
INSERT INTO categories (name, parent_id)
SELECT 'Electronics', NULL
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Electronics');

INSERT INTO categories (name, parent_id)
SELECT 'Mobile Phones', parent.id
FROM categories parent
WHERE parent.name = 'Electronics'
  AND NOT EXISTS (SELECT 1 FROM categories c WHERE c.name = 'Mobile Phones');

INSERT INTO categories (name, parent_id)
SELECT 'Smartphones', parent.id
FROM categories parent
WHERE parent.name = 'Mobile Phones'
  AND NOT EXISTS (SELECT 1 FROM categories c WHERE c.name = 'Smartphones');

INSERT INTO categories (name, parent_id)
SELECT 'Mobile Tablets', parent.id
FROM categories parent
WHERE parent.name = 'Mobile Phones'
  AND NOT EXISTS (SELECT 1 FROM categories c WHERE c.name = 'Mobile Tablets');

INSERT INTO categories (name, parent_id)
SELECT 'Computers', parent.id
FROM categories parent
WHERE parent.name = 'Electronics'
  AND NOT EXISTS (SELECT 1 FROM categories c WHERE c.name = 'Computers');

INSERT INTO categories (name, parent_id)
SELECT 'Laptops', parent.id
FROM categories parent
WHERE parent.name = 'Computers'
  AND NOT EXISTS (SELECT 1 FROM categories c WHERE c.name = 'Laptops');

INSERT INTO categories (name, parent_id)
SELECT 'Monitors', parent.id
FROM categories parent
WHERE parent.name = 'Computers'
  AND NOT EXISTS (SELECT 1 FROM categories c WHERE c.name = 'Monitors');

-- ========== Fashion ==========
INSERT INTO categories (name, parent_id)
SELECT 'Fashion', NULL
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Fashion');

INSERT INTO categories (name, parent_id)
SELECT 'Men''s Fashion', parent.id
FROM categories parent
WHERE parent.name = 'Fashion'
  AND NOT EXISTS (SELECT 1 FROM categories c WHERE c.name = 'Men''s Fashion');

INSERT INTO categories (name, parent_id)
SELECT 'Men''s Shoes', parent.id
FROM categories parent
WHERE parent.name = 'Men''s Fashion'
  AND NOT EXISTS (SELECT 1 FROM categories c WHERE c.name = 'Men''s Shoes');

INSERT INTO categories (name, parent_id)
SELECT 'Men''s Jackets', parent.id
FROM categories parent
WHERE parent.name = 'Men''s Fashion'
  AND NOT EXISTS (SELECT 1 FROM categories c WHERE c.name = 'Men''s Jackets');

INSERT INTO categories (name, parent_id)
SELECT 'Women''s Fashion', parent.id
FROM categories parent
WHERE parent.name = 'Fashion'
  AND NOT EXISTS (SELECT 1 FROM categories c WHERE c.name = 'Women''s Fashion');

INSERT INTO categories (name, parent_id)
SELECT 'Women''s Bags', parent.id
FROM categories parent
WHERE parent.name = 'Women''s Fashion'
  AND NOT EXISTS (SELECT 1 FROM categories c WHERE c.name = 'Women''s Bags');

INSERT INTO categories (name, parent_id)
SELECT 'Dresses', parent.id
FROM categories parent
WHERE parent.name = 'Women''s Fashion'
  AND NOT EXISTS (SELECT 1 FROM categories c WHERE c.name = 'Dresses');

INSERT INTO categories (name, parent_id)
SELECT 'Fashion Accessories', parent.id
FROM categories parent
WHERE parent.name = 'Fashion'
  AND NOT EXISTS (SELECT 1 FROM categories c WHERE c.name = 'Fashion Accessories');

INSERT INTO categories (name, parent_id)
SELECT 'Watches', parent.id
FROM categories parent
WHERE parent.name = 'Fashion Accessories'
  AND NOT EXISTS (SELECT 1 FROM categories c WHERE c.name = 'Watches');

INSERT INTO categories (name, parent_id)
SELECT 'Jewelry', parent.id
FROM categories parent
WHERE parent.name = 'Fashion Accessories'
  AND NOT EXISTS (SELECT 1 FROM categories c WHERE c.name = 'Jewelry');

-- ========== Furniture ==========
INSERT INTO categories (name, parent_id)
SELECT 'Furniture', NULL
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Furniture');

INSERT INTO categories (name, parent_id)
SELECT 'Living Room Furniture', parent.id
FROM categories parent
WHERE parent.name = 'Furniture'
  AND NOT EXISTS (SELECT 1 FROM categories c WHERE c.name = 'Living Room Furniture');

INSERT INTO categories (name, parent_id)
SELECT 'Bedroom Furniture', parent.id
FROM categories parent
WHERE parent.name = 'Furniture'
  AND NOT EXISTS (SELECT 1 FROM categories c WHERE c.name = 'Bedroom Furniture');

INSERT INTO categories (name, parent_id)
SELECT 'Office Furniture', parent.id
FROM categories parent
WHERE parent.name = 'Furniture'
  AND NOT EXISTS (SELECT 1 FROM categories c WHERE c.name = 'Office Furniture');

-- ========== Collectibles ==========
INSERT INTO categories (name, parent_id)
SELECT 'Collectibles', NULL
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Collectibles');

INSERT INTO categories (name, parent_id)
SELECT 'Trading Cards', parent.id
FROM categories parent
WHERE parent.name = 'Collectibles'
  AND NOT EXISTS (SELECT 1 FROM categories c WHERE c.name = 'Trading Cards');

INSERT INTO categories (name, parent_id)
SELECT 'Coins & Stamps', parent.id
FROM categories parent
WHERE parent.name = 'Collectibles'
  AND NOT EXISTS (SELECT 1 FROM categories c WHERE c.name = 'Coins & Stamps');

INSERT INTO categories (name, parent_id)
SELECT 'Memorabilia', parent.id
FROM categories parent
WHERE parent.name = 'Collectibles'
  AND NOT EXISTS (SELECT 1 FROM categories c WHERE c.name = 'Memorabilia');

-- ========== Sports ==========
INSERT INTO categories (name, parent_id)
SELECT 'Sports', NULL
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Sports');

INSERT INTO categories (name, parent_id)
SELECT 'Cycling', parent.id
FROM categories parent
WHERE parent.name = 'Sports'
  AND NOT EXISTS (SELECT 1 FROM categories c WHERE c.name = 'Cycling');

INSERT INTO categories (name, parent_id)
SELECT 'Fitness Equipment', parent.id
FROM categories parent
WHERE parent.name = 'Sports'
  AND NOT EXISTS (SELECT 1 FROM categories c WHERE c.name = 'Fitness Equipment');

INSERT INTO categories (name, parent_id)
SELECT 'Outdoor Sports', parent.id
FROM categories parent
WHERE parent.name = 'Sports'
  AND NOT EXISTS (SELECT 1 FROM categories c WHERE c.name = 'Outdoor Sports');

INSERT INTO categories (name, parent_id)
SELECT 'Team Sports', parent.id
FROM categories parent
WHERE parent.name = 'Sports'
  AND NOT EXISTS (SELECT 1 FROM categories c WHERE c.name = 'Team Sports');

-- ========== Home & Garden ==========
INSERT INTO categories (name, parent_id)
SELECT 'Home & Garden', NULL
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Home & Garden');

INSERT INTO categories (name, parent_id)
SELECT 'Kitchen', parent.id
FROM categories parent
WHERE parent.name = 'Home & Garden'
  AND NOT EXISTS (SELECT 1 FROM categories c WHERE c.name = 'Kitchen');

INSERT INTO categories (name, parent_id)
SELECT 'Garden', parent.id
FROM categories parent
WHERE parent.name = 'Home & Garden'
  AND NOT EXISTS (SELECT 1 FROM categories c WHERE c.name = 'Garden');

INSERT INTO categories (name, parent_id)
SELECT 'Home Decor', parent.id
FROM categories parent
WHERE parent.name = 'Home & Garden'
  AND NOT EXISTS (SELECT 1 FROM categories c WHERE c.name = 'Home Decor');

-- ========== Toys & Games ==========
INSERT INTO categories (name, parent_id)
SELECT 'Toys & Games', NULL
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Toys & Games');

INSERT INTO categories (name, parent_id)
SELECT 'Board Games', parent.id
FROM categories parent
WHERE parent.name = 'Toys & Games'
  AND NOT EXISTS (SELECT 1 FROM categories c WHERE c.name = 'Board Games');

INSERT INTO categories (name, parent_id)
SELECT 'Action Figures', parent.id
FROM categories parent
WHERE parent.name = 'Toys & Games'
  AND NOT EXISTS (SELECT 1 FROM categories c WHERE c.name = 'Action Figures');

INSERT INTO categories (name, parent_id)
SELECT 'Video Games', parent.id
FROM categories parent
WHERE parent.name = 'Toys & Games'
  AND NOT EXISTS (SELECT 1 FROM categories c WHERE c.name = 'Video Games');

-- ========== Art & Antiques ==========
INSERT INTO categories (name, parent_id)
SELECT 'Art & Antiques', NULL
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Art & Antiques');

INSERT INTO categories (name, parent_id)
SELECT 'Paintings', parent.id
FROM categories parent
WHERE parent.name = 'Art & Antiques'
  AND NOT EXISTS (SELECT 1 FROM categories c WHERE c.name = 'Paintings');

INSERT INTO categories (name, parent_id)
SELECT 'Sculptures', parent.id
FROM categories parent
WHERE parent.name = 'Art & Antiques'
  AND NOT EXISTS (SELECT 1 FROM categories c WHERE c.name = 'Sculptures');

INSERT INTO categories (name, parent_id)
SELECT 'Vintage Items', parent.id
FROM categories parent
WHERE parent.name = 'Art & Antiques'
  AND NOT EXISTS (SELECT 1 FROM categories c WHERE c.name = 'Vintage Items');

-- ========== Vehicles ==========
INSERT INTO categories (name, parent_id)
SELECT 'Vehicles', NULL
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Vehicles');

INSERT INTO categories (name, parent_id)
SELECT 'Cars', parent.id
FROM categories parent
WHERE parent.name = 'Vehicles'
  AND NOT EXISTS (SELECT 1 FROM categories c WHERE c.name = 'Cars');

INSERT INTO categories (name, parent_id)
SELECT 'Motorcycles', parent.id
FROM categories parent
WHERE parent.name = 'Vehicles'
  AND NOT EXISTS (SELECT 1 FROM categories c WHERE c.name = 'Motorcycles');

INSERT INTO categories (name, parent_id)
SELECT 'Vehicle Parts', parent.id
FROM categories parent
WHERE parent.name = 'Vehicles'
  AND NOT EXISTS (SELECT 1 FROM categories c WHERE c.name = 'Vehicle Parts');

-- ========== Other (catch-all — robust fallback) ==========
INSERT INTO categories (name, parent_id)
SELECT 'Other', NULL
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Other');

INSERT INTO categories (name, parent_id)
SELECT 'Miscellaneous', parent.id
FROM categories parent
WHERE parent.name = 'Other'
  AND NOT EXISTS (SELECT 1 FROM categories c WHERE c.name = 'Miscellaneous');

INSERT INTO categories (name, parent_id)
SELECT 'Uncategorized', parent.id
FROM categories parent
WHERE parent.name = 'Other'
  AND NOT EXISTS (SELECT 1 FROM categories c WHERE c.name = 'Uncategorized');
