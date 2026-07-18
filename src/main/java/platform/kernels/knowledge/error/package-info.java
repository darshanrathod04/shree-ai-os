/**
 * <b>Knowledge Kernel Error Architecture</b>
 *
 * <p>Defines the standardized, immutable error reporting layer for the Knowledge Kernel within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Provides consistent, immutable error reporting for all Knowledge operations.</li>
 *   <li>Defines standardized error codes for Knowledge Kernel failures.</li>
 *   <li>Establishes a kernel-specific exception hierarchy rooted in {@link RuntimeException}.</li>
 *   <li>Ensures every exception encapsulates exactly one immutable {@link platform.kernels.knowledge.error.KnowledgeError}.</li>
 *   <li>Compliant with Kernel Development Standard (EIO-ARCH-001).</li>
 * </ul>
 *
 * <p><b>Exception Hierarchy:</b></p>
 * <pre>
 * RuntimeException
 *         │
 *         ▼
 *   KnowledgeException
 *         │
 *    ├── KnowledgeValidationException
 *    ├── KnowledgeGraphException
 *    ├── KnowledgeExtractionException
 *    └── KnowledgeNotFoundException
 * </pre>
 *
 * <p><b>Error Principles:</b></p>
 * <ul>
 *   <li>The Error layer MAY describe failures, encapsulate metadata, expose standardized error codes,
 *       and support consistent exception handling.</li>
 *   <li>The Error layer MUST NEVER perform validation, recovery, retry operations, mutate graphs,
 *       modify Knowledge objects, perform reasoning, access persistence, access repositories,
 *       invoke AI, perform networking, or publish events.</li>
 * </ul>
 *
 * <p><b>Domain Boundary:</b></p>
 * <ul>
 *   <li>Valid responsibilities: graph not found, invalid relationship structure, extraction operation failed,
 *       validation failure, missing knowledge entity.</li>
 *   <li>Forbidden responsibilities: explaining why knowledge is false, contradiction analysis, confidence scoring,
 *       inference, ontology repair, graph correction.</li>
 * </ul>
 *
 * <p><b>Package Structure:</b></p>
 * <pre>
 * platform.kernels.knowledge.error
 * ├── KnowledgeErrorCode.java              — Standardized error code enumeration
 * ├── KnowledgeError.java                  — Immutable error value object
 * ├── KnowledgeException.java              — Base exception (extends RuntimeException)
 * ├── KnowledgeValidationException.java    — Validation failure exception
 * ├── KnowledgeGraphException.java         — Graph operation failure exception
 * ├── KnowledgeExtractionException.java    — Extraction failure exception
 * └── KnowledgeNotFoundException.java      — Entity not found exception
 * </pre>
 *
 * <p><b>Ownership:</b> Knowledge Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 * <p><b>Constitutional Authority:</b> EIO-KNW-104, EIO-ARCH-001</p>
 *
 * @see platform.kernels.knowledge.error.KnowledgeErrorCode
 * @see platform.kernels.knowledge.error.KnowledgeError
 * @see platform.kernels.knowledge.error.KnowledgeException
 * @see platform.kernels.knowledge.error.KnowledgeValidationException
 * @see platform.kernels.knowledge.error.KnowledgeGraphException
 * @see platform.kernels.knowledge.error.KnowledgeExtractionException
 * @see platform.kernels.knowledge.error.KnowledgeNotFoundException
 */
package platform.kernels.knowledge.error;