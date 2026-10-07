CREATE TABLE file_version_blocks (
    id UUID PRIMARY KEY,
    file_version_id UUID NOT NULL REFERENCES file_versions(id),
    block_id UUID NOT NULL REFERENCES blocks(id),
    block_order INTEGER NOT NULL,
    UNIQUE (file_version_id, block_order)
);

CREATE INDEX idx_file_version_blocks_file_version_id ON file_version_blocks(file_version_id);
CREATE INDEX idx_file_version_blocks_block_id ON file_version_blocks(block_id);
