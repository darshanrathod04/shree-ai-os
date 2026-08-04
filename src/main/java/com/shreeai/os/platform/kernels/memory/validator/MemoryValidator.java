package com.shreeai.os.platform.kernels.memory.validator;

import com.shreeai.os.platform.core.registry.validator.ValidationResult;
import com.shreeai.os.platform.kernels.identity.model.IdentityId;
import com.shreeai.os.platform.kernels.memory.model.CreateMemoryRequest;
import com.shreeai.os.platform.kernels.memory.model.Memory;
import com.shreeai.os.platform.kernels.memory.model.MemoryContent;
import com.shreeai.os.platform.kernels.memory.model.MemoryId;
import com.shreeai.os.platform.kernels.memory.model.MemoryMetadata;
import com.shreeai.os.platform.kernels.memory.model.MemoryStatistics;
import com.shreeai.os.platform.kernels.memory.model.UpdateMemoryRequest;

/**
 * <b>MemoryValidator</b>
 *
 * <p>Stateless validator that ensures every Memory model satisfies the architectural
 * requirements before being used by the Memory Engine.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Validates that Memory models meet all structural requirements.</li>
 *   <li>Answers the question: "Is this Memory model valid?" — it never stores or searches memories.</li>
 *   <li>Returns structured {@link ValidationResult} supporting multiple errors in a single execution.</li>
 *   <li>Enforces the architectural invariants defined in ADD-201, ADD-104, ADD-105, ADD-106.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Memory Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Design Constraints:</b></p>
 * <ul>
 *   <li>Stateless — all state is passed as method parameters.</li>
 *   <li>Deterministic — same inputs always produce the same result.</li>
 *   <li>No business logic — validation rules only.</li>
 *   <li>No model mutation — models are never modified.</li>
 *   <li>No memory storage — never stores, searches, or indexes memories.</li>
 *   <li>No external access — never accesses Registry, Lifecycle, Event Bus, Configuration, or databases.</li>
 *   <li>No AI logic — never performs reasoning, decisions, or LLM communication.</li>
 *   <li>No logging or events — pure validation only.</li>
 * </ul>
 *
 * <p><b>Constitutional Authority:</b> ADD-201, ADD-104, ADD-105, ADD-106</p>
 *
 * @see ValidationResult
 * @see MemoryId
 * @see Memory
 * @see MemoryContent
 * @see MemoryMetadata
 * @see MemoryStatistics
 * @see CreateMemoryRequest
 * @see UpdateMemoryRequest
 */
public final class MemoryValidator {

    /**
     * Constructs a new {@code MemoryValidator}.
     * Public to allow test instantiation.
     */
    public MemoryValidator() {
    }

    // -----------------------------------------------------------------------
    // MemoryId
    // -----------------------------------------------------------------------

    /**
     * Validates a {@link MemoryId}.
     *
     * <p>Validation rules:</p>
     * <ul>
     *   <li>MemoryId must not be null</li>
     *   <li>Value must not be null or blank</li>
     * </ul>
     *
     * @param memoryId the memory identifier to validate
     * @return a {@link ValidationResult} containing any errors or warnings
     * @throws NullPointerException if {@code memoryId} is null
     */
    public static ValidationResult validateMemoryId(MemoryId memoryId) {
        if (memoryId == null) {
            throw new NullPointerException("MemoryId must not be null");
        }

        ValidationResult.Builder builder = ValidationResult.builder();

        String value = memoryId.value();
        if (value == null || value.isBlank()) {
            builder.addError("MemoryId value must not be null or blank");
        }

        return builder.build();
    }

    // -----------------------------------------------------------------------
    // Memory
    // -----------------------------------------------------------------------

