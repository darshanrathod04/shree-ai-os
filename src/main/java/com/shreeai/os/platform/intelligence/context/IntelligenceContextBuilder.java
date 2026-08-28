package com.shreeai.os.platform.intelligence.context;

import com.shreeai.os.platform.intelligence.evidence.EvidenceItem;
import com.shreeai.os.platform.intelligence.evidence.EvidenceProvenance;
import com.shreeai.os.platform.intelligence.evidence.EvidenceSource;
import com.shreeai.os.platform.intelligence.evidence.EvidenceType;
import com.shreeai.os.platform.sdk.SDKRequest;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * <b>IntelligenceContextBuilder</b>
 *
 * <p>Adapter that constructs a structured {@link IntelligenceContext} from existing
 * V1 request types without breaking backward compatibility.</p>
 *
 * <p>This is the information-preservation boundary for Phase 1. It converts the
 * lossy {@code String}-based fields of {@link SDKRequest} and the legacy
 * {@code ExecutionRequest} into first-class structured fields (intent, objective,
 * constraints, project profile, evidence items with provenance).</p>
 *
 * <p><b>Ownership:</b> Intelligence Foundation</p>
 * <p><b>Version:</b> 1.0</p>
 */
public final class IntelligenceContextBuilder {

    private IntelligenceContextBuilder() {
        // Static factory only
    }

    /**
     * Builds an IntelligenceContext from an SDKRequest, preserving all structured
     * fields that the SDK request carries.
     *
     * @param request the SDK request (must not be null)
     * @return a new IntelligenceContext
     */
    public static IntelligenceContext fromSdkRequest(SDKRequest request) {
        Objects.requireNonNull(request, "request must not be null");

        String requestId = request.sessionId() != null && !request.sessionId().isBlank()
                ? request.sessionId()
                : "req-" + UUID.randomUUID();

        // Extract structured project evidence from SDK metadata if present.
        // The SDK metadata map is the only structured carrier available in V1;
        // keys are documented in the SDK contract.
        ProjectProfile project = extractProject(request.metadata());
        List<EvidenceItem> evidence = extractEvidence(request.metadata(), requestId);

        IntelligenceRequest intelligenceRequest = IntelligenceRequest.builder(requestId, request.message())
                .intent(extractString(request.metadata(), "intent"))
                .objective(extractString(request.metadata(), "objective"))
                .constraints(extractMap(request.metadata(), "constraints"))
                .userId(request.userId())
                .sessionId(request.sessionId())
                .project(project)
                .evidence(evidence)
                .metadata(request.metadata())
                .requestedAt(Instant.now())
                .build();

        return IntelligenceContext.builder("ctx-" + requestId, intelligenceRequest)
                .project(project)
                .evidence(evidence)
                .metadata(Map.of("source", "SDK_REQUEST"))
                .createdAt(Instant.now())
                .build();
    }

    /**
     * Builds an IntelligenceContext from a legacy ExecutionRequest, preserving the
     * request ID, user input, and any structured metadata carried by the execution.
     *
     * @param requestId the execution request identifier
     * @param userInput the user input text
     * @param metadata the execution metadata custom values (may be null)
     * @return a new IntelligenceContext
     */
    public static IntelligenceContext fromExecution(
            String requestId,
            String userInput,
            Map<String, Object> metadata) {
        Objects.requireNonNull(requestId, "requestId must not be null");
        Objects.requireNonNull(userInput, "userInput must not be null");

        Map<String, Object> safeMetadata = metadata != null ? Map.copyOf(metadata) : Map.of();
        ProjectProfile project = extractProject(safeMetadata);
        List<EvidenceItem> evidence = extractEvidence(safeMetadata, requestId);

        IntelligenceRequest intelligenceRequest = IntelligenceRequest.builder(requestId, userInput)
                .intent(extractString(safeMetadata, "intent"))
                .objective(extractString(safeMetadata, "objective"))
                .constraints(extractMap(safeMetadata, "constraints"))
                .sessionId(extractString(safeMetadata, "sessionId"))
                .userId(extractString(safeMetadata, "userId"))
                .project(project)
                .evidence(evidence)
                .metadata(safeMetadata)
                .requestedAt(Instant.now())
                .build();

        return IntelligenceContext.builder("ctx-" + requestId, intelligenceRequest)
                .project(project)
                .evidence(evidence)
                .metadata(Map.of("source", "EXECUTION_REQUEST"))
                .createdAt(Instant.now())
                .build();
    }

    // ========================================================================
    // Project extraction
    // ========================================================================

