package com.shreeai.os.platform.kernels.context.model;

/**
 * <b>PlatformType</b>
 *
 * <p>Defines the target platform for content generation.</p>
 *
 * <p><b>Ownership:</b> Context Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 */
public enum PlatformType {
    /**
     * Windows operating system.
     */
    WINDOWS,

    /**
     * macOS operating system.
     */
    MAC,

    /**
     * Linux operating system.
     */
    LINUX,

    /**
     * Android mobile platform.
     */
    ANDROID,

    /**
     * iOS mobile platform.
     */
    IOS,

    /**
     * Web browser platform.
     */
    WEB
}