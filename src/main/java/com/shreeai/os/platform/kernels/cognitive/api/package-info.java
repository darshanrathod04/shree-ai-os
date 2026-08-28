/**
 * <b>Cognitive Kernel API Layer</b>
 *
 * <p>Defines the public contracts for the Cognitive Kernel, providing interfaces
 * for reasoning, decision support, reflection, and cognitive state management.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Defines the primary façade for cognitive operations (CognitiveService).</li>
 *   <li>Provides contracts for reasoning over knowledge (ReasoningService).</li>
 *   <li>Provides contracts for decision support (DecisionService).</li>
 *   <li>Provides contracts for reflective analysis (ReflectionService).</li>
 *   <li>Provides contracts for cognitive state management (CognitiveStateService).</li>
 *   <li>Contains no implementation logic — interfaces only.</li>
 *   <li>Compliant with Kernel Development Standard (EIO-ARCH-001).</li>
 * </ul>
 *
 * <p><b>API Hierarchy:</b></p>
 * <pre>
 *              CognitiveService
 *              /      |      \
 *             /       |       \
 *    Reasoning   Decision   Reflection
 *                 |
 *                 |
 *       CognitiveStateService
 * </pre>
 *
 * <p><b>Design Principles:</b></p>
 * <ul>
 *   <li>Interface-only — no implementation logic.</li>
 *   <li>Technology-agnostic — no framework dependencies.</li>
 *   <li>Business-focused — exposes only business-level contracts.</li>
 *   <li>Stateless — no mutable state.</li>
 * </ul>
 *
 * <p><b>Platform Boundaries:</b></p>
 * <ul>
 *   <li>May reference platform models and value objects.</li>
 *   <li>Must never depend on persistence, repositories, networking, AI providers, execution engines, orchestration components, tool invocation, or UI components.</li>
 * </ul>
 *
 * <p><b>Package Structure:</b></p>
 * <pre>
 * platform.kernels.cognitive.api
 * ├── CognitiveService.java          — Primary cognitive façade
 * ├── ReasoningService.java          — Reasoning contracts
 * ├── DecisionService.java           — Decision support contracts
 * ├── ReflectionService.java         — Reflection contracts
 * ├── CognitiveStateService.java     — Cognitive state management contracts
 * ├── package-info.java              — Package documentation
 * └── README.md                      — API layer documentation
 * </pre>
 *
 * <p><b>Ownership:</b> Cognitive Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 * <p><b>Constitutional Authority:</b> EIO-COG-101, EIO-ARCH-001</p>
 *
 * @see com.shreeai.os.platform.kernels.cognitive.api.CognitiveService
 * @see com.shreeai.os.platform.kernels.cognitive.api.ReasoningService
 * @see com.shreeai.os.platform.kernels.cognitive.api.DecisionService
 * @see com.shreeai.os.platform.kernels.cognitive.api.ReflectionService
 * @see com.shreeai.os.platform.kernels.cognitive.api.CognitiveStateService
 */
package com.shreeai.os.platform.kernels.cognitive.api;