/**
 * <b>Runtime — Embedding Subsystem</b>
 *
 * <p>Provider-neutral semantic embedding infrastructure. Deliberately separated
 * from {@code runtime.vector}: embeddings describe <i>how text becomes a
 * vector</i>, while the vector subsystem describes <i>where vectors live and
 * how they are searched</i>. Kernels depend only on the SPIs
 * ({@link com.shreeai.os.platform.runtime.embedding.EmbeddingProvider},
 * {@link com.shreeai.os.platform.runtime.embedding.EmbeddingRepository});
 * concrete providers are selected via configuration and injected by the
 * composition root — never hard-coded.</p>
 *
 * <p><b>Components:</b></p>
 * <pre>
 * ├── EmbeddingProvider            — SPI: text → vector (deterministic, versioned)
 * ├── EmbeddingRepository          — SPI: persisted embeddings keyed by owner id
 * ├── EmbeddedVector               — immutable persisted value (embedding + version)
 * ├── LocalDeterministicEmbedder   — default provider (hashed n-grams, no network)
 * ├── OpenAiCompatibleEmbedder     — optional remote provider (OpenAI-compatible API)
 * ├── InMemoryEmbeddingRepository  — thread-safe default repository
 * └── EmbeddingRuntimeException    — failure translation type
 * </pre>
 *
 * <p><b>Constitutional Authority:</b> PHASE-1-ARCH-001</p>
 */
package com.shreeai.os.platform.runtime.embedding;
