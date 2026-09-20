# Security Policy

## Supported Versions

Shree AI OS is currently distributed as a **Developer Preview**. Security updates and patches are actively provided for the latest release:

| Version | Supported | Status |
|---|---|---|
| `1.0.6-developer-preview` | ✅ Yes | **Active (Current Release)** |
| `1.0.5-developer-preview` | ❌ No | Deprecated |
| Earlier preview releases | ❌ No | Discontinued |

---

## Reporting a Vulnerability

We take the security of Shree AI OS seriously. If you discover a potential security flaw, vulnerability, or boundary bypass:

> **DO NOT disclose vulnerabilities publicly in issues, discussions, or social media.**

Please report all vulnerabilities via **GitHub Private Vulnerability Reporting** under the **Security** tab of the repository.

### Information to Include in Your Report:
1. **Description:** Clear and concise explanation of the vulnerability.
2. **Steps to Reproduce:** Exact steps, payload, or execution request that triggers the issue.
3. **Proof-of-Concept (PoC):** Minimal Java code sample or test case demonstrating the defect.
4. **Impact Assessment:** Explanation of potential consequences (e.g., privilege escalation, boundary escape, credential leakage).
5. **Affected Subsystem:** Target component (e.g., `DefaultRuntimeService`, `TenantIsolationEnforcer`, `SettingsSDK`, `ByokSettingsService`, `MemorySDK`, etc.).

---

## Response Timeline & Disclosure SLA

| Stage | Target Timeline |
|---|---|
| **Acknowledgement** | Within **48 hours** |
| **Initial Assessment & Classification** | Within **5 business days** |
| **Remediation & Patch Development** | Prioritized according to severity (P0 Critical within 72 hours) |
| **Coordinated Public Disclosure** | Published concurrently with the official patch release |

---

## Core Security Architecture & Principles

Shree AI OS is designed around deterministic, in-process runtime safety. The platform incorporates multiple defensive layers to protect host applications and tenant data:

### 1. Fail-Closed Authorization Architecture
All tool calls, code modifications, terminal interactions, and execution graph steps must pass through the platform's central authorization gate (`DefaultRuntimeService.graphPermissionManager`):
- Execution capabilities are explicitly mapped to policy actions (`ALLOW`, `REQUIRE_APPROVAL` / `ASK_USER`, `DENY`).
- **Fail-Closed Guarantee:** If any evaluation encounters a `NullPointerException`, `IllegalArgumentException`, missing capability mapping, or unexpected runtime fault, the gate strictly returns `PermissionDecision.DENY`.
- The system never defaults to open access under abnormal conditions.

### 2. Multi-Tenant Isolation Boundaries
- Every request carries an authenticated `TenantContext` containing `tenantId`, `identityId`, `sessionId`, and `workspaceId`.
- Access across memory stores, session caches, reflection records, and knowledge graphs is guarded by `TenantIsolationEnforcer`.
- Vector tables in PostgreSQL enforce strict `tenant_id TEXT` partitioning, making cross-tenant data leakage physically impossible.

### 3. Bring-Your-Own-Key (BYOK) Credential Protection
- API keys configured via `SettingsSDK` are retained strictly in volatile in-process memory.
- Keys are masked before being exposed through any diagnostic, event, or logging surface (e.g. `sk-****3xyz`).
- Plaintext API credentials are never logged, serialized to disk, or broadcast over the `RuntimeEventBus`.

### 4. Deterministic Pre-LLM Guardrails
- Large Language Models are treated strictly as probabilistic text generators, **never as execution authorities**.
- Goal decomposition, validation, safety policy enforcement, and code patch verification occur deterministically in Java before any prompt or response reaches a model.

---

## Scope of Security Reports

### Qualifying In-Scope Reports:
- Cross-tenant data leaks or namespace boundary escapes
- Fail-closed gate bypasses or unauthorized capability execution
- API credential or secret leakage in logs, responses, or events
- Serialization vulnerabilities or remote code execution vectors
- Concurrency race conditions leading to corrupted security states
- Denial-of-service vulnerabilities via unhandled thread pool exhaustion or pipe buffer deadlocks

### Out-of-Scope Reports:
- Attacks requiring full root or OS-level file system access to the host machine
- Normal bug reports, UI issues, or test assertion failures without security impact
- Non-exploitable static analysis warnings
- Prompt injection against open-ended conversational fallbacks (Mode B) where no system tool execution is permitted

---

**Project:** Shree AI OS  
**Current Release:** `1.0.6-developer-preview`  
**Security Contact:** GitHub Private Vulnerability Reporting