package com.shree.playground.service;

import com.shreeai.os.platform.sdk.SDKResponse;
import com.shreeai.os.platform.kernels.knowledge.engine.QueryNormalizer;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class MemoryService {

    private final Map<String, MemoryRecord> memoryStore = new ConcurrentHashMap<>();

    // =====================================================
    // STORE MEMORY
    // =====================================================

    public SDKResponse store(String title, String content) {

        String id = UUID.randomUUID().toString();

        MemoryRecord record = new MemoryRecord(
                id,
                title,
                content,
                Instant.now()
        );

        memoryStore.put(id, record);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("memoryId", id);
        payload.put("title", title);
        payload.put("createdAt", record.createdAt());

        return SDKResponse.builder()
                .answer("Memory stored successfully.")
                .confidence(0.99)
                .reasoningAvailable(true)
                .metadata("operation:STORE_MEMORY")
                .structuredPayload(payload)
                .timestamp(Instant.now())
                .build();
    }

    // =====================================================
    // SEARCH MEMORY
    // =====================================================

    public SDKResponse search(String query) {

        String normalized = QueryNormalizer.normalize(query);
        String needle = normalized.isEmpty()
                ? (query == null ? "" : query.toLowerCase())
                : normalized;

        List<MemoryRecord> results = memoryStore.values().stream()
                .filter(m ->
                        m.title().toLowerCase().contains(needle) ||
                                m.content().toLowerCase().contains(needle)
                )
                .sorted(Comparator.comparing(MemoryRecord::createdAt).reversed())
                .collect(Collectors.toList());

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("query", query);
        payload.put("count", results.size());
        payload.put("results", results);

        String answer;

        if (results.isEmpty()) {
            answer = "No memory found for \"" + query + "\".";
        } else {
            answer = "Found " + results.size() + " matching memories.";
        }
        // 'answer' is used by the SDK response and is intentionally kept as the
        // human-readable raw query (preserves SDK contract).

        return SDKResponse.builder()
                .answer(answer)
                .confidence(results.isEmpty() ? 0.60 : 0.95)
                .reasoningAvailable(true)
                .metadata("operation:SEARCH_MEMORY")
                .structuredPayload(payload)
                .timestamp(Instant.now())
                .build();
    }

    // =====================================================
    // RECALL MEMORY
    // =====================================================

    public SDKResponse recall(String query) {

        String normalized = QueryNormalizer.normalize(query);
        String needle = normalized.isEmpty()
                ? (query == null ? "" : query.toLowerCase())
                : normalized;

        Optional<MemoryRecord> memory = memoryStore.values().stream()
                .filter(m ->
                        m.title().toLowerCase().contains(needle) ||
                                m.content().toLowerCase().contains(needle)
                )
                .max(Comparator.comparing(MemoryRecord::createdAt));

        if (memory.isEmpty()) {

            return SDKResponse.builder()
                    .answer("I couldn't recall any memory related to \"" + query + "\".")
                    .confidence(0.55)
                    .reasoningAvailable(true)
                    .metadata("operation:RECALL_MEMORY")
                    .structuredPayload(Map.of(
                            "query", query,
                            "memory", null
                    ))
                    .timestamp(Instant.now())
                    .build();
        }

        MemoryRecord m = memory.get();

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("query", query);
        payload.put("memory", m);

        String answer = """
                **%s**
                
                %s
                """.formatted(m.title(), m.content());

        return SDKResponse.builder()
                .answer(answer)
                .confidence(0.97)
                .reasoningAvailable(true)
                .metadata("operation:RECALL_MEMORY")
                .structuredPayload(payload)
                .timestamp(Instant.now())
                .build();
    }

    // =====================================================
    // MEMORY MODEL
    // =====================================================

    private record MemoryRecord(
            String id,
            String title,
            String content,
            Instant createdAt
    ) {
    }
}