    /**
     * Validates a {@link Memory}.
     *
     * <p>Validation rules:</p>
     * <ul>
     *   <li>Memory must not be null</li>
     *   <li>Id must not be null</li>
     *   <li>Content must not be null</li>
     *   <li>Metadata must not be null</li>
     *   <li>CreatedAt must not be null</li>
     *   <li>UpdatedAt must not be null</li>
     * </ul>
     *
     * @param memory the memory to validate
     * @return a {@link ValidationResult} containing any errors or warnings
     * @throws NullPointerException if {@code memory} is null
     */
    public static ValidationResult validateMemory(Memory memory) {
        if (memory == null) {
            throw new NullPointerException("Memory must not be null");
        }

        ValidationResult.Builder builder = ValidationResult.builder();

        // Id must not be null
        MemoryId id = memory.id();
        if (id == null) {
            builder.addError("Memory id must not be null");
        }

        // Content must not be null
        MemoryContent content = memory.content();
        if (content == null) {
            builder.addError("Memory content must not be null");
        }

        // Metadata must not be null
        MemoryMetadata metadata = memory.metadata();
        if (metadata == null) {
            builder.addError("Memory metadata must not be null");
        }

        // CreatedAt must not be null
        if (memory.createdAt() == null) {
            builder.addError("Memory createdAt must not be null");
        }

        // UpdatedAt must not be null
        if (memory.updatedAt() == null) {
            builder.addError("Memory updatedAt must not be null");
        }

        // Recurse into id if present
        if (id != null) {
            ValidationResult idResult = validateMemoryId(id);
            if (!idResult.isValid()) {
                for (String error : idResult.errors()) {
                    builder.addError("Memory id: " + error);
                }
            }
        }

        // Recurse into content if present
        if (content != null) {
            ValidationResult contentResult = validateContent(content);
            if (!contentResult.isValid()) {
                for (String error : contentResult.errors()) {
                    builder.addError("Memory content: " + error);
                }
            }
        }

        // Recurse into metadata if present
        if (metadata != null) {
            ValidationResult metadataResult = validateMetadata(metadata);
            if (!metadataResult.isValid()) {
                for (String error : metadataResult.errors()) {
                    builder.addError("Memory metadata: " + error);
                }
            }
        }

        return builder.build();
    }

    // -----------------------------------------------------------------------
    // MemoryContent
    // -----------------------------------------------------------------------

    /**
     * Validates a {@link MemoryContent}.
     *
     * <p>Validation rules:</p>
     * <ul>
     *   <li>Content must not be null</li>
     *   <li>Text must not be null or blank</li>
     *   <li>Metadata map must not be null</li>
     *   <li>CreatedAt must not be null</li>
     * </ul>
     *
     * @param content the memory content to validate
     * @return a {@link ValidationResult} containing any errors or warnings
     * @throws NullPointerException if {@code content} is null
     */
    public static ValidationResult validateContent(MemoryContent content) {
        if (content == null) {
            throw new NullPointerException("MemoryContent must not be null");
        }

        ValidationResult.Builder builder = ValidationResult.builder();

        // Text must not be null or blank
        String text = content.text();
        if (text == null || text.isBlank()) {
            builder.addError("MemoryContent text must not be null or blank");
        }

        // Metadata map must not be null
        if (content.metadata() == null) {
            builder.addError("MemoryContent metadata map must not be null");
        }

        // CreatedAt must not be null
        if (content.createdAt() == null) {
            builder.addError("MemoryContent createdAt must not be null");
        }

        return builder.build();
    }

    // -----------------------------------------------------------------------
    // MemoryMetadata
    // -----------------------------------------------------------------------

