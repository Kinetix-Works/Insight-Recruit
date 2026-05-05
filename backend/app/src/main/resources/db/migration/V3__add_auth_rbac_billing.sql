-- ==============================================================================
-- Migration: V3__add_auth_rbac_billing.sql
-- Description: Adds passwordless auth, RBAC, and subscription foundations.
-- Target DB: PostgreSQL 13+
-- ==============================================================================

-- 1. ENUM Definitions
CREATE TYPE user_role AS ENUM ('TENANT_ADMIN', 'RECRUITER', 'VIEWER');
CREATE TYPE plan_type AS ENUM ('FREE', 'PRO', 'ENTERPRISE');
CREATE TYPE subscription_status AS ENUM ('ACTIVE', 'PAST_DUE', 'CANCELED');

-- 2. Table: users (passwordless principal records)
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    email VARCHAR(320) UNIQUE,
    phone_number VARCHAR(20) UNIQUE,
    role user_role NOT NULL DEFAULT 'VIEWER',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_users_identifier_present CHECK (email IS NOT NULL OR phone_number IS NOT NULL)
);

-- 3. Table: tenant_subscriptions (one active record per tenant)
CREATE TABLE tenant_subscriptions (
    tenant_id UUID PRIMARY KEY REFERENCES tenants(id) ON DELETE CASCADE,
    plan_type plan_type NOT NULL DEFAULT 'FREE',
    status subscription_status NOT NULL DEFAULT 'ACTIVE',
    features_json JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 4. RLS for new tenant-bound tables
ALTER TABLE users ENABLE ROW LEVEL SECURITY;
ALTER TABLE tenant_subscriptions ENABLE ROW LEVEL SECURITY;

CREATE POLICY tenant_isolation_users ON users
    USING (tenant_id = current_setting('app.current_tenant', true)::UUID);

CREATE POLICY tenant_isolation_tenant_subscriptions ON tenant_subscriptions
    USING (tenant_id = current_setting('app.current_tenant', true)::UUID);

-- 5. Performance indexes
CREATE INDEX idx_users_tenant_id ON users(tenant_id);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_tenant_subscriptions_status ON tenant_subscriptions(status);
