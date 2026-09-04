# Security Policy

## Supported Versions

Shree AI OS is currently in **Developer Preview**. Security fixes are provided exclusively for the latest active preview release.

| Version | Supported |
| :--- | :--- |
| `v1.0.0-developer-preview` | ✅ Yes |
| Earlier preview versions | ❌ No |

---

## Reporting a Vulnerability

If you discover a security vulnerability, please **do not create a public GitHub issue**.

Use **GitHub Private Vulnerability Reporting** directly from the repository's **Security** tab. When submitting a report, please include:

* Clear description of the vulnerability
* Concrete steps to reproduce
* Minimal proof-of-concept (PoC) code or request payload
* Expected vs actual behavior
* Potential architectural or security impact
* Affected components (`MemorySDK`, `TenantIsolationEnforcer`, `ByokSettingsService`, `Runtime`, etc.)

---

## Response Timeline

| Stage | Target |
| :--- | :--- |
| **Acknowledgement** | Within 48 hours |
| **Initial Assessment** | Within 5 business days |
| **Fix Development** | Prioritized based on severity |
| **Coordinated Disclosure** | Published alongside the patch release |

---

## Security Principles

Shree AI OS is designed around deterministic runtime safety. The following behaviors are treated as security-critical:

### 1. Tenant Isolation
Cross-tenant access is strictly validated by `TenantIsolationEnforcer` before runtime execution. Any request attempting to access or mutate another tenant's resources must fail immediately with an access exception.

### 2. BYOK Protection
API keys configured through `SettingsSDK` reside in memory and must never be exposed through chat responses, runtime logs, or event bus notifications.

### 3. Deterministic Execution
The LLM is treated as an untrusted text generation component, not an execution authority. Planning, validation, memory, and execution decisions are strictly enforced by runtime kernels before model output is returned.

### 4. Memory Boundaries
Memory operations, reflection histories, and identity resolutions operate strictly within the active tenant context. Isolation violations are classified as high-severity security defects.

---

## In-Scope Vulnerabilities

Examples of qualifying security vulnerabilities include:

* Cross-tenant data access or isolation leaks
* Memory partition bypasses
* API key exposure or reflection leaks
* Unauthorized or unvalidated workflow execution
* Event bus privilege escalation
* Identity boundary bypasses

> *Note: Functional bugs, documentation typos, and feature requests should be submitted via standard GitHub Issues.*

---

**Project:** Shree AI OS  
**Security Channel:** GitHub Private Vulnerability Reporting  
**Status:** Developer Preview v1.0