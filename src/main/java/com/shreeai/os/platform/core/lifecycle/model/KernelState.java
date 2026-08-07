package com.shreeai.os.platform.core.lifecycle.model;

import com.shreeai.os.platform.core.lifecycle.api.LifecycleService;

/**
 * <b>KernelState</b>
 *
 * <p>Represents the lifecycle state of a Kernel within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Provides a typed enumeration of all possible kernel lifecycle states.</li>
 *   <li>Enables consistent state reporting across the Lifecycle Service.</li>
 *   <li>Supports the LifecycleService's state query mechanism.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> KERNEL-010</p>
 *
 * @see LifecycleTransition
 * @see LifecycleService
 */
public enum KernelState {

    /**
     * Kernel has been created but not initialized.
     */
    CREATED,

    /**
     * Kernel has been initialized but not started.
     */
    INITIALIZED,

    /**
     * Kernel is running and operational.
     */
    RUNNING,

    /**
     * Kernel is suspended (state preserved for later resumption).
     */
    SUSPENDED,

    /**
     * Kernel has been stopped.
     */
    STOPPED,

    /**
     * Kernel has encountered an error.
     */
    FAILED,

    /**
     * Kernel has been permanently terminated.
     */
    TERMINATED
}