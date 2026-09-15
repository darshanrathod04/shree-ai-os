package com.shreeai.os.platform.kernels.acquisition.model;

/**
 * <b>ProviderType</b>
 *
 * <p>Defines the locked provider categories the Knowledge Acquisition Kernel
 * can route knowledge requirements to.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Enumerates the provider types available for acquisition routing.</li>
 *   <li>Provides type safety for the deterministic Provider Router (K0.6.2).</li>
 * </ul>
 *
 * <p><b>Provider Categories:</b></p>
 * <ul>
 *   <li>OFFICIAL_DOCS - official documentation sites.</li>
 *   <li>GITHUB - code repositories and project sources.</li>
 *   <li>ENTERPRISE_DOCS - internal company documentation.</li>
 *   <li>WEB - the open web (also the deterministic fallback provider).</li>
 *   <li>DATABASE - structured data stores.</li>
 *   <li>API - programmatic service interfaces.</li>
 *   <li>LOCAL_FILES - files already present on the user's machine.</li>
 * </ul>
 *
 * <p><b>Thread Safety:</b> Enums are inherently thread-safe.</p>
 *
 * <p><b>Ownership:</b> Knowledge Acquisition Kernel - K0.6.2 Provider Router</p>
 * <p><b>Version:</b> 1.0</p>
 */
public enum ProviderType {
    /** Official documentation sites. */
    OFFICIAL_DOCS,

    /** Code repositories and project sources. */
    GITHUB,

    /** Internal company documentation. */
    ENTERPRISE_DOCS,

    /** The open web; also the locked deterministic fallback provider. */
    WEB,

    /** Structured data stores. */
    DATABASE,

    /** Programmatic service interfaces. */
    API,

    /** Files already present on the user's machine. */
    LOCAL_FILES
}
