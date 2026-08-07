/**
 * <b>Cognitive Domain Model</b>
 *
 * <p>Provides the immutable value objects that represent cognitive concepts
 * throughout the platform.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Defines the canonical domain representations for cognitive entities.</li>
 *   <li>Provides immutable value objects for cognitive concepts.</p>
 *   <li>Ensures type-safe identity references across the platform.</li>
 *   <li>Maintains separation from service contracts and implementation logic.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Cognitive Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-COG-102, EIO-ARCH-001</p>
 *
 * <p><b>Design Principles:</b></p>
 * <ul>
 *   <li>Immutable — all models are immutable value objects.</li>
 *   <li>Framework-independent — no framework dependencies.</li>
 *   <li>Behavior-free — contains data only, no business logic.</li>
 *   <li>Validated — constructor validation ensures integrity.</li>
 *   <li>Defensive — defensive copying preserves immutability.</li>
 * </ul>
 *
 * <p><b>Model Hierarchy:</b></p>
 * <pre>
 * CognitiveState
 *     ├── ReasoningRequest
 *     ├── DecisionContext
 *     └── ReflectionScope
 *         └── EvaluationCriteria
 *             ├── Hypothesis
 *             └── Recommendation
 *                 └── CognitiveSnapshot
 *
 * CognitiveId (identity for all aggregates)
 * </pre>
 *
 * <p><b>Usage:</b> These models serve as the canonical domain representations
 * used by the Cognitive API and future kernel implementations. They define
 * what the platform can reason about, not how reasoning is performed.</p>
 *
 * @see platform.kernels.cognitive.api
 */
package com.shreeai.os.platform.kernels.cognitive.model;