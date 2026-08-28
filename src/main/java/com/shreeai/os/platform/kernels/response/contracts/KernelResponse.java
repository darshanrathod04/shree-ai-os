package com.shreeai.os.platform.kernels.response.contracts;

/**
 * Base contract implemented by every kernel response.
 *
 * Planning, Knowledge, Memory and Conversation responses all implement
 * this interface. It allows the Response Formatter to format responses
 * without knowing kernel internals.
 */
public sealed interface KernelResponse permits
        PlanningResponse,
        KnowledgeResponse,
        MemoryResponse,
        ConversationResponse {

    String title();

    double confidence();
}