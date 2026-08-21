package com.shreeai.os.platform.kernels.response.service;

import com.shreeai.os.platform.kernels.response.api.ResponseSynthesizer;
import com.shreeai.os.platform.kernels.response.model.SynthesizedResponse;
import com.shreeai.os.platform.runtime.pipeline.PipelineContext;
import com.shreeai.os.platform.runtime.pipeline.PipelineExecutionState;

import java.util.Objects;

/**
 * ResponseSynthesisService
 *
 * Thin application service that delegates synthesis to the
 * configured ResponseSynthesizer.
 *
 * This preserves dependency inversion:
 *
 * Runtime
 *    ↓
 * Service
 *    ↓
 * ResponseSynthesizer
 */
public final class ResponseSynthesisService {

    private final ResponseSynthesizer synthesizer;

    public ResponseSynthesisService(ResponseSynthesizer synthesizer) {
        this.synthesizer = Objects.requireNonNull(
                synthesizer,
                "ResponseSynthesizer cannot be null"
        );
    }

    public SynthesizedResponse synthesize(
            PipelineContext context,
            PipelineExecutionState state
    ) {
        return synthesizer.synthesize(context, state);
    }
}