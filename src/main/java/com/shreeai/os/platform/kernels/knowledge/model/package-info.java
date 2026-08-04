/**
 * <b>Knowledge Kernel Semantic Domain Model</b>
 *
 * <p>Defines the complete immutable semantic domain model for the Knowledge Kernel within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Defines the semantic language of the Knowledge Kernel.</li>
 *   <li>Provides immutable value objects, entities, and enumerations for knowledge representation.</li>
 *   <li>Represents structured knowledge, semantic concepts, relationships, and the knowledge graph.</li>
 *   <li>Contains no behavior — this is a domain model layer only.</li>
 *   <li>Compliant with Kernel Development Standard (EIO-ARCH-001).</li>
 * </ul>
 *
 * <p><b>Semantic Model Philosophy:</b></p>
 * <ul>
 *   <li>Models are immutable — once created, they cannot be modified.</li>
 *   <li>Identity is represented by {@link com.shreeai.os.platform.kernels.knowledge.model.KnowledgeId} — never by primitive identifiers.</li>
 *   <li>Java 21 records are used where appropriate for simple value objects.</li>
 *   <li>Final classes with static factory methods are used where records are not appropriate.</li>
 *   <li>All collections are defensively copied and returned as unmodifiable.</li>
 *   <li>Constructor validation ensures domain invariants are satisfied at creation time.</li>
 * </ul>
 *
 * <p><b>Semantic Hierarchy:</b></p>
 * <pre>
 * KnowledgeGraph
 *         │
 *         ├── KnowledgeNode
 *         │       │
 *         │       └── KnowledgeConcept
 *         │
 *         └── KnowledgeRelationship
 * </pre>
 *
 * <p><b>Architectural Boundaries:</b></p>
 * <ul>
 *   <li>Identity defines WHO — Knowledge models do not represent identity.</li>
 *   <li>Memory defines WHAT HAPPENED — Knowledge models do not store historical interaction records.</li>
 *   <li>Context defines WHAT IS HAPPENING NOW — Knowledge models do not represent runtime state.</li>
 *   <li>Knowledge defines WHAT IS KNOWN AND HOW IT IS RELATED — semantic entities and relationships.</li>
 * </ul>
 *
 * <p><b>Package Structure:</b></p>
 * <pre>
 * platform.kernels.knowledge.model
 * ├── KnowledgeId.java              — Immutable value object for entity identity
 * ├── KnowledgeNode.java            — Semantic entity (graph vertex)
 * ├── KnowledgeConcept.java         — Semantic concept (specialized node)
 * ├── KnowledgeRelationship.java    — Semantic relationship (graph edge)
 * ├── KnowledgeGraph.java           — Semantic graph (aggregate container)
 * ├── KnowledgeSnapshot.java        — Read-only graph snapshot
 * ├── CreateKnowledgeRequest.java   — Creation request model
 * ├── UpdateKnowledgeRequest.java   — Update request model
 * ├── KnowledgeType.java            — Entity type enumeration
 * ├── KnowledgeRelationshipType.java — Relationship type enumeration
 * ├── KnowledgeState.java           — Lifecycle state enumeration
 * └── KnowledgeScope.java           — Visibility scope enumeration
 * </pre>
 *
 * <p><b>Out of Scope:</b></p>
 * <ul>
 *   <li>Graph algorithms — belong in the Engine layer (KNW-106).</li>
 *   <li>Persistence — storage concerns belong in the Service layer.</li>
 *   <li>Validation — validation logic belongs in the Validation layer (KNW-103).</li>
 *   <li>Error handling — exceptions belong in the Error layer (KNW-104).</li>
 *   <li>Runtime state — belongs in the Context Kernel.</li>
 *   <li>Historical records — belongs in the Memory Kernel.</li>
 *   <li>AI providers — belong in the Cognitive or Chief Kernel.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Knowledge Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 * <p><b>Constitutional Authority:</b> EIO-KNW-101, EIO-KNW-102, EIO-ARCH-001</p>
 *
 * @see com.shreeai.os.platform.kernels.knowledge.model.KnowledgeId
 * @see com.shreeai.os.platform.kernels.knowledge.model.KnowledgeNode
 * @see com.shreeai.os.platform.kernels.knowledge.model.KnowledgeConcept
 * @see com.shreeai.os.platform.kernels.knowledge.model.KnowledgeRelationship
 * @see com.shreeai.os.platform.kernels.knowledge.model.KnowledgeGraph
 * @see com.shreeai.os.platform.kernels.knowledge.model.KnowledgeSnapshot
 */
package com.shreeai.os.platform.kernels.knowledge.model;