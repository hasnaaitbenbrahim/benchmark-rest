BEGIN;

-- Schema: Categories and Items

CREATE TABLE IF NOT EXISTS category (
    id          BIGSERIAL PRIMARY KEY,
    code        VARCHAR(32)  NOT NULL UNIQUE,
    name        VARCHAR(128) NOT NULL,
    updated_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS item (
    id           BIGSERIAL PRIMARY KEY,
    sku          VARCHAR(64)  NOT NULL UNIQUE,
    name         VARCHAR(128) NOT NULL,
    price        NUMERIC(10,2) NOT NULL CHECK (price >= 0),
    stock        INTEGER       NOT NULL DEFAULT 0 CHECK (stock >= 0),
    category_id  BIGINT        NOT NULL REFERENCES category(id) ON DELETE RESTRICT,
    updated_at   TIMESTAMP     NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_item_category_id ON item(category_id);

COMMIT;
