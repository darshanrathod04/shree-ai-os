package com.shreeai.os.platform.runtime.studio.journal;

/**
 * <b>JournalStatus</b>
 *
 * <p>Terminal state of an {@link ExecutionJournal}. A journal becomes
 * {@code IN_PROGRESS} when a {@code PIPELINE_STARTED} event is observed,
 * and transitions to {@code COMPLETED} or {@code FAILED} on
 * {@code PIPELINE_COMPLETED} / {@code PIPELINE_FAILED}.</p>
 *
 * <p><b>Ownership:</b> Runtime — Studio Execution Journal (Phase 3)</p>
 * <p><b>Version:</b> 1.0</p>
 */
public enum JournalStatus {

    IN_PROGRESS,
    COMPLETED,
    FAILED
}