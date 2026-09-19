package com.shreeai.os.platform.kernels.acquisition.model;

/**
 * <b>AcquisitionStatus</b>
 *
 * <p>The closed, deterministic execution statuses of the K0.6.5 Knowledge
 * Acquisition Orchestrator. A status records exactly one outcome for one
 * acquisition decision target and nothing more.</p>
 *
 * <p><b>Locked status semantics:</b></p>
 * <table border="1">
 *   <caption>Locked status semantics</caption>
 *   <tr><th>Status</th><th>Meaning</th></tr>
 *   <tr><td>{@link #PENDING}</td><td>The decision has not been executed yet.</td></tr>
 *   <tr><td>{@link #ACQUIRED}</td><td>Fresh knowledge was ingested successfully
 *       ({@code ACQUIRE} first-time or {@code REFRESH} re-ingestion).</td></tr>
 *   <tr><td>{@link #SKIPPED}</td><td>Existing knowledge was reused - the cache
 *       was valid, no ingestion was performed.</td></tr>
 *   <tr><td>{@link #FAILED}</td><td>The decision could not be executed; the
 *       remaining targets are never affected.</td></tr>
 * </table>
 *
 * <p><b>Immutability:</b> Enums are inherently immutable.</p>
 * <p><b>Thread Safety:</b> Enums are inherently thread-safe.</p>
 *
 * <p><b>Ownership:</b> Knowledge Acquisition Kernel - K0.6.5 Acquisition Orchestrator</p>
 * <p><b>Version:</b> 1.0</p>
 */
public enum AcquisitionStatus {

    /** The decision has not been executed yet. */
    PENDING,

    /** Fresh knowledge was ingested successfully. */
    ACQUIRED,

    /** Existing knowledge was reused - no ingestion was performed. */
    SKIPPED,

    /** The decision could not be executed; other targets are unaffected. */
    FAILED;

    /**
     * Returns true when this status represents a successfully completed
     * decision execution - either fresh knowledge was acquired or cached
     * knowledge was reused.
     *
     * @return true when the decision executed successfully
     */
    public boolean isSuccess() {
        return this == ACQUIRED || this == SKIPPED;
    }
}
