/**
 * <b>Kernel Registration Validation</b>
 *
 * <p>Validation layer for the Kernel Registry within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Ensures every Kernel satisfies the architectural requirements before registration.</li>
 *   <li>Answers the question: "Can this Kernel be registered?" — it never registers the kernel.</li>
 *   <li>Returns structured validation results supporting multiple errors in a single execution.</li>
 *   <li>Enforces the architectural invariants defined in KERNEL-007.</li>
 * </ul>
 *
 * <p><b>Package Structure:</b></p>
 * <pre>
 * platform.core.registry.validator
 * ├── ValidationResult.java              — Structured validation result
 * └── KernelRegistrationValidator.java   — Stateless registration validator
 * </pre>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Constitutional Authority:</b> CONST-001, KERNEL-005, KERNEL-007,
 * ADD-PLT-202, ADD-PLT-205</p>
 *
 * <p><b>Design Constraints:</b></p>
 * <ul>
 *   <li>Validator is stateless and deterministic.</li>
 *   <li>No business logic — validation rules only.</li>
 *   <li>No model mutation — models are never modified.</li>
 *   <li>No Spring annotations — framework-agnostic.</li>
 *   <li>No persistence.</li>
 * </ul>
 *
 * @see com.shreeai.os.platform.core.registry.validator.ValidationResult
 * @see com.shreeai.os.platform.core.registry.validator.KernelRegistrationValidator
 */
package com.shreeai.os.platform.core.registry.validator;