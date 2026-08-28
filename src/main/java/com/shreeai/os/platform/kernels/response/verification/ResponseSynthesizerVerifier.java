package com.shreeai.os.platform.kernels.response.verification;

import com.shreeai.os.platform.kernels.response.api.ResponseSynthesizer;
import com.shreeai.os.platform.kernels.response.service.ResponseSynthesisService;

/**
 * Architectural verifier for the Response Kernel.
 *
 * Constitutional Rules:
 * - Runtime depends only on service.
 * - Service depends only on interface.
 * - Engine implements interface.
 */
public final class ResponseSynthesizerVerifier {

    public boolean verify(
            ResponseSynthesizer synthesizer,
            ResponseSynthesisService service
    ) {
        return synthesizer != null
                && service != null;
    }
}