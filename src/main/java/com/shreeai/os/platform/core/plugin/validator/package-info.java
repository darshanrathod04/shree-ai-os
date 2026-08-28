/**
 * <b>Plugin Validator</b>
 *
 * <p>Stateless validation layer for the Plugin Framework within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Validates that Plugin models meet all structural requirements.</li>
 *   <li>Answers the question: "Is this Plugin model valid?"</li>
 *   <li>Never loads, executes, or modifies plugins.</li>
 *   <li>Returns structured {@link com.shreeai.os.platform.core.registry.validator.ValidationResult} supporting multiple errors.</li>
 * </ul>
 *
 * <p><b>Package Boundaries:</b></p>
 * <ul>
 *   <li>This package contains ONLY validators.</li>
 *   <li>No business logic.</li>
 *   <li>No persistence.</li>
 *   <li>No services.</li>
 *   <li>No plugin loading.</li>
 *   <li>No lifecycle management.</li>
 *   <li>No events.</li>
 *   <li>No threading.</li>
 * </ul>
 *
 * <p><b>Characteristics:</b></p>
 * <ul>
 *   <li>Stateless — all state passed as parameters.</li>
 *   <li>Deterministic — same inputs produce same results.</li>
 *   <li>Thread-safe — no mutable state.</li>
 *   <li>Pure validation — no side effects.</li>
 *   <li>No mutation — models are never modified.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> ADD-PLT-301</p>
 *
 * @see PluginValidator
 * @see com.shreeai.os.platform.core.registry.validator.ValidationResult
 */
package com.shreeai.os.platform.core.plugin.validator;