CREATE TABLE blocks (
    id UUID PRIMARY KEY,
    hash VARCHAR(64) NOT NULL UNIQUE,
    size_bytes BIGINT NOT NULL,
    storage_key VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL
);
