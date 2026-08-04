# MONOREPO-ARCHITECTURE-V1 — Shree AI OS Monorepo Structure

## Document Information

| Field          | Value                       |
|----------------|-----------------------------|
| Document ID    | MONOREPO-ARCHITECTURE-V1    |
| Document Type  | Engineering Design Document |
| Platform       | Shree AI OS                 |
| Version        | 1.0 (Founding Edition)      |
| Status         | Draft                       |
| Owner          | Chief AI Architect          |
| Founder        | Darshan Rathod              |
| Classification | Platform Engineering        |

---

# Official Architecture Statement

> **Shree AI OS is engineered as a Maven multi-module monorepo that enforces strict architectural boundaries between framework-independent Platform Core, evolvable Kernels, and application-specific implementations.**

---

# Purpose

This document defines the repository architecture for Shree AI OS.

It establishes:
- Maven multi-module hierarchy
- Dependency graph rules
- Framework boundaries
- Module responsibilities
- Build order

---

# Repository Structure

## Top-Level Layout

```
shree-ai-os/
├── pom.xml                                    # Parent POM (platform-wide)
├── docs/                                      # Documentation (PROJECT-001)
├── platform/                                  # Platform Core & Kernels
│   ├── core/                                  # Platform Core Services
│   │   └── shree-platform-core/
│   ├── kernels/                               # Platform Kernels
│   │   ├── shree-kernel-identity/
│   │   ├── shree-kernel-memory/
│   │   ├── shree-kernel-knowledge/
│   │   ├── shree-kernel-planning/
│   │   ├── shree-kernel-reasoning/
│   │   └── shree-kernel-capability/
│   └── contracts/                             # Shared contracts (future)
├── sdk/                                       # Developer SDK
│   └── shree-sdk/
├── tools/                                     # Development tools
├── examples/                                  # Example applications
├── tests/                                     # Integration tests
├── scripts/                                   # Build/deploy scripts
└── README.md
```

---

# Maven Module Hierarchy

## Parent POM Structure

```
shree-ai-os (pom.xml)
│
├── platform/core/shree-platform-core (jar)
│   ├── api
│   ├── model
│   ├── validator
│   ├── error
│   ├── service
│   └── engine
│
├── platform/kernels/shree-kernel-identity (jar)
│   └── depends on: shree-platform-core
│
├── platform/kernels/shree-kernel-memory (jar)
│   └── depends on: shree-platform-core
│
├── platform/kernels/shree-kernel-knowledge (jar)
│   └── depends on: shree-platform-core
│
├── platform/kernels/shree-kernel-planning (jar)
│   └── depends on: shree-platform-core
│
├── platform/kernels/shree-kernel-reasoning (jar)
│   └── depends on: shree-platform-core
│
├── platform/kernels/shree-kernel-capability (jar)
│   └── depends on: shree-platform-core
│
├── sdk/shree-sdk (jar)
│   └── depends on: shree-platform-core, all kernels
│
├── shree-bootstrap (war)
│   └── depends on: shree-sdk, shree-platform-core
│
├── shree-assistant (war)
│   └── depends on: shree-sdk, shree-platform-core
│
└── smart-campus-connect (war)
    └── depends on: shree-sdk, shree-platform-core
```

---

# Dependency Graph

## Allowed Dependencies

```
Application (Spring Boot)
    ↓
SDK
    ↓
Platform Core ← → Kernel (via contracts only)
    ↓
Java Standard Library
```

## Forbidden Dependencies

```
Platform Core → Spring Boot (FORBIDDEN)
Platform Core → Kernel Implementation (FORBIDDEN)
Kernel → Kernel Implementation (FORBIDDEN)
Kernel → Application (FORBIDDEN)
SDK → Application (FORBIDDEN)
```

## Dependency Rules

### Layer 1: Platform Core (shree-platform-core)
- **Dependencies:** Java 21 standard library only
- **Forbidden:** Spring, JPA, Hibernate, external frameworks
- **Purpose:** Framework-independent infrastructure

### Layer 2: Kernels (shree-kernel-*)
- **Dependencies:** shree-platform-core, Java standard library
- **Forbidden:** Other kernels (except via contracts), Spring
- **Purpose:** Intelligent capabilities

### Layer 3: SDK (shree-sdk)
- **Dependencies:** shree-platform-core, all kernels (contracts only)
- **Forbidden:** Application frameworks
- **Purpose:** Developer interface

