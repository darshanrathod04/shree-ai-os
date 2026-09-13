package com.shreeai.os.platform.kernels.context.model;

/**
 * <b>PrimaryDomain</b>
 *
 * <p>Defines the primary domain categories for user requests within the Context Kernel.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Enumerates the possible primary domains a user request can target.</li>
 *   <li>Provides type safety for domain detection results.</li>
 *   <li>Immutable enum representing technical or business domains.</li>
 * </ul>
 *
 * <p><b>Domain Categories:</b></p>
 * <ul>
 *   <li>JAVA - Java programming language and ecosystem</li>
 *   <li>SPRING - Spring Framework and Spring Boot</li>
 *   <li>DATABASE - Database systems and SQL</li>
 *   <li>AI - Artificial Intelligence and Machine Learning</li>
 *   <li>WEB - Web development (HTML, CSS, JavaScript)</li>
 *   <li>MOBILE - Mobile app development</li>
 *   <li>DEVOPS - DevOps, CI/CD, and infrastructure</li>
 *   <li>CLOUD - Cloud computing platforms</li>
 *   <li>SECURITY - Cybersecurity and secure coding</li>
 *   <li>FINANCE - Financial technology and banking</li>
 *   <li>MEDICAL - Healthcare and medical technology</li>
 *   <li>EDUCATION - Educational technology</li>
 *   <li>GENERAL - Request is general but understandable</li>
 *   <li>UNKNOWN - No domain can be confidently detected</li>
 * </ul>
 *
 * <p><b>Thread Safety:</b> Enums are inherently thread-safe.</p>
 *
 * <p><b>Ownership:</b> Context Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 */
public enum PrimaryDomain {
    /**
     * Java programming language and ecosystem.
     */
    JAVA,

    /**
     * Spring Framework and Spring Boot.
     */
    SPRING,

    /**
     * Database systems and SQL.
     */
    DATABASE,

    /**
     * Artificial Intelligence and Machine Learning.
     */
    AI,

    /**
     * Web development (HTML, CSS, JavaScript).
     */
    WEB,

    /**
     * Mobile app development.
     */
    MOBILE,

    /**
     * DevOps, CI/CD, and infrastructure.
     */
    DEVOPS,

    /**
     * Cloud computing platforms.
     */
    CLOUD,

    /**
     * Cybersecurity and secure coding.
     */
    SECURITY,

    /**
     * Financial technology and banking.
     */
    FINANCE,

    /**
     * Healthcare and medical technology.
     */
    MEDICAL,

    /**
     * Educational technology.
     */
    EDUCATION,

    /**
     * Request is general but understandable.
     */
    GENERAL,

    /**
     * No domain can be confidently detected.
     */
    UNKNOWN
}