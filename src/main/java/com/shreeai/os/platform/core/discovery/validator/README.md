# Discovery Validation

## Package
`platform.core.discovery.validator`

## Purpose
Validation layer for the Discovery Service that ensures discovery requests satisfy all architectural requirements before capability resolution.

The validator answers the question: *"Can this discovery request be processed?"*
It never answers: *"Resolve the capability."*

## Architectural Responsibility
- Ensures discovery requests satisfy all architectural requirements before capability resolution.
- Returns structured validation results supporting multiple errors in a single execution.
- Reuses the approved Registry validation architecture.
- Remains independent of the discovery implementation and registry.

## Ownership
**Platform Core**

## Constitutional Authority
- CONST-001
- KERNEL-006 — Kernel Discovery
- ADD-PLT-202 — Platform Core
- ADD-PLT-205 — Platform Core Services
- ADD-PLT-206 — Kernel Orchestration

## Public Contracts

### `DiscoveryValidator`
Stateless validator for discovery request readiness.

| Method | Returns | Description |
|--------|---------|-------------|
| `validateCapabilityId(CapabilityId)` | `ValidationResult` | Validates a capability identifier |
| `validateContractId(ContractId)` | `ValidationResult` | Validates a contract identifier |
| `validateDiscoveryResult(DiscoveryResult)` | `ValidationResult` | Validates a discovery result for consistency |
| `validateCapabilityMetadata(CapabilityId, Object)` | `ValidationResult` | Validates capability metadata (method stub) |

## Validation Rules

### `validateCapabilityId(CapabilityId)` performs:
| Rule | Type | Description |
|------|------|-------------|
| CapabilityId exists | Error | CapabilityId must not be null |
| CapabilityId format | Error | Must match pattern `^[a-zA-Z0-9-]+$` |

### `validateContractId(ContractId)` performs:
| Rule | Type | Description |
|------|------|-------------|
| ContractId exists | Error | ContractId must not be null |
| ContractId format | Error | Must match pattern `^[a-zA-Z0-9-]+$` |

### `validateDiscoveryResult(DiscoveryResult)` performs:
| Rule | Type | Description |
|------|------|-------------|
| DiscoveryResult exists | Error | DiscoveryResult must not be null |
| ResolutionStatus valid | Error | ResolutionStatus must not be null |
| CapabilityId present | Error | DiscoveryResult capabilityId must not be null |
| KernelId present | Error | DiscoveryResult kernelId must not be null |
| ContractId present | Error | DiscoveryResult contractId must not be null |
| FOUND consistency | Warning | FOUND status should have all fields populated |

### `validateCapabilityMetadata(CapabilityId, Object)` performs:
| Rule | Type | Description |
|------|------|-------------|
| Capability metadata consistency | Stub | Method stub for future implementation |

## Design Constraints
- **Stateless** — all state is passed as method parameters
- **Deterministic** — same inputs always produce the same result
- **Reuses ValidationResult** — uses existing `platform.core.registry.validator.ValidationResult`
- **No business logic** — validation rules only
- **No model mutation** — models are never modified
- **Never performs discovery** — validation only
- **Never accesses Registry** — independent validation layer

## Relationship to the Discovery API
```
platform.core.discovery.api.DiscoveryService
                            |
                            | uses (via implementation)
                            v
          platform.core.discovery.validator.DiscoveryValidator
                            |
                            | validates
                            v
          platform.core.discovery.model.DiscoveryResult
                            |
                            +--- CapabilityId
                            +--- ContractId
                            +--- ResolutionStatus
                            +--- KernelId
                            |
                            | returns
                            v
          platform.core.registry.validator.ValidationResult
```

## Related Documents
- [EIO-201 — Discovery Service Public API](../api/README.md)
- [EIO-202 — Discovery Domain Models](../model/README.md)
- [EIO-103 — Kernel Registration Validation](../../registry/validator/README.md)
- [KERNEL-006 — Kernel Discovery](../../../../../../docs/architecture/kernel/KERNEL-006-KERNEL-DISCOVERY.md)
- [STD-002 — Kernel Development Standard](../../../../../../docs/engineering/standards/STD-002-KERNEL-DEVELOPMENT-STANDARD.MD)