### Layer 4: Applications (shree-bootstrap, shree-assistant, smart-campus-connect)
- **Dependencies:** shree-sdk, shree-platform-core, Spring Boot
- **Forbidden:** Direct kernel implementation access
- **Purpose:** End-user applications

---

# Module Specifications

## 1. shree-platform-core

**Type:** Framework-independent library (jar)

**Package Structure:**
```
platform.core.registry/
    ├── api/
    ├── model/
    ├── validator/
    ├── error/
    ├── service/
    ├── engine/
    └── test/

platform.core.discovery/
    ├── api/
    ├── model/
    ├── validator/
    ├── error/
    ├── service/
    ├── engine/
    └── test/

platform.core.lifecycle/
    ├── api/
    ├── model/
    ├── validator/
    ├── error/
    ├── service/
    ├── engine/
    └── test/

platform.core.event/
    ├── api/
    ├── model/
    ├── validator/
    ├── error/
    ├── service/
    ├── engine/
    └── test/

platform.core.config/
    ├── api/
    ├── model/
    ├── validator/
    ├── error/
    ├── service/
    ├── engine/
    └── test/

platform.core.health/
    ├── api/
    ├── model/
    ├── validator/
    ├── error/
    ├── service/
    ├── engine/
    └── test/
```

**Dependencies:**
- Java 21
- JUnit 5 (test scope)
- AssertJ (test scope)

**Forbidden:**
- Spring Boot
- JPA/Hibernate
- REST annotations
- External frameworks

---

## 2. shree-kernel-identity

**Type:** Framework-independent library (jar)

**Package Structure:**
```
platform.kernel.identity/
    ├── api/
    ├── model/
    ├── service/
    ├── engine/
    └── test/
```

**Dependencies:**
- shree-platform-core
- Java 21

**Purpose:** Identity management kernel

---

## 3. shree-kernel-memory

**Type:** Framework-independent library (jar)

**Package Structure:**
```
platform.kernel.memory/
    ├── api/
    ├── model/
    ├── service/
    ├── engine/
    └── test/
```

**Dependencies:**
- shree-platform-core
- Java 21

**Purpose:** Memory management kernel

---

## 4. shree-kernel-knowledge

**Type:** Framework-independent library (jar)

**Package Structure:**
```
platform.kernel.knowledge/
    ├── api/
    ├── model/
    ├── service/
    ├── engine/
    └── test/
```

**Dependencies:**
- shree-platform-core
- Java 21

**Purpose:** Knowledge management kernel

---

## 5. shree-kernel-planning

**Type:** Framework-independent library (jar)

**Package Structure:**
```
platform.kernel.planning/
    ├── api/
    ├── model/
    ├── service/
    ├── engine/
    └── test/
```

**Dependencies:**
- shree-platform-core
- Java 21

**Purpose:** Planning and orchestration kernel

---

## 6. shree-kernel-reasoning

**Type:** Framework-independent library (jar)

**Package Structure:**
```
platform.kernel.reasoning/
    ├── api/
    ├── model/
    ├── service/
    ├── engine/
    └── test/
```

**Dependencies:**
- shree-platform-core
- Java 21

**Purpose:** Reasoning and inference kernel

---

## 7. shree-kernel-capability

**Type:** Framework-independent library (jar)

**Package Structure:**
```
platform.kernel.capability/
    ├── api/
    ├── model/
    ├── service/
    ├── engine/
    └── test/
```

**Dependencies:**
- shree-platform-core
- Java 21

**Purpose:** Capability management kernel

---

## 8. shree-sdk

**Type:** Framework-independent library (jar)

**Package Structure:**
```
platform.sdk/
    ├── client/
    ├── builder/
    ├── config/
    └── test/
```

**Dependencies:**
- shree-platform-core
- All kernels (contracts only)
- Java 21

**Purpose:** Developer-facing SDK

---

## 9. shree-bootstrap

**Type:** Spring Boot application (war)

**Package Structure:**
```
app.bootstrap/
    ├── config/
    ├── controller/
    ├── service/
    └── test/
```

**Dependencies:**
- shree-sdk
- shree-platform-core
- Spring Boot Web
- Spring Boot Security

**Purpose:** Platform bootstrap application

---

## 10. shree-assistant

**Type:** Spring Boot application (war)

