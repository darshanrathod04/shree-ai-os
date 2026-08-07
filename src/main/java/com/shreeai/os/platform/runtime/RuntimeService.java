package com.shreeai.os.platform.runtime;

/**
 * Base contract for every runtime component in Shree AI OS.
 *
 * Every runtime service must follow the same lifecycle.
 */
public interface RuntimeService {

    /**
     * Prepare internal resources.
     */
    void initialize();

    /**
     * Start runtime execution.
     */
    void start();

    /**
     * Verify runtime health.
     */
    void verify();

    /**
     * Gracefully stop the service.
     */
    void shutdown();

    /**
     * Runtime service name.
     */
    String getName();
}