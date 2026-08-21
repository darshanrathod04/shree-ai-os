package com.shreeai.os.platform.kernels.response.api;

import com.shreeai.os.platform.kernels.response.model.SynthesizedResponse;
import com.shreeai.os.platform.runtime.pipeline.PipelineContext;
import com.shreeai.os.platform.runtime.pipeline.PipelineExecutionState;

/**
 * ResponseSynthesizer
 *
 * Constitutional contract responsible for transforming validated
 * intelligence into a professional end-user response.
 *
 * Responsibilities:
 * - Read pipeline state
 * - Read reasoning/planning outputs
 * - Produce structured response
 *
 * Never:
 * - Perform reasoning
 * - Modify pipeline state
 * - Invent evidence
 */
public interface ResponseSynthesizer {

    /**
     * Synthesizes the final response.
     *
     * @param context immutable pipeline context
     * @param state completed execution state
     * @return synthesized professional response
     */
    SynthesizedResponse synthesize(
            PipelineContext context,
            PipelineExecutionState state
    );
}