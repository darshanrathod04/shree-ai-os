package com.shreeai.os.platform.kernels.chief.engine;

import com.shreeai.os.platform.kernels.chief.api.ChiefService;
import com.shreeai.os.platform.kernels.chief.model.ChiefRequest;
import com.shreeai.os.platform.kernels.chief.model.ChiefResponse;

/**
 * <b>ChiefProcessingEngine</b>
 *
 * <p>Strategic processing contract for the Chief Kernel.
 * This interface defines the contract for orchestration processing.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Defines strategic processing contract.</li>
 *   <li>Provides orchestration processing interface.</li>
 *   <li>No implementation in this interface.</li>
 * </ul>
 *
 * <p><b>Design Principles:</b></p>
 * <ul>
 *   <li>Interface-only — no implementation logic.</li>
 *   <li>Technology-agnostic — no framework dependencies.</li>
 *   <li>Contract-focused — exposes only processing contracts.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Chief Kernel — Engine Layer</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-CHIEF-106, EIO-ARCH-001</p>
 *
 * @see ChiefService
 *
 * @since 1.0
 */
public interface ChiefProcessingEngine {

    /**
     * Processes an orchestration request.
     *
     * <p>This operation processes a validated orchestration request
     * and returns the orchestration response.</p>
     *
     * <p><b>Semantic Boundary:</b> This method defines the contract only.
     * Implementation is provided by DefaultChiefProcessingEngine.</p>
     *
     * @param request the orchestration request (must not be {@code null})
     * @return the orchestration response
     * @throws IllegalArgumentException if request is {@code null}
     */
    ChiefResponse process(ChiefRequest request);
}