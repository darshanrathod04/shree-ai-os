/**
 * <b>Memory Error</b>
 *
 * <p>Error handling architecture for the Memory Kernel within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Defines error codes for all Memory subsystem failures.</li>
 *   <li>Provides immutable error model with structured data.</li>
 *   <li>Enables consistent exception handling via base {@link MemoryException}.</li>
 *   <li>Never executes memory operations or stores memories.</li>
 * </ul>
 *
 * <p><b>Package Boundaries:</b></p>
 * <ul>
 *   <li>This package contains ONLY error definitions.</li>
 *   <li>No business logic.</li>
 *   <li>No persistence.</li>
 *   <li>No services.</li>
 *   <li>No memory storage.</li>
 *   <li>No memory search.</li>
 *   <li>No lifecycle management.</li>
 *   <li>No events.</li>
 *   <li>No threading.</li>
 * </ul>
 *
 * <p><b>Error Hierarchy:</b></p>
 * <ul>
 *   <li>{@link MemoryErrorCode} — Enumeration of error conditions</li>
 *   <li>{@link MemoryError} — Immutable error model</li>
 *   <li>{@link MemoryException} — Base runtime exception</li>
 *   <li>{@link DuplicateMemoryException} — Duplicate memory</li>
 *   <li>{@link MemoryNotFoundException} — Memory not found</li>
 *   <li>{@link InvalidMemoryException} — Validation failure</li>
 * </ul>
 *
 * <p><b>Relationship with Validator:</b> The validator checks structural integrity
 * and returns {@link com.shreeai.os.platform.core.registry.validator.ValidationResult}. This
 * package provides the runtime exception mechanism for when validation fails
 * and operations must be aborted.</p>
 *
 * <p><b>Ownership:</b> Memory Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> ADD-201</p>
 *
 * @see MemoryErrorCode
 * @see MemoryError
 * @see MemoryException
 */
package com.shreeai.os.platform.kernels.memory.error;