**Package Structure:**
```
app.assistant/
    ├── config/
    ├── controller/
    ├── service/
    └── test/
```

**Dependencies:**
- shree-sdk
- shree-platform-core
- Spring Boot Web

**Purpose:** AI Assistant application

---

## 11. smart-campus-connect

**Type:** Spring Boot application (war)

**Package Structure:**
```
app.smartcampus/
    ├── config/
    ├── controller/
    ├── service/
    └── test/
```

**Dependencies:**
- shree-sdk
- shree-platform-core
- Spring Boot Web

**Purpose:** Smart Campus Connect application

---

# Parent POM Configuration

## Java Version
- Java 21 (LTS)

## Maven Coordinates
- Group ID: `platform.shree.ai.os`
- Artifact ID: `shree-ai-os`
- Version: `1.0.0-SNAPSHOT`

## Dependency Management

### Platform Core Dependencies
```xml
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter</artifactId>
    <version>5.10.0</version>
    <scope>test</scope>
</dependency>

<dependency>
    <groupId>org.assertj</groupId>
    <artifactId>assertj-core</artifactId>
    <version>3.24.2</version>
    <scope>test</scope>
</scope>
</dependency>
```

### Application Dependencies
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
    <version>3.2.0</version>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
    <version>3.2.0</version>
</dependency>

<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
    <version>2.15.2</version>
</dependency>
```

---

# Build Order

## Maven Build Sequence

1. **shree-platform-core** (no internal dependencies)
2. **shree-kernel-identity** (depends on core)
3. **shree-kernel-memory** (depends on core)
4. **shree-kernel-knowledge** (depends on core)
5. **shree-kernel-planning** (depends on core)
6. **shree-kernel-reasoning** (depends on core)
7. **shree-kernel-capability** (depends on core)
8. **shree-sdk** (depends on core + all kernels)
9. **shree-bootstrap** (depends on sdk + core)
10. **shree-assistant** (depends on sdk + core)
11. **smart-campus-connect** (depends on sdk + core)

---

# Framework Boundaries

## Platform Core & Kernels
- **NO Spring Boot**
- **NO REST controllers**
- **NO JPA/Hibernate**
- **NO external frameworks**
- Pure Java 21 with standard library only

## Applications
- Spring Boot permitted
- REST controllers permitted
- JPA/Hibernate permitted (if needed)
- External frameworks permitted

## SDK
- Framework-independent
- No Spring dependencies
- No application frameworks

---

# Architectural Principles

## AP-001 — Framework Isolation
Platform Core and Kernels remain framework-independent.

## AP-002 — Dependency Direction
Dependencies flow downward only:
Applications → SDK → Kernels → Platform Core → Java

## AP-003 — Contract-Only Communication
Kernels communicate through contracts, never implementations.

## AP-004 — No Circular Dependencies
The dependency graph remains acyclic.

## AP-005 — Single Responsibility
Each module owns exactly one architectural concern.

---

# Migration Strategy

## Phase 1: Repository Creation
1. Create new monorepo structure
2. Create parent pom.xml
3. Create module directories

## Phase 2: Platform Core Migration
1. Implement Kernel Registry (EIO-001)
2. Implement other Platform Core services
3. Verify framework independence

## Phase 3: Kernel Migration
1. Migrate existing kernels to new structure
2. Verify kernel contracts
3. Verify dependency rules

## Phase 4: Application Migration
1. Migrate applications to new module structure
2. Verify Spring Boot isolation
3. Verify SDK integration

## Phase 5: Cleanup
1. Remove old project structure
2. Update documentation
3. Version freeze

---

# Success Criteria

✓ Parent POM defines all modules
✓ Dependency graph is acyclic
✓ Platform Core has zero framework dependencies
✓ Kernels depend only on Platform Core
✓ Applications depend on SDK and Platform Core
✓ Build order is deterministic
✓ Framework boundaries are enforced
✓ Repository structure matches PROJECT-001

---

# Constitutional Authority

Derived from:
- CONST-001
- ADD-201
- ADD-202
- ADD-205
- ADD-207
- KERNEL-005
- KERNEL-006
- STD-003
- ADR-001
- PROJECT-001

---

Platform: Shree AI OS

Maintained By: Chief AI Architect

Architecture Layer: Platform Engineering

End of MONOREPO-ARCHITECTURE-V1