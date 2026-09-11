CREATE TABLE IF NOT EXISTS ai_clauses (
    clause_id SERIAL PRIMARY KEY,
    version_id INT NOT NULL,
    clause_type VARCHAR(100) NOT NULL,
    title VARCHAR(255) NOT NULL,
    clause_text TEXT NOT NULL,
    matched_keywords JSONB,
    page_number INT,
    confidence FLOAT,
    model_id INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ai_clauses_version
        FOREIGN KEY (version_id) REFERENCES contract_versions(version_id) ON DELETE CASCADE,
    CONSTRAINT fk_ai_clauses_model
        FOREIGN KEY (model_id) REFERENCES ai_models(model_id)
);

CREATE INDEX IF NOT EXISTS idx_ai_clauses_version
    ON ai_clauses(version_id);
