package com.shreeai.os.platform.resolver;

import com.shreeai.os.platform.gateway.GatewayRequest;
import com.shreeai.os.platform.sdk.SDKRequest;

/**
 * <b>CapabilityResolver</b>
 *
 * <p>Decides WHICH capabilities a request requires and produces a
 * {@link CapabilityPlan}.</p>
 *
 * <p><b>Contract:</b></p>
 * <ul>
 *   <li>Never executes capabilities.</li>
 *   <li>Never calls the runtime.</li>
 *   <li>Never executes kernels.</li>
 *   <li>Pure, deterministic, side-effect-free resolution.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Resolver</p>
 * <p><b>Version:</b> 1.0</p>
 */
public interface CapabilityResolver {

    /**
     * Resolves the capabilities required for an SDK request.
     *
     * @param request the SDK request (must not be null)
     * @return the capability plan for the request
     */
    CapabilityPlan resolve(SDKRequest request);

    /**
     * Resolves the capabilities required for a gateway request.
     *
     * <p>The default implementation delegates to the wrapped
     * {@link SDKRequest}. Implementations may override to enrich the plan
     * with gateway-normalized metadata.</p>
     *
     * @param request the gateway request (must not be null)
     * @return the capability plan for the request
     */
    default CapabilityPlan resolve(GatewayRequest request) {
        return resolve(request.sdkRequest());
    }
}