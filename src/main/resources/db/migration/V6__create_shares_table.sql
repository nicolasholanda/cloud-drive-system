CREATE TABLE shares (
    id UUID PRIMARY KEY,
    file_id UUID NOT NULL REFERENCES files(id),
    shared_with_id UUID NOT NULL REFERENCES users(id),
    permission VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    UNIQUE (file_id, shared_with_id)
);

CREATE INDEX idx_shares_file_id ON shares(file_id);
CREATE INDEX idx_shares_shared_with_id ON shares(shared_with_id);
