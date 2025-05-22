ALTER TABLE files
    ADD COLUMN file_hash VARCHAR(64);

CREATE UNIQUE INDEX idx_files_file_hash ON files (file_hash);