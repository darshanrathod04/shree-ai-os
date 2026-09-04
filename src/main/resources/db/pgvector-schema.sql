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
    content_tsv       tsvector,
    document_id       TEXT,
    tenant_id         TEXT,
    embedding_version TEXT,
    metadata          JSONB,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Approximate nearest-neighbour index for cosine KNN (PgVectorSearchEngine).
CREATE INDEX IF NOT EXISTS shree_vector_memory_embedding_idx
    ON shree_vector_memory USING hnsw (embedding vector_cosine_ops);

-- GIN index over the tsvector column for full-text search (hybrid search).
CREATE INDEX IF NOT EXISTS shree_vector_memory_tsv_idx
    ON shree_vector_memory USING gin (content_tsv);

-- Backfill: keep content_tsv in sync with content for both new and existing rows.
-- Trigger fires on INSERT/UPDATE; for existing rows the DDL below performs a one-time UPDATE.
CREATE OR REPLACE FUNCTION shree_vector_memory_tsv_update() RETURNS trigger AS $$
BEGIN
    NEW.content_tsv := to_tsvector('english', coalesce(NEW.content, ''));
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS shree_vector_memory_tsv_trigger ON shree_vector_memory;
CREATE TRIGGER shree_vector_memory_tsv_trigger
    BEFORE INSERT OR UPDATE OF content
    ON shree_vector_memory
    FOR EACH ROW EXECUTE FUNCTION shree_vector_memory_tsv_update();

-- One-time backfill for rows inserted before the trigger existed.
UPDATE shree_vector_memory
   SET content_tsv = to_tsvector('english', content)
 WHERE content_tsv IS NULL;

-- Embedding repository: persisted embeddings keyed by owner id.
CREATE TABLE IF NOT EXISTS shree_embedding (
    owner_id          TEXT PRIMARY KEY,
    embedding         vector(256),
    embedding_version TEXT,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now()
);
