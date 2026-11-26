package com.shreeai.os.platform.kernels.knowledge.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * <b>SlidingWindowChunker</b>
 *
 * <p>Token/word-aware overlapping text chunker for Knowledge Ingestion (K2).
 * Preserves short sections verbatim (zero overhead for small docs) and splits
 * long sections into overlapping windows so embedding models (e.g., 384-dim
 * {@code all-MiniLM-L6-v2}) never truncate trailing context.</p>
 */
public final class SlidingWindowChunker {

    public static final int DEFAULT_MAX_WORDS = 180;
    public static final int DEFAULT_OVERLAP_WORDS = 35;

    private final int maxWords;
    private final int overlapWords;

    public SlidingWindowChunker() {
        this(DEFAULT_MAX_WORDS, DEFAULT_OVERLAP_WORDS);
    }

    public SlidingWindowChunker(int maxWords, int overlapWords) {
        if (maxWords < 10) {
            throw new IllegalArgumentException("maxWords must be >= 10");
        }
        if (overlapWords < 0 || overlapWords >= maxWords) {
            throw new IllegalArgumentException("overlapWords must be in [0, maxWords)");
        }
        this.maxWords = maxWords;
        this.overlapWords = overlapWords;
    }

    /**
     * Splits {@code text} into deterministic overlapping chunks. If the input
     * contains {@code <= maxWords} whitespace-delimited words, returns a
     * single-element list containing the original text verbatim.
     */
    public List<String> chunk(String text) {
        Objects.requireNonNull(text, "text must not be null");
        String trimmed = text.trim();
        if (trimmed.isEmpty()) {
            return List.of();
        }

        String[] words = trimmed.split("\\s+");
        if (words.length <= maxWords) {
            return List.of(text);
        }

        List<String> chunks = new ArrayList<>();
        int step = maxWords - overlapWords;
        for (int start = 0; start < words.length; start += step) {
            int end = Math.min(words.length, start + maxWords);
            StringBuilder sb = new StringBuilder();
            for (int i = start; i < end; i++) {
                if (i > start) {
                    sb.append(' ');
                }
                sb.append(words[i]);
            }
            chunks.add(sb.toString());
            if (end == words.length) {
                break;
            }
        }
        return List.copyOf(chunks);
    }
}
