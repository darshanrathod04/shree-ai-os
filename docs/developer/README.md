
# Shree AI OS

**In-Process, Privacy-First Cognitive Runtime & Hybrid RAG Engine**

[![Java 21](https://img.shields.io/badge/Java-21-blue.svg)](https://adoptium.net/)
[![Spring Boot 3](https://img.shields.io/badge/Spring%20Boot-3-green.svg)](https://spring.io/projects/spring-boot)
[![pgvector](https://img.shields.io/badge/pgvector-0.7-blue.svg)](https://github.com/pgvector/pgvector)
[![License: Proprietary](https://img.shields.io/badge/License-Proprietary-red.svg)](./LICENSE)

---

## What is Shree AI OS?

Shree AI OS is an **in-process, privacy-first cognitive runtime** that gives your application a full AI brain - without sending your data to any third-party API. It ships as a standard JVM library you embed directly in your service, providing:

| Capability | Implementation |
|---|---|
| **Semantic Embeddings** | Local ONNX model (`all-MiniLM-L6-v2`), 384-dim, zero-latency inference |
| **Smart Document Chunking** | Sentence-boundary-aware sliding window (600-char target, 80-char overlap) |
| **Hybrid Vector Search** | PostgreSQL + pgvector: HNSW (semantic KNN) + GIN full-text (keyword), fused via **Reciprocal Rank Fusion (RRF)** |
| **Grounded Responses** | Every answer carries citations back to specific ingested chunks |
| **Memory & Knowledge Graph** | In-process episodic memory + entity-relationship knowledge graph |
| **Multi-Agent Orchestration** | Chief-of-staff pattern with typed intent routing |
| **Constitutional Governance** | Approval guardrails, audit logging, and traceable reasoning |

---

## Architecture Overview

```
                        Shree AI OS Runtime
 -------------------------------------------------------------------------
  +----------+   +----------+   +----------+   +--------------------+
  |Identity  |   | Memory   |   |Knowledge |   | Reasoning          |
  |Kernel    |   | Kernel   |   |Kernel    |   | Kernel             |
  |          |   |          |   |          |   |                    |
  | Profiles |   | Episodic |   | Entities |   | Evidence rank      |
  | Tenants  |   | memory   |   | Relations|   | Grounded answer    |
  |          |   | Semantic |   | Graph    |   | Citations          |
  |          |   | search   |   | search   |   |                    |
  +----+-----+   +----+-----+   +----+-----+   +---------+----------+
       |              |             |                  |
       +--------------+-------------+------------------+
                            |
                   +--------+--------+
                   | Canonical Runtime |
                   | 10-stage pipeline |
                   +--------+-----------+
                            |
                   +--------v-----------+
                   | ONNX Embedder     |
                   | all-MiniLM-L6-v2 |
                   | 384 dimensions   |
                   +--------+-----------+
                            |
                   +--------v---------------------------------------------+
                   |   PostgreSQL + pgvector  (Hybrid RRF Search)       |
                   |                                                         |
                   |  +-------------------+    +---------------------+    |
                   |  | HNSW index       | +  | GIN index           |    |
                   |  | (vector KNN)     |    | (tsvector FTS)      |    |
                   |  +-------------------+    +---------------------+    |
                   +--------------------------------------------------------+

**Data flow for a grounded chat query:**

```
User query
    |
    v
+---------------+    +---------------------+
| ONNX Embedder |--->| Hybrid RRF Search   |
| (384-dim vec) |     | (vector + FTS)      |
+-------+-------+    +---------+-----------+
        |                      |
        |          +-----------v-----------+
        |          | Top-K ranked chunks   |
        |          | with citations        |
        |          +-----------+-----------+
        |                      |
        |          +-----------v-----------+
        |          | Reasoning Kernel       |
        |          +-----------+-----------+
        |                      |
        v                      v
       Answer     +-----------------------+
                 | SDKResponse             |
                 | { answer, citations }  |
                 +-----------------------+
```

---

## Key Architecture Highlights

### Hybrid Search (PostgreSQL + pgvector)

Two separate retrieval channels run in parallel and their results are fused with **Reciprocal Rank Fusion**:

```
                 +-- KNN over embedding (HNSW) --> rank_vec (1..20)
query -----------+                                 |
                 +-- Keyword match (GIN FTS) ----> rank_text (1..20)
                                                            |
                                                            v
                    rrf_score = 1/(60 + rank_vec) + 1/(60 + rank_text)
```

- **CTE 1 (vector_matches):** `ROW_NUMBER() OVER (ORDER BY embedding <=> ?::vector) LIMIT 20`
- **CTE 2 (text_matches):** `WHERE content_tsv @@ plainto_tsquery('english', ?) ORDER BY ts_rank_cd(...) LIMIT 20`
- **Fusion:** `FULL OUTER JOIN` on id, `ORDER BY rrf_score DESC LIMIT topK`
- Falls back to pure vector search when no text query is available.

### Smart Chunking (`DocumentChunker`)

Documents are split using a sentence-boundary regex into segments, then reassembled into chunks of **no more than 600 characters** with **80-character overlap**. The overlap breaks on whitespace so words are never cut mid-token. This preserves semantic completeness of sentences while keeping chunks small enough for accurate embedding.

---

## 3-Minute Quickstart

### Prerequisites

| Requirement | Version | Notes |
|---|---|---|
| JDK | 21+ | [Adoptium](https://adoptium.net/) recommended |
| Docker | 24+ | Required for the pgvector database |
| Maven | 3.9+ | Or use the bundled `./mvnw` wrapper |

### Step 1 - Start the Database

```bash
# Boot PostgreSQL 16 + pgvector
docker compose up -d

# Verify it is healthy
docker compose ps
# NAME                  STATUS         PORTS
# shree-postgres-vector  healthy (up)  0.0.0.0:5432->5432/tcp
```

### Step 2 - Configure (Optional)

```bash
# Copy the template and fill in your values
cp .env.example .env

# Or just set the one key you need
# (set SHREE_EMBEDDING_PROVIDER=onnx to skip API keys entirely)
export GEMINI_API_KEY=your_key_here
```

> **Default configuration** already points to the docker-compose database at
> `jdbc:postgresql://localhost:5432/shree` with password `shreeai`.
> No changes needed for a local dev run.

### Step 3 - Run the Playground

```bash
mvn clean spring-boot:run -pl application/shree-playground
```

The app starts on **http://localhost:7070**.

```bash
curl http://localhost:7070/actuator/health
# {"status":"UP"}
```

---

## Ready-to-Use cURL Examples

### Ingest a Document

```bash
curl -s -X POST http://localhost:7070/api/playground/knowledge/ingest \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Understanding Vector Databases",
    "content": "Vector databases store high-dimensional embeddings that capture semantic meaning. pgvector extends PostgreSQL with the vector type and HNSW indexing for fast approximate nearest-neighbour search. Combined with full-text search via tsvector and GIN indexes, hybrid RRF ranking produces more relevant results than either method alone."
  }' | jq .
```

**Example response:**

```json
{
  "status": "SUCCESS",
  "message": "Knowledge ingested successfully",
  "data": {
    "nodeId": "abc-123",
    "title": "Understanding Vector Databases",
    "chunksIndexed": 1,
    "embeddingVersion": "local-onnx-v1"
  }
}
```

### Search the Knowledge Graph

```bash
curl -s -X POST http://localhost:7070/api/playground/knowledge/search \
  -H "Content-Type: application/json" \
  -d '{"query": "How does hybrid RRF search work?"}' | jq .
```

### Chat with Semantic Grounding

```bash
curl -s -X POST http://localhost:7070/api/playground/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "How does Shree AI OS handle vector search?"}' | jq .
```

**Example response structure:**

```json
{
  "status": "SUCCESS",
  "data": {
    "response": "Shree AI OS uses a dual-channel retrieval approach...",
    "citations": [
      {
        "chunkId": "abc-123",
        "title": "Understanding Vector Databases",
        "excerpt": "pgvector extends PostgreSQL with the vector type...",
        "score": 0.952
      }
    ],
    "intent": "KNOWLEDGE_QUERY",
    "confidence": 0.94,
    "traceId": "trace-xyz-789"
  }
}
```

### Store in Memory

```bash
curl -s -X POST http://localhost:7070/api/playground/memory/store \
  -H "Content-Type: application/json" \
  -d '{"title": "Project Meeting Notes", "content": "Discussed Q4 roadmap and migration to pgvector."}' | jq .
```

### Recall from Memory

```bash
curl -s -X POST http://localhost:7070/api/playground/memory/recall \
  -H "Content-Type: application/json" \
  -d '{"query": "What was discussed about pgvector?"}' | jq .
```

---

## Configuration Reference

All settings are in `application/shree-playground/src/main/resources/application.properties`
and can be overridden via environment variables:

| Property | Env Variable | Default | Description |
|---|---|---|---|
| `shree.vector.provider` | `SHREE_VECTOR_PROVIDER` | `pgvector` | `pgvector` or `in-memory` |
| `shree.vector.jdbc.url` | `SHREE_VECTOR_JDBC_URL` | `jdbc:postgresql://localhost:5432/shree` | PostgreSQL JDBC URL |
| `shree.vector.jdbc.user` | `SHREE_VECTOR_JDBC_USER` | `postgres` | Database user |
| `shree.vector.jdbc.password` | `SHREE_VECTOR_JDBC_PASSWORD` | `shreeai` | Database password |
| `shree.embedding.provider` | `SHREE_EMBEDDING_PROVIDER` | `onnx` | `onnx` (local) or `gemini` |
| `shree.embedding.dimensions` | `SHREE_EMBEDDING_DIMENSIONS` | `384` | Embedding vector dimension |

---

## Repository Structure

```
shree-ai-os/
|-- docker-compose.yml           # PostgreSQL + pgvector (one command to start)
|-- .env.example                # Environment variable template
|-- application/
|   +-- shree-playground/       # Spring Boot playground app (port 7070)
|       +-- src/main/resources/
|           +-- application.properties
|-- src/
|   +-- main/java/com/shreeai/os/
|       +-- ShreeAiOsApplication.java
|       +-- platform/
|           +-- core/           # Registry, discovery, lifecycle, events
|           +-- kernels/        # Identity, Memory, Context, Knowledge,
|           |                   # Reasoning, Planning, Execution, Chief
|           +-- runtime/        # Canonical Runtime, Vector Store, Embeddings
|       +-- sdk/                # Public developer SDK
+-- docs/                       # Constitutional + architectural docs
```

---

## Constitutional Rules

All code is governed by five immutable rules:

| Rule | Description |
|---|---|
| **R1** | No canonical code imports `platform.legacy` - enforced by `CanonicalIsolationTest` |
| **R2** | Legacy types migrate by promote-and-delegate; the Runtime is the single source of truth |
| **R3** | Public API surfaces (REST routes, SDK signatures) are frozen until final removal |
| **R4** | A legacy component is removed only when zero canonical imports and zero test dependencies exist |
| **R5** | Every phase ends green: `mvn clean test` passes completely |

---

## Build & Test

```bash
# Compile everything (no tests)
mvn clean compile -DskipTests

# Compile + run unit tests
mvn clean test

# Run only the vector store integration tests (requires Docker)
mvn test -Dtest=PgVectorIntegrationTest
```

> **Note:** `PgVectorIntegrationTest` is automatically skipped when Docker is unavailable,
> so `mvn clean test` is safe to run in any CI environment.

---

## Contributing

See `docs/architecture/ARCHITECTURE_AUDIT.md` and
`docs/architecture/LEGACY_MIGRATION_REPORT.md` for governing architectural documents.

All contributions must:
1. Pass `mvn clean test`
2. Maintain constitutional isolation (R1-R5)
3. Include or update tests for any new behaviour
4. Follow the existing code conventions

---

**Platform:** Shree AI OS
**Version:** 1.0
**Constitutional Authority:** PHASE-1-ARCH-001, EIO-KNW-101
**Founder:** Darshan Rathod
