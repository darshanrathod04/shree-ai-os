/**
 * <b>Chief Kernel Engine Layer</b>
 *
 * <p>This package provides the deterministic strategic processing engine for the Chief Kernel.
 * The Engine Layer transforms validated orchestration requests into strategic processing results.</p>
 *
 * <p><b>Engine Philosophy:</b></p>
 * <ul>
 *   <li><b>Deterministic</b> — same input always produces same output</li>
 *   <li><b>Stateless</b> — no mutable state, no cached decisions</li>
 *   <li><b>Immutable</b> — all outputs are immutable value objects</li>
 *   <li><b>Isolated</b> — separated from validation and service orchestration</li>
 * </ul>
 *
 * <p><b>Processing Flow:</b></p>
 * <pre>
 * Validated Request
 *        │
 *        ▼
 * DefaultChiefProcessingEngine
 *        │
 *        ▼
 * Strategic Computation
 *        │
 *        ▼
 * ChiefProcessingResult
 *        │
 *        ▼
 * Return
 * </pre>
 *
 * <p><b>Engine Components:</b></p>
 * <ul>
 *   <li>{@link com.shreeai.os.platform.kernels.chief.engine.ChiefProcessingEngine} — strategic processing contract</li>
 *   <li>{@link com.shreeai.os.platform.kernels.chief.engine.ChiefProcessingResult} — immutable processing result</li>
 *   <li>{@link com.shreeai.os.platform.kernels.chief.engine.DefaultChiefProcessingEngine} — default engine implementation</li>
 * </ul>
 *
 * <p><b>Design Principles:</b></p>
 * <ul>
 *   <li><b>Deterministic</b> — no randomness, no timestamps for decisions</li>
 *   <li><b>Immutable</b> — all fields are final, no setters</li>
 *   <li><b>Stateless</b> — no mutable state, no caches</li>
 *   <li><b>Thread-safe</b> — no synchronization required</li>
 *   <li><b>Constructor validation</b> — Objects.requireNonNull()</li>
 *   <li><b>Defensive copying</b> — List.copyOf(), Map.copyOf()</li>
 * </ul>
 *
 * <p><b>Engine Layer must never:</b></p>
 * <ul>
 *   <li>Validate requests</li>
 *   <li>Invoke validators</li>
 *   <li>Translate exceptions</li>
 *   <li>Coordinate services</li>
 *   <li>Perform retry</li>
 *   <li>Perform recovery</li>
 *   <li>Access persistence</li>
 *   <li>Access networking</li>
 *   <li>Execute orchestration algorithms</li>
 *   <li>Implement business logic</li>
 * </ul>
 *
 * <p><b>Migration Note:</b></p>
 * <p>The ChiefProcessingEngine interface was migrated from
 * {@code platform.kernels.chief.service} to this package in EIO-CHIEF-106
 * to establish the canonical engine layer location.</p>
 *
 * <p><b>Ownership:</b> Chief Kernel — Engine Layer</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-CHIEF-106, EIO-ARCH-001</p>
 *
 * @since 1.0
 */
package com.shreeai.os.platform.kernels.chief.engine;