/**
 * <b>Memory Kernel — Platform Language</b>
 *
 * <p>This package contains the complete Platform Language for the Memory Kernel.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Defines immutable data contracts for Memory entities.</li>
 *   <li>Provides type-safe domain primitives (MemoryId, MemoryType, etc.).</li>
 *   <li>Encapsulates request/result types for Memory operations.</li>
 *   <li>Contains no business logic, service logic, or implementation.</li>
 * </ul>
 *
 * <p><b>Package Structure:</b></p>
 * <pre>
 * model/
 * ├── Memory.java                (core entity)
 * ├── MemoryId.java              (value object — identifier)
 * ├── MemoryType.java            (enum)
 * ├── MemoryStatus.java          (enum)
 * ├── MemoryVisibility.java      (enum)
 * ├── MemoryContent.java         (value object — content)
 * ├── MemoryMetadata.java        (value object — metadata)
 * ├── MemoryResult.java          (value object — operation result)
 * ├── MemoryStatistics.java      (value object — stats)
 * ├── MemoryExport.java          (value object — export)
 * ├── MemoryImport.java          (value object — import request)
 * ├── MemoryImportResult.java    (value object — import result)
 * ├── CreateMemoryRequest.java   (request — create)
 * └── UpdateMemoryRequest.java   (request — update)
 * </pre>
 *
 * <p><b>Design Invariants:</b></p>
 * <ul>
 *   <li>All records are immutable.</li>
 *   <li>Collections are defensively copied and wrapped in unmodifiable views.</li>
 *   <li>Constructor validation uses {@link java.util.Objects#requireNonNull} only.</li>
 *   <li>No business logic, validation logic, or service logic.</li>
 *   <li>No framework dependencies (Spring, Lombok, JPA, etc.).</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Memory Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 * <p><b>Constitutional Authority:</b> ADD-201</p>
 *
 * @see platform.kernels.memory.api
 */
package com.shreeai.os.platform.kernels.memory.model;