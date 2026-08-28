package com.shreeai.os.platform.kernels.cognitive.engine;

/**
 * <b>ReflectionVerdict</b>
 *
 * <p>The outcome verdict produced by post-execution reflection.</p>
 *
 * <p><b>Ownership:</b> Cognitive Kernel</p>
 */
public enum ReflectionVerdict {
    /** Execution met the request well (score &gt;= 0.75). */
    SUCCESS,
    /** Execution partially met the request (0.4-0.75). */
    PARTIAL,
    /** Execution failed or fell well short (&lt; 0.4). */
    FAILURE
}