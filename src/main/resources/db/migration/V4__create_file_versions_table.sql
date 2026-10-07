CREATE TABLE file_versions (
    id UUID PRIMARY KEY,
    file_id UUID NOT NULL REFERENCES files(id),
    version_number INTEGER NOT NULL,
    total_size_bytes BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    UNIQUE (file_id, version_number)
);

CREATE INDEX idx_file_versions_file_id ON file_versions(file_id);
