 # Discovery Error Architecture

## Package
`platform.core.discovery.error`

## Purpose
Standardized error model for the Discovery Service that provides a consistent error architecture across Platform Core Services.

The error architecture mirrors the approved Registry Error Architecture pattern.

## Architectural Responsibility
- Provides a standardized error model for the Discovery Service.
- Defines error codes, structured error descriptions, and a base exception hierarchy.
- Mirrors the Registry Error Architecture for consistency across Platform Core Services.
- Ensures all discovery errors are consistent, typed, and documented.

## Ownership
**Platform Core**

## Constitutional Authority
- CONST-001
- KERNEL-006 — Kernel Discovery
- ADD-PLT-202 — Platform Core
- ADD-PLT-205 — Platform Core Services
- ADD-PLT-206 — Kernel Orchestration

## Error Model

### `DiscoveryErrorCode`
Standardized error codes for discovery operations.

| Code | Description |
|------|-------------|
| `DISCOVERY_CAPABILITY_NOT_FOUND` | The requested capability was not found in the platform |
| `DISCOVERY_CONTRACT_NOT_FOUND` | The requested contract was not found in the platform |
| `DISCOVERY_INVALID_REQUEST` | The discovery request is invalid or malformed |
| `DISCOVERY_INCOMPATIBLE_VERSION` | The requested capability or contract version is incompatible |
| `DISCOVERY_VALIDATION_FAILED` | Discovery validation failed — the request does not satisfy prerequisites |

### `DiscoveryError`
Immutable error description containing code, message, timestamp, and optional details.

| Method | Returns | Description |
|--------|---------|-------------|
| `code()` | `DiscoveryErrorCode` | The error code |
| `message()` | `String` | The human-readable error message |
| `timestamp()` | `Instant` | The instant when the error occurred |
| `details()` | `Map<String, Object>` | Optional error details (unmodifiable) |

### `DiscoveryException`
Base exception for all Discovery Service errors. Extends `RuntimeException`.

| Method | Returns | Description |
|--------|---------|-------------|
| `error()` | `DiscoveryError` | The associated discovery error |
| `code()` | `DiscoveryErrorCode` | The error code |
| `getMessage()` | `String` | The error message |
| `timestamp()` | `Instant` | The error timestamp |
| `details()` | `Map<String, Object>` | The error details |

**All future discovery exceptions SHALL extend this class.**

### Exception Hierarchy

| Exception | Extends | Error Code | Description |
|-----------|---------|------------|-------------|
| `CapabilityNotFoundException` | `DiscoveryException` | `DISCOVERY_CAPABILITY_NOT_FOUND` | Thrown when a capability is not found |
| `ContractNotFoundException` | `DiscoveryException` | `DISCOVERY_CONTRACT_NOT_FOUND` | Thrown when a contract is not found |
| `InvalidDiscoveryRequestException` | `DiscoveryException` | `DISCOVERY_INVALID_REQUEST` | Thrown when a request is invalid |

## Design Constraints
- **DiscoveryException is the ONLY base exception** — all future exceptions extend it
- **Immutable where applicable** — DiscoveryError is immutable
- **No business logic** — error definitions only
- **No Spring annotations** — framework-agnostic
- **No persistence** — no database or serialization annotations

## Relationship to the Registry Error Architecture
```
platform.core.registry.error                    platform.core.discovery.error
├── RegistryErrorCode                     ↔    ├── DiscoveryErrorCode
├── RegistryError                         ↔    ├── DiscoveryError
├── RegistryException                     ↔    ├── DiscoveryException
├── DuplicateKernelException              ↔    ├── CapabilityNotFoundException
├── KernelNotFoundException               ↔    ├── ContractNotFoundException
└── InvalidKernelException                ↔    └── InvalidDiscoveryRequestException
```

## Usage Example
```java
// Throwing an exception
throw new CapabilityNotFoundException("text-generation");

// Catching and handling
try {
    DiscoveryResult result = discoveryService.resolveByCapability(capabilityId);
} catch (CapabilityNotFoundException e) {
    System.err.println("Capability not found: " + e.code());
    System.err.println("Error at: " + e.timestamp());
}
```

## Related Documents
- [EIO-201 — Discovery Service Public API](../api/README.md)
- [EIO-202 — Discovery Domain Models](../model/README.md)
- [EIO-204 — Discovery Validation](../validator/README.md)
- [EIO-104 — Registry Error Architecture](../../registry/error/README.md)
- [KERNEL-006 — Kernel Discovery](../../../../../../docs/architecture/kernel/KERNEL-006-KERNEL-DISCOVERY.md)
- [STD-002 — Kernel Development Standard](../../../../../../docs/engineering/standards/STD-002-KERNEL-DEVELOPMENT-STANDARD.MD)