    /**
     * Validates a {@link MemoryMetadata}.
     *
     * <p>Validation rules:</p>
     * <ul>
     *   <li>Metadata must not be null</li>
     *   <li>MemoryId must not be null</li>
     *   <li>Type must not be null</li>
     *   <li>Status must not be null</li>
     *   <li>Visibility must not be null</li>
     *   <li>Owner must not be null</li>
     *   <li>Tags must not be null</li>
     *   <li>CreatedAt must not be null</li>
     *   <li>UpdatedAt must not be null</li>
     *   <li>AccessedAt must not be null</li>
     * </ul>
     *
     * @param metadata the memory metadata to validate
     * @return a {@link ValidationResult} containing any errors or warnings
     * @throws NullPointerException if {@code metadata} is null
     */
    public static ValidationResult validateMetadata(MemoryMetadata metadata) {
        if (metadata == null) {
            throw new NullPointerException("MemoryMetadata must not be null");
        }

        ValidationResult.Builder builder = ValidationResult.builder();

        // MemoryId must not be null
        if (metadata.memoryId() == null) {
            builder.addError("MemoryMetadata memoryId must not be null");
        }

        // Type must not be null
        if (metadata.type() == null) {
            builder.addError("MemoryMetadata type must not be null");
        }

        // Status must not be null
        if (metadata.status() == null) {
            builder.addError("MemoryMetadata status must not be null");
        }

        // Visibility must not be null
        if (metadata.visibility() == null) {
            builder.addError("MemoryMetadata visibility must not be null");
        }

        // Owner must not be null
        IdentityId owner = metadata.owner();
        if (owner == null) {
            builder.addError("MemoryMetadata owner must not be null");
        }

        // Tags must not be null
        if (metadata.tags() == null) {
            builder.addError("MemoryMetadata tags must not be null");
        }

        // CreatedAt must not be null
        if (metadata.createdAt() == null) {
            builder.addError("MemoryMetadata createdAt must not be null");
        }

        // UpdatedAt must not be null
        if (metadata.updatedAt() == null) {
            builder.addError("MemoryMetadata updatedAt must not be null");
        }

        // AccessedAt must not be null
        if (metadata.accessedAt() == null) {
            builder.addError("MemoryMetadata accessedAt must not be null");
        }

        return builder.build();
    }

    // -----------------------------------------------------------------------
    // CreateMemoryRequest
    // -----------------------------------------------------------------------

    /**
     * Validates a {@link CreateMemoryRequest}.
     *
     * <p>Validation rules:</p>
     * <ul>
     *   <li>Request must not be null</li>
     *   <li>Content must not be null</li>
     *   <li>Metadata must not be null</li>
     *   <li>CreatedAt must not be null</li>
     * </ul>
     *
     * @param request the create memory request to validate
     * @return a {@link ValidationResult} containing any errors or warnings
     * @throws NullPointerException if {@code request} is null
     */
    public static ValidationResult validateCreateRequest(CreateMemoryRequest request) {
        if (request == null) {
            throw new NullPointerException("CreateMemoryRequest must not be null");
        }

        ValidationResult.Builder builder = ValidationResult.builder();

        // Content must not be null
        MemoryContent content = request.content();
        if (content == null) {
            builder.addError("CreateMemoryRequest content must not be null");
        }

        // Metadata must not be null
        MemoryMetadata metadata = request.metadata();
        if (metadata == null) {
            builder.addError("CreateMemoryRequest metadata must not be null");
        }

        // CreatedAt must not be null
        if (request.createdAt() == null) {
            builder.addError("CreateMemoryRequest createdAt must not be null");
        }

        // Recurse into content if present
        if (content != null) {
            ValidationResult contentResult = validateContent(content);
            if (!contentResult.isValid()) {
                for (String error : contentResult.errors()) {
                    builder.addError("CreateMemoryRequest content: " + error);
                }
            }
        }

        // Recurse into metadata if present
        if (metadata != null) {
            ValidationResult metadataResult = validateMetadata(metadata);
            if (!metadataResult.isValid()) {
                for (String error : metadataResult.errors()) {
                    builder.addError("CreateMemoryRequest metadata: " + error);
                }
            }
        }

        return builder.build();
    }

    // -----------------------------------------------------------------------
    // UpdateMemoryRequest
    // -----------------------------------------------------------------------

