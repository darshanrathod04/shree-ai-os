package com.shreeai.os.platform.legacy.memory;

import com.shreeai.os.platform.runtime.embedding.EmbeddingProvider;
import com.shreeai.os.platform.runtime.embedding.LocalDeterministicEmbedder;
import org.springframework.stereotype.Component;

/**
 * Legacy embedder — migrated to the canonical embedding subsystem by
 * promote-and-delegate (Constitutional Rule R2). The canonical
 * {@link LocalDeterministicEmbedder} is the single source of embedding logic;
 * no logic is duplicated here.
 */
@Component
public class MemoryEmbedder {

    private final EmbeddingProvider delegate = new LocalDeterministicEmbedder();

    public double[] embed(String text) {
        return delegate.embed(text);
    }
}
