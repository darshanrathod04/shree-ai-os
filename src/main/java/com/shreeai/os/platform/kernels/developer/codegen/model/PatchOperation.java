package com.shreeai.os.platform.kernels.developer.codegen.model;

/**
 * <b>PatchOperation</b> — type of code modification represented by a single
 * {@link FilePatch}. All operations are pure descriptions; no patch is ever
 * applied to disk by the Developer Agent.
 *
 * <p><b>Ownership:</b> Developer Agent (Sprint-15 — Autonomous Code Generation)</p>
 *
 * @since Sprint-15
 */
public enum PatchOperation {

    /** Create a brand new top-level class / interface / record / enum file. */
    CREATE_CLASS,

    /** Modify an existing class body (annotations, supertype, implements). */
    MODIFY_CLASS,

    /** Add a new method (with annotations, parameters, return type). */
    ADD_METHOD,

    /** Modify the body or signature of an existing method. */
    MODIFY_METHOD,

    /** Add a new field (with annotations and initializer). */
    ADD_FIELD,

    /** Add a new import statement. */
    ADD_IMPORT,

    /** Add a new REST endpoint (i.e. a new @GetMapping/@PostMapping method). */
    ADD_ENDPOINT,

    /** Add a new JPA entity (class + @Entity + @Id field). */
    ADD_ENTITY
}