    /**
     * Validates an {@link UpdateMemoryRequest}.
     *
     * <p>Validation rules:</p>
     * <ul>
     *   <li>Request must not be null</li>
     *   <li>MemoryId must not be null</li>
     *   <li>UpdatedAt must not be null</li>
     *   <li>If content is present, it must be valid</li>
     *   <li>If metadata is present, it must be valid</li>
     * </ul>
     *
     * @param request the update memory request to validate
     * @return a {@link ValidationResult} containing any errors or warnings
     * @throws NullPointerException if {@code request} is null
     */
    public static ValidationResult validateUpdateRequest(UpdateMemoryRequest request) {
        if (request == null) {
            throw new NullPointerException("UpdateMemoryRequest must not be null");
        }

        ValidationResult.Builder builder = ValidationResult.builder();

        // MemoryId must not be null
        MemoryId memoryId = request.memoryId();
        if (memoryId == null) {
            builder.addError("UpdateMemoryRequest memoryId must not be null");
        }

        // UpdatedAt must not be null
        if (request.updatedAt() == null) {
            builder.addError("UpdateMemoryRequest updatedAt must not be null");
        }

        // Recurse into memoryId if present
        if (memoryId != null) {
            ValidationResult idResult = validateMemoryId(memoryId);
            if (!idResult.isValid()) {
                for (String error : idResult.errors()) {
                    builder.addError("UpdateMemoryRequest memoryId: " + error);
                }
            }
        }

        // If content is present, validate it
        MemoryContent content = request.content();
        if (content != null) {
            ValidationResult contentResult = validateContent(content);
            if (!contentResult.isValid()) {
                for (String error : contentResult.errors()) {
                    builder.addError("UpdateMemoryRequest content: " + error);
                }
            }
        }

        // If metadata is present, validate it
        MemoryMetadata metadata = request.metadata();
        if (metadata != null) {
            ValidationResult metadataResult = validateMetadata(metadata);
            if (!metadataResult.isValid()) {
                for (String error : metadataResult.errors()) {
                    builder.addError("UpdateMemoryRequest metadata: " + error);
                }
            }
        }

        return builder.build();
    }

    // -----------------------------------------------------------------------
    // MemoryStatistics
    // -----------------------------------------------------------------------

    /**
     * Validates a {@link MemoryStatistics}.
     *
     * <p>Validation rules:</p>
     * <ul>
     *   <li>Statistics must not be null</li>
     *   <li>Total memories count must be non-negative</li>
     *   <li>Active memories count must be non-negative</li>
     *   <li>Archived memories count must be non-negative</li>
     *   <li>Memories by type map must not be null</li>
     *   <li>Memories by status map must not be null</li>
     *   <li>Total access count must be non-negative</li>
     *   <li>Average importance must be between 0.0 and 1.0</li>
     *   <li>Average confidence must be between 0.0 and 1.0</li>
     *   <li>Last updated must not be null</li>
     * </ul>
     *
     * @param statistics the memory statistics to validate
     * @return a {@link ValidationResult} containing any errors or warnings
     * @throws NullPointerException if {@code statistics} is null
     */
    public static ValidationResult validateStatistics(MemoryStatistics statistics) {
        if (statistics == null) {
            throw new NullPointerException("MemoryStatistics must not be null");
        }

        ValidationResult.Builder builder = ValidationResult.builder();

        // Total memories must be >= 0
        if (statistics.totalMemories() < 0) {
            builder.addError("MemoryStatistics totalMemories must be non-negative");
        }

        // Active memories must be >= 0
        if (statistics.activeMemories() < 0) {
            builder.addError("MemoryStatistics activeMemories must be non-negative");
        }

        // Archived memories must be >= 0
        if (statistics.archivedMemories() < 0) {
            builder.addError("MemoryStatistics archivedMemories must be non-negative");
        }

        // Memories by type map must not be null
        if (statistics.memoriesByType() == null) {
            builder.addError("MemoryStatistics memoriesByType map must not be null");
        }

        // Memories by status map must not be null
        if (statistics.memoriesByStatus() == null) {
            builder.addError("MemoryStatistics memoriesByStatus map must not be null");
        }

        // Total access count must be >= 0
        if (statistics.totalAccessCount() < 0) {
            builder.addError("MemoryStatistics totalAccessCount must be non-negative");
        }

        // Average importance must be between 0.0 and 1.0
        double avgImportance = statistics.averageImportance();
        if (avgImportance < 0.0 || avgImportance > 1.0) {
            builder.addError("MemoryStatistics averageImportance must be between 0.0 and 1.0");
        }

        // Average confidence must be between 0.0 and 1.0
        double avgConfidence = statistics.averageConfidence();
        if (avgConfidence < 0.0 || avgConfidence > 1.0) {
            builder.addError("MemoryStatistics averageConfidence must be between 0.0 and 1.0");
        }

        // Last updated must not be null
        if (statistics.lastUpdated() == null) {
            builder.addError("MemoryStatistics lastUpdated must not be null");
        }

        return builder.build();
    }
}