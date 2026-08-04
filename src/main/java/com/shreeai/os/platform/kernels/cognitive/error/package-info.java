/**
 * <b>Cognitive Kernel - Error Layer</b>
 *
 * <p>Provides standardized, immutable error representation and classification for the Cognitive Kernel.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Classifies cognitive failures into stable categories</li>
 *   <li>Encapsulates immutable error information</li>
 *   <li>Preserves root causes for debugging</li>
 *   <li>Maintains consistent failure reporting</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Cognitive Kernel - Error Layer</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Error Architecture Philosophy:</b></p>
 * <p>The Error Layer exists solely to classify and represent failures. It must never attempt
 * recovery, retry failed operations, evaluate reasoning quality, modify cognitive state, or
 * invoke business logic. Every exception encapsulates a single immutable CognitiveError,
 * ensuring consistent failure reporting across the platform while preserving the original
 * cause where applicable.</p>
 *
 * <p><b>Exception Hierarchy:</b></p>
 * <pre>
 * RuntimeException
 *     │
 *     ▼
 * CognitiveException
 *     │
 *     ├── ReasoningException
 *     ├── DecisionException
 *     ├── ReflectionException
 *     └── CognitiveStateException
 * </pre>
 *
 * <p><b>Architectural Boundaries:</b></p>
 * <p>The Error Layer may:</p>
 * <ul>
 *   <li>Represent failures</li>
 *   <li>Classify failures</li>
 *   <li>Aggregate immutable metadata</li>
 * </ul>
 *
 * <p>The Error Layer must never:</p>
 * <ul>
 *   <li>Resolve failures</li>
 *   <li>Repair state</li>
 *   <li>Execute reasoning</li>
 *   <li>Evaluate hypotheses</li>
 *   <li>Generate recommendations</li>
 *   <li>Invoke validators</li>
 *   <li>Invoke engines</li>
 *   <li>Invoke services</li>
 * </ul>
 *
 * <p><b>Immutable Error Representation:</b></p>
 * <p>All error objects are immutable value objects with final fields, defensive copying,
 * and unmodifiable collections. Once created, error information never changes.</p>
 *
 * <p><b>Exception Principles:</b></p>
 * <p>Exceptions shall:</p>
 * <ul>
 *   <li>Classify failures</li>
 *   <li>Carry immutable error information</li>
 *   <li>Preserve root cause</li>
 *   <li>Remain deterministic</li>
 * </ul>
 *
 * <p>Exceptions shall never:</p>
 * <ul>
 *   <li>Retry operations</li>
 *   <li>Recover automatically</li>
 *   <li>Invoke services</li>
 *   <li>Invoke reasoning</li>
 *   <li>Invoke AI</li>
 *   <li>Mutate domain models</li>
 *   <li>Log directly</li>
 *   <li>Access persistence</li>
 *   <li>Perform networking</li>
 * </ul>
 *
 * <p><b>Platform Layering:</b></p>
 * <p>This package implements the fourth canonical layer in the platform architecture:</p>
 * <pre>
 * API
 *  ↓
 * Model
 *  ↓
 * Validation
 *  ↓
 * Error
 *  ↓
 * Service
 *  ↓
 * Engine
 *  ↓
 * Verification
 * </pre>
 *
 * <p><b>Recovery Responsibility:</b></p>
 * <p>Recovery belongs to future Service and Chief kernels. The Error Layer only reports
 * failures - it never attempts to resolve them.</p>
 *
 * <p><b>Platform Language:</b> Java 21</p>
 * <p><b>Constitutional Authority:</b> EIO-COG-104, EIO-ARCH-001</p>
 *
 * @since 1.0
 */
package com.shreeai.os.platform.kernels.cognitive.error;