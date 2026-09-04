package com.shreeai.os.platform.kernels.knowledge.chunking;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Sentence-boundary-aware sliding window document chunker.
 */
public final class DocumentChunker {

    private final int targetChunkSizeChars;
    private final int overlapChars;

    // Sentence splitting regex (., !, ?, newline aware)
    private static final Pattern SENTENCE_PATTERN =
            Pattern.compile("[^.!?\\n]+[.!?\\n]?", Pattern.MULTILINE);

    public DocumentChunker() {
        this(600, 100); // Default ~150-200 words per chunk with ~25-30 words overlap
    }

    public DocumentChunker(int targetChunkSizeChars, int overlapChars) {
        if (overlapChars >= targetChunkSizeChars) {
            throw new IllegalArgumentException("Overlap must be smaller than target chunk size");
        }
        this.targetChunkSizeChars = targetChunkSizeChars;
        this.overlapChars = overlapChars;
    }

    public record TextChunk(int index, String text, int startChar, int endChar) {}

    public List<TextChunk> chunk(String fullText) {
        List<TextChunk> chunks = new ArrayList<>();
        if (fullText == null || fullText.isBlank()) {
            return chunks;
        }

        String cleaned = fullText.strip();
        if (cleaned.length() <= targetChunkSizeChars) {
            chunks.add(new TextChunk(0, cleaned, 0, cleaned.length()));
            return chunks;
        }

        // Split text into semantic sentences
        List<String> sentences = extractSentences(cleaned);

        StringBuilder currentChunk = new StringBuilder();
        int chunkIndex = 0;
        int currentStartPos = 0;

        for (int i = 0; i < sentences.size(); i++) {
            String sentence = sentences.get(i);

            if (currentChunk.length() + sentence.length() > targetChunkSizeChars && currentChunk.length() > 0) {
                String chunkText = currentChunk.toString().strip();
                int endPos = currentStartPos + chunkText.length();
                chunks.add(new TextChunk(chunkIndex++, chunkText, currentStartPos, endPos));

                // Sliding window overlap calculation
                String overlapPrefix = computeOverlap(chunkText, overlapChars);
                currentChunk = new StringBuilder();
                if (!overlapPrefix.isBlank()) {
                    currentChunk.append(overlapPrefix).append(" ");
                }
                currentStartPos = endPos - overlapPrefix.length();
            }

            currentChunk.append(sentence).append(" ");
        }

        if (!currentChunk.toString().isBlank()) {
            String finalChunk = currentChunk.toString().strip();
            chunks.add(new TextChunk(chunkIndex, finalChunk, currentStartPos, currentStartPos + finalChunk.length()));
        }

        return chunks;
    }

    private List<String> extractSentences(String text) {
        List<String> sentences = new ArrayList<>();
        Matcher matcher = SENTENCE_PATTERN.matcher(text);
        while (matcher.find()) {
            String s = matcher.group().strip();
            if (!s.isBlank()) {
                sentences.add(s);
            }
        }
        if (sentences.isEmpty()) {
            sentences.add(text);
        }
        return sentences;
    }

    private String computeOverlap(String text, int targetOverlap) {
        if (text.length() <= targetOverlap) {
            return text;
        }
        // Break on whitespace to avoid cutting words midway
        int cutIndex = text.length() - targetOverlap;
        int spaceIndex = text.indexOf(' ', cutIndex);
        if (spaceIndex != -1 && spaceIndex < text.length()) {
            return text.substring(spaceIndex + 1);
        }
        return text.substring(cutIndex);
    }
}