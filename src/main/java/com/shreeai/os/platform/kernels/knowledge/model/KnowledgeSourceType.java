package com.shreeai.os.platform.kernels.knowledge.model;

/**
 * <b>KnowledgeSourceType</b>
 *
 * <p>Extensible enumeration of the knowledge source families that Shree AI OS
 * can register through the {@code KnowledgeSourceRegistry}.</p>
 *
 * <p>Each type is a category of origin - it does not describe content. Every
 * future ingestion pipeline must register its source type here first.</p>
 *
 * <p><b>Ownership:</b> Knowledge Kernel - K1 Universal Knowledge Source Registry</p>
 * <p><b>Version:</b> 1.0</p>
 */
public enum KnowledgeSourceType {

    /** Portable Document Format files (for example {@code Java.pdf}). */
    PDF,

    /** Markdown documents (for example {@code spring.md}). */
    MARKDOWN,

    /** A directory or folder of documents (for example {@code docs/}). */
    FOLDER,

    /** A database connection or schema (for example PostgreSQL). */
    DATABASE,

    /** An internal or external API endpoint (for example Internal REST). */
    API,

    /** A web site or online documentation host (for example Oracle Docs). */
    WEB,

    /** A JSON document or blob (for example {@code knowledge.json}). */
    JSON,

    /** Plain text notes (for example a simple {@code .txt} file). */
    TEXT
}