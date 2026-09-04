-- =====================================================================
-- Phase 2 — Distributed State & Multi-Tenancy (L2 PostgreSQL Ledger)
-- =====================================================================
-- Every table is tenant-scoped. No query may run without a tenant_id
-- predicate at the application layer.
-- =====================================================================

-- ---------------------------------------------------------------------
-- Episodic Memory
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS episodic_memory (
    tenant_id    VARCHAR(128) NOT NULL,
    memory_id    VARCHAR(128) NOT NULL,
    content      TEXT NOT NULL,
    metadata     JSONB NOT NULL DEFAULT '{}',
    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (tenant_id, memory_id)
);

CREATE INDEX IF NOT EXISTS idx_episodic_tenant_updated
    ON episodic_memory (tenant_id, updated_at DESC);

-- ---------------------------------------------------------------------
-- Memory Version Ledger
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS memory_version_ledger (
    id           BIGSERIAL PRIMARY KEY,
    tenant_id    VARCHAR(128) NOT NULL,
    memory_id    VARCHAR(128) NOT NULL,
    version      BIGINT NOT NULL,
    change_type  VARCHAR(32) NOT NULL,
    snapshot     TEXT NOT NULL,
    recorded_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_ledger_lookup
    ON memory_version_ledger (tenant_id, memory_id, version DESC);

-- ---------------------------------------------------------------------
-- Reflection History
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS reflection_history (
    tenant_id        VARCHAR(128) NOT NULL,
    organization_id  VARCHAR(128) NOT NULL,
    execution_id     VARCHAR(128) NOT NULL,
    request_id       VARCHAR(128) NOT NULL,
    verdict          VARCHAR(32) NOT NULL,
    score            DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    importance_score INTEGER NOT NULL DEFAULT 0,
    lessons          JSONB NOT NULL DEFAULT '[]',
    root_cause       TEXT,
    retry_advised    BOOLEAN NOT NULL DEFAULT FALSE,
    evaluated_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (tenant_id, execution_id)
);

CREATE INDEX IF NOT EXISTS idx_reflection_tenant_verdict
    ON reflection_history (tenant_id, verdict, evaluated_at DESC);

-- ---------------------------------------------------------------------
-- Execution History
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS execution_history (
    tenant_id        VARCHAR(128) NOT NULL,
    organization_id  VARCHAR(128) NOT NULL,
    execution_id     VARCHAR(128) NOT NULL,
    request_id       VARCHAR(128) NOT NULL,
    status           VARCHAR(32) NOT NULL,
    capability       VARCHAR(64),
    input            TEXT,
    output           TEXT,
    confidence       DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    started_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    completed_at     TIMESTAMPTZ,
    metadata         JSONB NOT NULL DEFAULT '{}',
    PRIMARY KEY (tenant_id, execution_id)
);

CREATE INDEX IF NOT EXISTS idx_execution_tenant_status
    ON execution_history (tenant_id, status, completed_at DESC);