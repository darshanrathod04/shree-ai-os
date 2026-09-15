package com.shreeai.os.platform.kernels.knowledge.model;

/**
 * <b>ConceptType</b>
 *
 * <p>The closed, deterministic classification of concepts discovered by the
 * Knowledge Graph Builder (Knowledge Kernel - K3). The type is part of the
 * deterministic concept identity: {@code conceptId = SHA-256(type +
 * canonicalName)}.</p>
 *
 * <p><b>Extensibility:</b> the set is dictionary-driven and deliberately
 * finite - new kinds are added here, never invented at runtime.</p>
 *
 * <p><b>Ownership:</b> Knowledge Kernel - K3 Knowledge Graph Builder</p>
 * <p><b>Version:</b> 1.0</p>
 */
public enum ConceptType {

    /** A programming language (e.g. Java, Kotlin). */
    TECHNOLOGY,

    /** A framework (e.g. Spring Boot). */
    FRAMEWORK,

    /** A programming language member of {@link #TECHNOLOGY}-adjacent families. */
    LANGUAGE,

    /** A database or storage engine (e.g. PostgreSQL). */
    DATABASE,

    /** A library (e.g. Hibernate, Collections). */
    LIBRARY,

    /** A build or operations tool (e.g. Maven, Docker). */
    TOOL,

    /** A documentation topic inside a technology ecosystem (e.g. List). */
    TOPIC,

    /** Anything recognized by the dictionary without a more specific family. */
    GENERAL
}
