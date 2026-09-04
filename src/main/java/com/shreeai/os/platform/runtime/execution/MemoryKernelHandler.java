package com.shreeai.os.platform.runtime.execution;

import com.shreeai.os.platform.kernels.knowledge.engine.QueryNormalizer;
import com.shreeai.os.platform.kernels.memory.api.MemorySearchService;
import com.shreeai.os.platform.kernels.memory.model.Memory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * A {@link KernelHandler} that bridges the Memory Kernel's
 * {@link MemorySearchService} to the Runtime execution dispatch layer.
 * Registered for the {@link ExecutionCapability#MEMORY_RECALL} capability,
 * it performs memory search/retrieval and converts results into a
 * {@link RichExecutionResult}.
 *
 * @since 2.1
 */
public final class MemoryKernelHandler implements KernelHandler {

    private final MemorySearchService memorySearchService;

    public MemoryKernelHandler(MemorySearchService memorySearchService) {
        this.memorySearchService = Objects.requireNonNull(
                memorySearchService, "memorySearchService must not be null");
    }

    @Override
    public RichExecutionResult handle(
            ExecutionCapability capability,
            String input,
            Map<String, Object> context) {

        Objects.requireNonNull(capability, "capability must not be null");
        Objects.requireNonNull(context, "context must not be null");

        try {
            String query = input != null && !input.isBlank() ? input : "";

            // Sprint-9: Normalize natural-language queries so "who is darshan"
            // becomes "darshan" before reaching the memory search service.
            // This is the same shared QueryNormalizer used by the Knowledge
            // kernel — ensures consistent recall semantics across kernels.
            String normalized = QueryNormalizer.normalize(query);
            String effectiveQuery = !normalized.isEmpty() ? normalized : query;

            List<Memory> memories = memorySearchService.search(effectiveQuery);

            StringBuilder outputBuilder = new StringBuilder();
            outputBuilder.append("Found ").append(memories.size()).append(" memory result(s).");

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("memoryCount", memories.size());

            if (!memories.isEmpty()) {
                outputBuilder.append("\n\n");
                for (int i = 0; i < memories.size(); i++) {
                    Memory memory = memories.get(i);
                    String content = memory.content().text();
                    outputBuilder.append("[")
                            .append(i + 1)
                            .append("] ")
                            .append(content);
                    if (i < memories.size() - 1) {
                        outputBuilder.append("\n");
                    }
                }
            }

            double confidence = memories.isEmpty() ? 0.1 : Math.min(0.95, 0.7 + 0.05 * memories.size());

            return RichExecutionResult.builder()
                    .capability(capability)
                    .status(ExecutionStatus.SUCCESS)
                    .output(outputBuilder.toString())
                    .confidence(confidence)
                    .metadata(metadata)
                    .build();

        } catch (Exception e) {
            return RichExecutionResult.failure(
                    capability,
                    "Memory recall failed: " + e.getMessage());
        }
    }
}
