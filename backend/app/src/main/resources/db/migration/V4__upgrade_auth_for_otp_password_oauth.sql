ALTER TYPE user_role ADD VALUE IF NOT EXISTS 'HR';
ALTER TYPE user_role ADD VALUE IF NOT EXISTS 'DEVELOPER';
ALTER TYPE user_role ADD VALUE IF NOT EXISTS 'STUDENT';
ALTER TYPE user_role ADD VALUE IF NOT EXISTS 'OTHER';

ALTER TABLE users
    ADD COLUMN IF NOT EXISTS first_name VARCHAR(100),
    ADD COLUMN IF NOT EXISTS last_name VARCHAR(100),
    ADD COLUMN IF NOT EXISTS auth_provider VARCHAR(20) NOT NULL DEFAULT 'LOCAL',
    ADD COLUMN IF NOT EXISTS password_hash VARCHAR(255),
    ADD COLUMN IF NOT EXISTS is_email_verified BOOLEAN NOT NULL DEFAULT FALSE;

UPDATE users
SET first_name = COALESCE(first_name, 'User'),
    last_name = COALESCE(last_name, 'Account')
WHERE first_name IS NULL OR last_name IS NULL;

ALTER TABLE users
    ALTER COLUMN first_name SET NOT NULL,
    ALTER COLUMN last_name SET NOT NULL,
    ALTER COLUMN email SET NOT NULL;

ALTER TABLE users
    DROP COLUMN IF EXISTS phone_number;

CREATE TABLE IF NOT EXISTS refresh_tokens (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_id VARCHAR(64) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    revoked_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE refresh_tokens ENABLE ROW LEVEL SECURITY;

CREATE POLICY tenant_isolation_refresh_tokens ON refresh_tokens
    USING (
        EXISTS (
            SELECT 1
            FROM users u
            WHERE u.id = refresh_tokens.user_id
              AND u.tenant_id = current_setting('app.current_tenant', true)::UUID
        )
    );

CREATE INDEX IF NOT EXISTS idx_refresh_tokens_user_id ON refresh_tokens(user_id);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_expires_at ON refresh_tokens(expires_at);
