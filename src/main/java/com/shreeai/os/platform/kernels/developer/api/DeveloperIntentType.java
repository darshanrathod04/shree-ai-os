package com.shreeai.os.platform.kernels.developer.api;

/**
 * <b>DeveloperIntentType</b>
 *
 * <p>Canonical intent categories for developer-facing requests.
 * These are distinct from the runtime {@code IntentType} which governs
 * kernel routing. Developer intent types drive the Developer Agent's
 * internal analysis pipeline.</p>
 *
 * @since Sprint-14
 */
public enum DeveloperIntentType {

    /**
     * Add a new feature (e.g., "Add JWT authentication", "Add user profile").
     */
    ADD_FEATURE,

    /**
     * Refactor existing code (e.g., "Extract service layer", "Rename UserService").
     */
    REFACTOR,

    /**
     * Fix a bug (e.g., "Fix null pointer in UserService", "Fix race condition").
     */
    FIX_BUG,

    /**
     * Performance or code optimization (e.g., "Cache the user lookup", "Optimize query").
     */
    OPTIMIZE,

    /**
     * Create or extend an API (e.g., "Create a new REST endpoint", "Add GraphQL API").
     */
    CREATE_API,

    /**
     * Add a database entity or schema (e.g., "Add Product entity", "Create Order table").
     */
    ADD_ENTITY,

    /**
     * Security-related change (e.g., "Add JWT", "Secure the endpoints", "Add CORS").
     */
    SECURITY,

    /**
     * Database/data-layer change (e.g., "Add repository", "Change query", "Add migration").
     */
    DATABASE
}
