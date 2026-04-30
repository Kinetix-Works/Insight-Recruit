-- ==============================================================================
-- Migration: V1__init_schema_and_rls.sql
-- Description: Core tables, ENUMs, and Row-Level Security (RLS) policies.
-- Target DB: PostgreSQL 13+
-- ==============================================================================

-- Enable UUID extension (if not already enabled by default in newer PG versions)
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 1. ENUM Definitions
CREATE TYPE processing_status AS ENUM ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED');

-- 2. Table: Tenants (Highest level of isolation)
CREATE TABLE tenants (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    company_name VARCHAR(255) NOT NULL,
    api_key VARCHAR(255) UNIQUE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 3. Table: Jobs (Metadata for specific roles)
CREATE TABLE jobs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    description_text TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 4. Table: Candidates (Stores extracted data, AI scores, and retention tracking)
CREATE TABLE candidates (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    job_id UUID NOT NULL REFERENCES jobs(id) ON DELETE CASCADE,
    name VARCHAR(255),
    resume_url VARCHAR(500) NOT NULL,
    score DECIMAL(5,2),
    ai_summary TEXT,
    status processing_status DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP -- Nullable; used for GDPR 90-day soft-delete/hard-purge sweeps
);

-- 5. Table: Audit_Logs (Tracks all actions for compliance)
CREATE TABLE audit_logs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    user_id VARCHAR(255) NOT NULL, -- Could be an email or external auth ID
    action VARCHAR(255) NOT NULL,  -- e.g., 'VIEW_RESUME', 'DOWNLOAD_PDF'
    resource_id UUID NOT NULL,     -- ID of the candidate or job accessed
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 6. Table: Feedback (Captures recruiter corrections for AI fine-tuning)
CREATE TABLE feedback (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    candidate_id UUID NOT NULL REFERENCES candidates(id) ON DELETE CASCADE,
    is_accurate BOOLEAN NOT NULL,
    user_comment TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ==============================================================================
-- 7. Row-Level Security (RLS) Implementation
-- ==============================================================================

-- Enable RLS on all tenant-bound tables
ALTER TABLE jobs ENABLE ROW LEVEL SECURITY;
ALTER TABLE candidates ENABLE ROW LEVEL SECURITY;
ALTER TABLE audit_logs ENABLE ROW LEVEL SECURITY;
ALTER TABLE feedback ENABLE ROW LEVEL SECURITY;

-- Create Policies
-- Note: Your backend (Spring Boot) MUST set the 'app.current_tenant' configuration 
-- variable at the start of every database transaction using a web filter/interceptor.

CREATE POLICY tenant_isolation_jobs ON jobs
    USING (tenant_id = current_setting('app.current_tenant', true)::UUID);

CREATE POLICY tenant_isolation_candidates ON candidates
    USING (tenant_id = current_setting('app.current_tenant', true)::UUID);

CREATE POLICY tenant_isolation_audit_logs ON audit_logs
    USING (tenant_id = current_setting('app.current_tenant', true)::UUID);

CREATE POLICY tenant_isolation_feedback ON feedback
    USING (tenant_id = current_setting('app.current_tenant', true)::UUID);

-- ==============================================================================
-- 8. Performance Indexes
-- ==============================================================================
CREATE INDEX idx_candidates_job_id ON candidates(job_id);
CREATE INDEX idx_candidates_status ON candidates(status);
CREATE INDEX idx_audit_logs_resource ON audit_logs(resource_id);