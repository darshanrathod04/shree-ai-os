/**
 * <b>Context Service Package</b>
 *
 * <p>Provides the coordination layer for the Context Kernel.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Coordinates all Context API requests.</li>
 *   <li>Validates incoming requests using ContextValidator.</li>
 *   <li>Delegates processing to ContextProcessingEngine.</li>
 *   <li>Translates failures into ContextException hierarchy.</li>
 *   <li>Contains ZERO business logic - pure coordination layer.</li>
 * </ul>
 *
 * <p><b>Coordinator Pattern:</b></p>
 * <p>This package implements the coordinator pattern as mandated by the Kernel Development
 * Standard (EIO-ARCH-001). The service layer is a thin coordination layer that:</p>
 * <ul>
 *   <li>Receives API requests from the API layer.</li>
 *   <li>Validates requests using static ContextValidator methods.</li>
 *   <li>Delegates all processing to ContextProcessingEngine.</li>
 *   <li>Translates failures into ContextException hierarchy.</li>
 *   <li>Returns API results.</li>
 * </ul>
 *
 * <p><b>Delegation Philosophy:</b></p>
 * <p>The service never performs processing itself. All business logic, persistence,
 * and state mutations are delegated to the ContextProcessingEngine. The service
 * maintains no mutable state and contains no business logic.</p>
 *
 * <p><b>Key Components:</b></p>
 * <ul>
 *   <li>{@link platform.kernels.context.service.DefaultContextService} - The default service implementation</li>
 *   <li>{@link platform.kernels.context.service.ContextProcessingEngine} - The processing engine interface</li>
 * </ul>
 *
 * <p><b>Thread Safety:</b> All implementations must be thread-safe. The DefaultContextService
 * achieves this through immutability and stateless design.</p>
 *
 * <p><b>Dependency Injection:</b> Uses constructor injection only. No field injection,
 * service locator, or static singleton patterns are permitted.</p>
 *
 * <p><b>Exception Translation:</b> All failures are translated into the Context exception
 * hierarchy using ContextError and ContextException. Primitive error information is
 * never exposed to consumers.</p>
 *
 * <p><b>Ownership:</b> Context Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-CTX-105, EIO-ARCH-001</p>
 *
 * @see platform.kernels.context.service.DefaultContextService
 * @see platform.kernels.context.service.ContextProcessingEngine
 */
package platform.kernels.context.service;