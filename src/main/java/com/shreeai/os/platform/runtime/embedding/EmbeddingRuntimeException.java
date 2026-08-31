package com.shreeai.os.platform.runtime.embedding;

/**
 * <b>EmbeddingRuntimeException</b>
 *
 * <p>Runtime failure raised by the embedding subsystem (provider or repository).
 * Kernel layers never deal with provider-specific exceptions — all failures
 * are translated to this type at the adapter boundary.</p>
 *
 * <p><b>Ownership:</b> Runtime — Embedding</p>
 * <p><b>Version:</b> 1.0</p>
 * <p><b>Constitutional Authority:</b> PHASE-1-ARCH-001</p>
 */
public class EmbeddingRuntimeException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public EmbeddingRuntimeException(String message) {
        super(message);
    }

    public EmbeddingRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
}
