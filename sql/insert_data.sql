BEGIN;

-- Reset tables for idempotent loading
TRUNCATE TABLE item RESTART IDENTITY CASCADE;
TRUNCATE TABLE category RESTART IDENTITY CASCADE;

-- Insert 2,000 categories named CAT0001..CAT2000 (code and name identical)
WITH seq AS (
  SELECT gs AS n FROM generate_series(1, 2000) AS gs
)
INSERT INTO category (code, name)
SELECT
  'CAT' || to_char(n, 'FM0000') AS code,
  'CAT' || to_char(n, 'FM0000') AS name
FROM seq
ORDER BY n;

-- Insert 100,000 items (~50 per category)
-- sku: SKU000001..SKU100000
-- name: Item 000001..Item 100000
-- price: deterministic range 1.00..100.99
-- stock: deterministic 0..499
-- category_id: round-robin across 1..2000
WITH seq AS (
  SELECT gs AS i FROM generate_series(1, 100000) AS gs
)
INSERT INTO item (sku, name, price, stock, category_id)
SELECT
  'SKU'  || to_char(i, 'FM000000')               AS sku,
  'Item '|| to_char(i, 'FM000000')               AS name,
  ROUND((( (i % 10000) + 100 ) / 100.0)::numeric, 2) AS price,
  (i % 500)                                      AS stock,
  ((i - 1) % 2000) + 1                           AS category_id
FROM seq
ORDER BY i;

COMMIT;
