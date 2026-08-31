-- =====================================================================
-- PHASE 1 — pgvector schema for Shree AI OS
-- Self-provisioned automatically by PgVectorStoreProvider.ensureSchema()
-- (dimensions injected from shree.embedding.dimensions, default 256).
-- This file is the canonical human-readable reference of that DDL.
-- Requires: CREATE EXTENSION IF NOT EXISTS vector;
-- =====================================================================

CREATE EXTENSION IF NOT EXISTS vector;

-- Vector memory: one row per ingested knowledge chunk.
-- Record id == knowledge node id (O(1) mapping back to the domain node).
CREATE TABLE IF NOT EXISTS shree_vector_memory (
    id                TEXT PRIMARY KEY,
    content           TEXT NOT NULL,
    embedding         vector(256),
    document_id       TEXT,
    tenant_id         TEXT,
    embedding_version TEXT,
    metadata          JSONB,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Approximate nearest-neighbour index for cosine KNN (PgVectorSearchEngine).
CREATE INDEX IF NOT EXISTS shree_vector_memory_embedding_idx
    ON shree_vector_memory USING hnsw (embedding vector_cosine_ops);

-- Embedding repository: persisted embeddings keyed by owner id.
CREATE TABLE IF NOT EXISTS shree_embedding (
    owner_id          TEXT PRIMARY KEY,
    embedding         vector(256),
    embedding_version TEXT,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now()
);
