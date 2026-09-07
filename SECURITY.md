# Security Policy

## Supported Versions

Shree AI OS is currently in **Developer Preview**. Security updates are provided only for the latest active preview release.

| Version | Supported |
|----------|-----------|
| `v1.0.5-developer-preview` | ✅ Yes |
| Earlier preview releases | ❌ No |

---

## Reporting a Vulnerability

If you discover a security vulnerability, **do not open a public GitHub Issue**.

Please use **GitHub Private Vulnerability Reporting** from the repository's **Security** tab.

Include the following information whenever possible:

- Clear description of the vulnerability
- Steps to reproduce
- Minimal proof-of-concept (PoC)
- Expected vs. actual behavior
- Potential security impact
- Affected runtime component (MemorySDK, Runtime, TenantIsolationEnforcer, SettingsSDK, etc.)

---

## Response Timeline

| Stage | Target |
|--------|--------|
| Acknowledgement | Within **48 hours** |
| Initial Assessment | Within **5 business days** |
| Severity Classification | As soon as assessment is complete |
| Security Fix | Prioritized according to severity |
| Public Disclosure | Coordinated with the patch release |

---

## Security Principles

Shree AI OS is built around deterministic runtime safety. The following guarantees are considered security-critical.

### Tenant Isolation

Every request executes inside an active tenant context. Cross-tenant access is validated by `TenantIsolationEnforcer` before runtime execution and must fail with an isolation exception.

### BYOK Protection

API keys configured through `SettingsSDK` remain in runtime memory and are masked before storage. Keys must never appear in responses, logs, events, or diagnostic output.

### Deterministic Execution

Language models are treated as **text generation providers**, not execution authorities. Planning, validation, memory, and workflow execution are enforced by deterministic runtime kernels before any model-generated response is returned.

### Memory & Identity Boundaries

Memory recall, reflection history, and identity resolution operate strictly within the active tenant scope. Any isolation bypass is treated as a high-severity security vulnerability.

---

## In Scope

Examples of qualifying security reports include:

- Cross-tenant data access
- Memory isolation bypass
- API key disclosure
- Unauthorized workflow execution
- Event bus privilege escalation
- Identity boundary bypass
- Runtime authorization flaws

---

## Out of Scope

The following should be reported through normal GitHub Issues instead:

- Feature requests
- Documentation improvements
- Performance optimizations
- UI or playground bugs without security impact
- Refactoring suggestions

---

## Coordinated Disclosure

Please allow reasonable time for investigation and remediation before publicly disclosing a vulnerability. Security advisories will be published alongside the corresponding patch release whenever applicable.

---

**Project:** Shree AI OS

**Current Version:** Developer Preview v1.0.5

**Reporting Channel:** GitHub Private Vulnerability Reporting