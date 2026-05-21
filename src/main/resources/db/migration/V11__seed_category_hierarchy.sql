-- Core Indonesian example hierarchy: Elektronik > Handphone > Smartphone (extended in V12)
INSERT INTO categories (name, parent_id)
SELECT 'Elektronik', NULL
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Elektronik' AND parent_id IS NULL);

INSERT INTO categories (name, parent_id)
SELECT 'Handphone', parent.id
FROM categories parent
WHERE parent.name = 'Elektronik' AND parent.parent_id IS NULL
  AND NOT EXISTS (
      SELECT 1 FROM categories child
      WHERE child.name = 'Handphone' AND child.parent_id = parent.id
  );

INSERT INTO categories (name, parent_id)
SELECT 'Smartphone', parent.id
FROM categories parent
WHERE parent.name = 'Handphone'
  AND NOT EXISTS (
      SELECT 1 FROM categories child
      WHERE child.name = 'Smartphone' AND child.parent_id = parent.id
  );
