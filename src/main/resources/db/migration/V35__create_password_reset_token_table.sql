CREATE TABLE password_reset_token (
    id BIGINT GENERATED ALWAYS AS IDENTITY,
    id_user BIGINT NOT NULL,
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    used_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT pk_password_reset_token PRIMARY KEY (id),
    CONSTRAINT fk_password_reset_token_user FOREIGN KEY (id_user) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_password_reset_token_user ON password_reset_token(id_user);
