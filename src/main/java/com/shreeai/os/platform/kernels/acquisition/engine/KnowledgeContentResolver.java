package com.shreeai.os.platform.kernels.acquisition.engine;

import com.shreeai.os.platform.kernels.acquisition.model.AcquisitionDecisionTarget;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeSource;

/**
 * <b>KnowledgeContentResolver</b>
 *
 * <p>Content resolution interface for the Knowledge Acquisition subsystem.
 * Resolves or fetches raw content for a given knowledge source and acquisition target.</p>
 *
 * <p><b>Ownership:</b> Knowledge Acquisition Kernel - K0.6</p>
 * <p><b>Version:</b> 1.0</p>
 */
@FunctionalInterface
public interface KnowledgeContentResolver {

    /**
     * Resolves raw document content for a target knowledge source.
     *
     * @param source the knowledge source being acquired (never null)
     * @param target the acquisition decision target (never null)
     * @return raw document content (e.g. Markdown, HTML, or plain text), or null if content cannot be resolved
     */
    String resolveContent(KnowledgeSource source, AcquisitionDecisionTarget target);
}
