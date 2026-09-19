package com.shreeai.os.platform.gateway;

import com.shreeai.os.platform.sdk.SDKRequest;
import com.shreeai.os.platform.sdk.SDKResponse;

/**
 * Application Gateway interface.
 *
 * <p>Defines the contract for the single entry point between the
 * public SDK and the Runtime. Implementations accept SDK requests,
 * validate and normalize them, then forward to the Runtime.</p>
 */
public interface ApplicationGateway {

    /**
     * Processes an SDK request through the gateway and returns the
     * SDK response.
     *
     * @param request the SDK request
     * @return the SDK response from the runtime
     * @throws GatewayException if the gateway cannot process the request
     */
    SDKResponse handle(SDKRequest request) throws GatewayException;

    /**
     * Returns whether the gateway is ready to accept requests.
     *
     * @return true if the gateway is operational
     */
    boolean isReady();
}
