/**
 * <b>Knowledge Kernel Public API</b>
 *
 * <p>Defines the stable public contracts for the Knowledge Kernel within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Defines the public contract for all Knowledge operations.</li>
 *   <li>Specifies WHAT the Knowledge Kernel can do — implementations define HOW.</li>
 *   <li>Enforces that no kernel accesses Knowledge internals directly.</li>
 *   <li>Provides stable, framework-agnostic contracts for platform-wide communication.</li>
 *   <li>Compliant with Kernel Development Standard (EIO-ARCH-001).</li>
 * </ul>
 *
 * <p><b>Knowledge Kernel Purpose:</b></p>
 * <ul>
 *   <li>Manages structured knowledge entities within the platform.</li>
 *   <li>Provides queryable knowledge access for other kernels.</li>
 *   <li>Maintains semantic relationships between knowledge entities.</li>
 *   <li>Exposes knowledge graph operations for semantic navigation.</li>
 *   <li>Defines concept extraction contracts for knowledge generation.</li>
 * </ul>
 *
 * <p><b>Architectural Boundaries:</b></p>
 * <ul>
 *   <li>The Knowledge Kernel does NOT store runtime state (Context Kernel responsibility).</li>
 *   <li>The Knowledge Kernel does NOT store historical interaction records (Memory Kernel responsibility).</li>
 *   <li>The Knowledge Kernel answers: "What is known, and how is it related?"</li>
 *   <li>Identity answers: "Who?"</li>
 *   <li>Memory answers: "What happened?"</li>
 *   <li>Context answers: "What is happening now?"</li>
 * </ul>
 *
 * <p><b>Package Structure:</b></p>
 * <pre>
 * platform.kernels.knowledge.api
 * ├── KnowledgeService.java           — Primary API for knowledge lifecycle management
 * ├── KnowledgeQueryService.java      — Query and search operations
 * ├── KnowledgeGraphService.java      — Semantic relationship and graph operations
 * └── KnowledgeExtractionService.java — Concept extraction contracts
 * </pre>
 *
 * <p><b>Ownership:</b> Knowledge Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 * <p><b>Constitutional Authority:</b> EIO-KNW-101, EIO-ARCH-001</p>
 *
 * <p><b>Out of Scope:</b></p>
 * <ul>
 *   <li>Runtime session state — belongs in the Context Kernel.</li>
 *   <li>Historical memory storage — belongs in the Memory Kernel.</li>
 *   <li>Planning and execution — belongs in the Planning and Execution Kernels.</li>
 *   <li>AI orchestration — belongs in the Chief Kernel and Cognitive Kernel.</li>
 *   <li>Implementation — no implementation classes in this package.</li>
 *   <li>Validation — validation logic belongs in the implementation layer.</li>
 *   <li>Storage — persistence concerns are handled by the implementation.</li>
 *   <li>Business Logic — algorithms belong in the implementation layer.</li>
 * </ul>
 *
 * @see com.shreeai.os.platform.kernels.knowledge.api.KnowledgeService
 * @see com.shreeai.os.platform.kernels.knowledge.api.KnowledgeQueryService
 * @see com.shreeai.os.platform.kernels.knowledge.api.KnowledgeGraphService
 * @see com.shreeai.os.platform.kernels.knowledge.api.KnowledgeExtractionService
 */
package com.shreeai.os.platform.kernels.knowledge.api;