    /**
     * Extracts a structured ProjectProfile from a metadata map.
     *
     * <p>Supported keys (documented SDK contract):</p>
     * <ul>
     *   <li>{@code projectName} — String</li>
     *   <li>{@code projectId} — String</li>
     *   <li>{@code technologies} — List<String></li>
     *   <li>{@code totalFiles} — Number</li>
     *   <li>{@code sourceFiles} — Number</li>
     *   <li>{@code testFiles} — Number</li>
     *   <li>{@code configurationFiles} — Number</li>
     *   <li>{@code documentationFiles} — Number</li>
     *   <li>{@code layers} — List<String></li>
     *   <li>{@code importantFiles} — List<String></li>
     * </ul>
     *
     * @param metadata the metadata map
     * @return a ProjectProfile, or {@code null} if no projectName is present
     */
    @SuppressWarnings("unchecked")
    public static ProjectProfile extractProject(Map<String, Object> metadata) {
        if (metadata == null) {
            return null;
        }
        Object nameValue = metadata.get("projectName");
        if (!(nameValue instanceof String projectName) || projectName.isBlank()) {
            return null;
        }

        ProjectProfile.Builder builder = ProjectProfile.builder(projectName);
        Object projectId = metadata.get("projectId");
        if (projectId instanceof String pid && !pid.isBlank()) {
            builder.projectId(pid);
        }
        Object technologies = metadata.get("technologies");
        if (technologies instanceof List<?> techList) {
            builder.technologies(techList.stream()
                    .filter(String.class::isInstance)
                    .map(String.class::cast)
                    .toList());
        }
        builder.totalFiles(extractInt(metadata, "totalFiles"));
        builder.sourceFiles(extractInt(metadata, "sourceFiles"));
        builder.testFiles(extractInt(metadata, "testFiles"));
        builder.configurationFiles(extractInt(metadata, "configurationFiles"));
        builder.documentationFiles(extractInt(metadata, "documentationFiles"));
        Object layers = metadata.get("layers");
        if (layers instanceof List<?> layerList) {
            builder.layers(layerList.stream()
                    .filter(String.class::isInstance)
                    .map(String.class::cast)
                    .toList());
        }
        Object importantFiles = metadata.get("importantFiles");
        if (importantFiles instanceof List<?> fileList) {
            builder.importantFiles(fileList.stream()
                    .filter(String.class::isInstance)
                    .map(String.class::cast)
                    .toList());
        }
        return builder.build();
    }

    // ========================================================================
    // Evidence extraction
    // ========================================================================

    /**
     * Extracts evidence items from a metadata map.
     *
     * <p>Supported keys (documented SDK contract):</p>
     * <ul>
     *   <li>{@code evidence} — List of maps, each with keys:
     *       {@code id}, {@code type} (EvidenceType name), {@code value},
     *       {@code sourceType}, {@code sourceId}, {@code confidence},
     *       {@code scope}</li>
     * </ul>
     *
     * @param metadata the metadata map
     * @param requestId the request identifier used for provenance
     * @return the extracted evidence items (may be empty)
     */
    @SuppressWarnings("unchecked")
    public static List<EvidenceItem> extractEvidence(Map<String, Object> metadata, String requestId) {
        if (metadata == null) {
            return List.of();
        }
        Object evidenceValue = metadata.get("evidence");
        if (!(evidenceValue instanceof List<?> evidenceList)) {
            return List.of();
        }

        List<EvidenceItem> items = new ArrayList<>();
        Instant now = Instant.now();
        for (Object entry : evidenceList) {
            if (!(entry instanceof Map<?, ?> rawMap)) {
                continue;
            }
            Map<String, Object> map = (Map<String, Object>) rawMap;
            Object idValue = map.get("id");
            Object valueValue = map.get("value");
            if (!(idValue instanceof String id) || id.isBlank()) {
                continue;
            }
            if (!(valueValue instanceof String value)) {
                continue;
            }

            EvidenceType type = parseType(map.get("type"));
            String sourceTypeName = map.get("sourceType") instanceof String st ? st : type.name();
            String sourceId = map.get("sourceId") instanceof String sid ? sid : requestId;
            double confidence = map.get("confidence") instanceof Number n ? n.doubleValue() : 0.5;
            String scope = map.get("scope") instanceof String s ? s : null;

            EvidenceSource source = new EvidenceSource(type, sourceId, null);
            EvidenceProvenance provenance = new EvidenceProvenance(
                    sourceTypeName, sourceId, null, now, "SDK_METADATA_EXTRACTION", null);

            items.add(new EvidenceItem(
                    id, type, value, source, provenance, confidence, now,
                    null, null, null, scope, List.of(), Map.of()
            ));
        }
        return List.copyOf(items);
    }

    // ========================================================================
    // Helpers
    // ========================================================================

    private static EvidenceType parseType(Object value) {
        if (value instanceof EvidenceType type) {
            return type;
        }
        if (value instanceof String name) {
            try {
                return EvidenceType.valueOf(name);
            } catch (IllegalArgumentException ignored) {
                return EvidenceType.OBSERVATION;
            }
        }
        return EvidenceType.OBSERVATION;
    }

    private static String extractString(Map<String, Object> metadata, String key) {
        Object value = metadata.get(key);
        return value instanceof String s && !s.isBlank() ? s : null;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> extractMap(Map<String, Object> metadata, String key) {
        Object value = metadata.get(key);
        if (value instanceof Map<?, ?> map) {
            return Map.copyOf((Map<String, Object>) map);
        }
        return Map.of();
    }

    private static int extractInt(Map<String, Object> metadata, String key) {
        Object value = metadata.get(key);
        if (value instanceof Number n) {
            return n.intValue();
        }
        return -1;
    }
}