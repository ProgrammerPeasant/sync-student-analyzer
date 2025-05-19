CREATE TABLE analysis_results (
                                  id UUID PRIMARY KEY,
                                  file_id UUID NOT NULL,
                                  file_name VARCHAR(255) NOT NULL,
                                  paragraph_count INT NOT NULL,
                                  word_count INT NOT NULL,
                                  character_count INT NOT NULL,
                                  word_cloud_url VARCHAR(1024),
                                  created_at TIMESTAMP NOT NULL
);

CREATE TABLE plagiarism_results (
                                    id UUID PRIMARY KEY,
                                    source_file_id UUID NOT NULL,
                                    target_file_id UUID NOT NULL,
                                    source_file_name VARCHAR(255) NOT NULL,
                                    target_file_name VARCHAR(255) NOT NULL,
                                    similarity_percentage DOUBLE PRECISION NOT NULL,
                                    is_plagiarism BOOLEAN NOT NULL,
                                    created_at TIMESTAMP NOT NULL
);