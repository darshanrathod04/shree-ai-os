package com.shreeai.os.platform.kernels.response.service;

import com.shreeai.os.platform.kernels.response.api.ResponseSynthesizer;
import com.shreeai.os.platform.kernels.response.engine.DefaultResponseSynthesizer;
import com.shreeai.os.platform.kernels.response.model.SynthesizedResponse;
import com.shreeai.os.platform.runtime.pipeline.PipelineContext;
import com.shreeai.os.platform.runtime.pipeline.PipelineExecutionState;

import java.util.Objects;

/**
 * Constitutional Response Synthesis Service.
 *
 * This service is the only Runtime entry point for response generation.
 * It never performs reasoning; it only converts validated pipeline state
 * into a professional user-facing response.
 */
public final class ResponseSynthesisService {

    private final ResponseSynthesizer synthesizer;

    public ResponseSynthesisService() {
        this(new DefaultResponseSynthesizer());
    }

    public ResponseSynthesisService(ResponseSynthesizer synthesizer) {
        this.synthesizer = Objects.requireNonNull(synthesizer);
    }

    public SynthesizedResponse synthesize(
            PipelineContext context,
            PipelineExecutionState state
    ) {
        return synthesizer.synthesize(context, state);
    }
}