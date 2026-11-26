package com.shreeai.os.platform.kernels.chief.service;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.shreeai.os.platform.kernels.chief.error.ChiefError;
import com.shreeai.os.platform.kernels.chief.api.ChiefService;
import com.shreeai.os.platform.kernels.chief.error.ChiefErrorCode;
import com.shreeai.os.platform.kernels.chief.error.ChiefException;
import com.shreeai.os.platform.kernels.chief.error.ChiefValidationException;
import com.shreeai.os.platform.kernels.chief.model.ChiefId;
import com.shreeai.os.platform.kernels.chief.model.ChiefRequest;
import com.shreeai.os.platform.kernels.chief.model.ChiefResponse;
import com.shreeai.os.platform.kernels.chief.model.ChiefMetrics;
import com.shreeai.os.platform.kernels.chief.validation.ChiefValidationResult;
import com.shreeai.os.platform.kernels.chief.validation.ChiefValidator;
import com.shreeai.os.platform.kernels.chief.engine.ChiefProcessingEngine;

/**
 * <b>DefaultChiefService</b>
 *
 * <p>Default implementation of the ChiefService interface.
 * This class coordinates validation and processing while remaining
 * free of strategic decision-making.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Validates incoming requests.</li>
 *   <li>Delegates processing to the engine.</li>
 *   <li>Translates exceptions to canonical hierarchy.</li>
 *   <li>Remains stateless and thread-safe.</li>
 * </ul>
 *
 * <p><b>Design Principles:</b></p>
 * <ul>
 *   <li>Immutable — all dependencies are final.</li>
 *   <li>Constructor injection — no setter or field injection.</li>
 *   <li>Stateless — no mutable fields.</li>
 *   <li>Thread-safe — no shared mutable state.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Chief Kernel — Service Layer</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-CHIEF-105, EIO-ARCH-001</p>
 *
 * @since 1.0
 */
public final class DefaultChiefService implements ChiefService {

    private final ChiefValidator validator;
    private final ChiefProcessingEngine processingEngine;
    private final Map<String, ChiefResponse> orchestrationHistory = new ConcurrentHashMap<>();
    private final Set<String> cancelledRequests = ConcurrentHashMap.newKeySet();
    private final AtomicInteger activeOrchestrations = new AtomicInteger(0);
    private final AtomicInteger completedOrchestrations = new AtomicInteger(0);
    private final AtomicInteger failedOrchestrations = new AtomicInteger(0);

    /**
     * Constructs a {@code DefaultChiefService} with the specified dependencies.
     *
     * @param validator        the chief validator (must not be {@code null})
     * @param processingEngine the chief processing engine (must not be {@code null})
     * @throws IllegalArgumentException if any dependency is {@code null}
     */
    public DefaultChiefService(
            ChiefValidator validator,
            ChiefProcessingEngine processingEngine) {
        this.validator = Objects.requireNonNull(validator, "ChiefValidator must not be null");
        this.processingEngine = Objects.requireNonNull(processingEngine, "ChiefProcessingEngine must not be null");
    }

    @Override
    public ChiefResponse submitOrchestration(ChiefRequest request) {
        Objects.requireNonNull(request, "ChiefRequest must not be null");

        // Validate request
        ChiefValidationResult validationResult = validator.validate(request);
        if (!validationResult.valid()) {
            throw new ChiefValidationException(
                    new ChiefError(
                            ChiefErrorCode.VALIDATION_ERROR,
                            "Invalid orchestration request",
                            "DefaultChiefService",
                            "submitOrchestration",
                            java.util.Map.of("issues", validationResult.issues()),
                            java.time.Instant.now()
                    )
            );
        }

        // Delegate to processing engine with lifecycle tracking
        String requestId = request.chiefId().value();
        activeOrchestrations.incrementAndGet();
        try {
            ChiefResponse response = processingEngine.process(request);
            orchestrationHistory.put(requestId, response);
            completedOrchestrations.incrementAndGet();
            return response;
        } catch (ChiefException e) {
            failedOrchestrations.incrementAndGet();
            throw e;
        } catch (RuntimeException e) {
            failedOrchestrations.incrementAndGet();
            throw new ChiefException(
                    new ChiefError(
                            ChiefErrorCode.ORCHESTRATION_ERROR,
                            "Unexpected orchestration error: " + e.getMessage(),
                            "DefaultChiefService",
                            "submitOrchestration",
                            Map.of(),
                            Instant.now()
                    ),
                    e
            );
        } finally {
            activeOrchestrations.decrementAndGet();
        }
    }

    @Override
    public ChiefResponse getOrchestrationStatus(String requestId) {
        Objects.requireNonNull(requestId, "RequestId must not be null");
        if (requestId.isBlank()) {
            throw new ChiefValidationException(
                    new ChiefError(
                            ChiefErrorCode.VALIDATION_ERROR,
                            "RequestId must not be blank",
                            "DefaultChiefService",
                            "getOrchestrationStatus",
                            Map.of("requestId", requestId),
                            Instant.now()
                    )
            );
        }
        ChiefResponse response = orchestrationHistory.get(requestId);
        if (response == null) {
            throw new ChiefException(
                    new ChiefError(
                            ChiefErrorCode.ORCHESTRATION_ERROR,
                            "No orchestration found for requestId: " + requestId,
                            "DefaultChiefService",
                            "getOrchestrationStatus",
                            Map.of("requestId", requestId, "cancelled", cancelledRequests.contains(requestId)),
                            Instant.now()
                    )
            );
        }
        return response;
    }

    @Override
    public boolean cancelOrchestration(String requestId) {
        Objects.requireNonNull(requestId, "RequestId must not be null");
        if (requestId.isBlank()) {
            return false;
        }
        boolean known = orchestrationHistory.containsKey(requestId);
        cancelledRequests.add(requestId);
        return known;
    }

    @Override
    public ChiefMetrics getChiefHealth() {
        return new ChiefMetrics(
                new ChiefId("chief-service"),
                activeOrchestrations.get(),
                completedOrchestrations.get(),
                failedOrchestrations.get(),
                0,
                Instant.now(),
                Map.of(
                        "status", "HEALTHY",
                        "trackedOrchestrations", orchestrationHistory.size(),
                        "cancelledOrchestrations", cancelledRequests.size()
                )
        );
    }
}