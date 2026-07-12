/**
 * <b>Discovery Validation</b>
 *
 * <p>Validation layer for the Discovery Service within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Ensures discovery requests satisfy all architectural requirements before capability resolution.</li>
 *   <li>Answers the question: "Can this discovery request be processed?" — it never performs discovery.</li>
 *   <li>Returns structured {@link platform.core.registry.validator.ValidationResult} supporting multiple errors in a single execution.</li>
 *   <li>Reuses the approved Registry validation architecture.</li>
 * </ul>
 *
 * <p><b>Package Structure:</b></p>
 * <pre>
 * platform.core.discovery.validator
 * └── DiscoveryValidator.java  — Stateless discovery validator
 * </pre>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Constitutional Authority:</b> CONST-001, KERNEL-006, ADD-PLT-202,
 * ADD-PLT-205, ADD-PLT-206</p>
 *
 * <p><b>Design Constraints:</b></p>
 * <ul>
 *   <li>Validator is stateless and deterministic.</li>
 *   <li>Reuses {@link platform.core.registry.validator.ValidationResult} — no duplicate validation classes.</li>
 *   <li>No business logic — validation rules only.</li>
 *   <li>No model mutation — models are never modified.</li>
 *   <li>Never performs discovery — validation only.</li>
 *   <li>Never accesses Registry — independent validation layer.</li>
 * </ul>
 *
 * @see platform.core.discovery.validator.DiscoveryValidator
 * @see platform.core.registry.validator.ValidationResult
 */
package platform.core.discovery.validator;