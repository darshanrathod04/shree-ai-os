# Contributing to Shree AI OS

First of all, thank you for taking the time to contribute to **Shree AI OS**.

This project is currently in **Developer Preview v1.0**. Our priority is stability, clean architecture, and high-quality developer experience—not rapid feature growth.

---

## Before You Start

Please make sure you have:

* Java 21
* Maven 3.9+
* Git
* Docker (optional, only for pgvector integration tests)

Clone the repository and verify the build:

```bash
git clone https://github.com/shree-ai/shree-ai-os.git
cd shree-ai-os

mvn clean test
```

A successful build should complete without failures.

---

## Development Principles

Every contribution should respect these principles.

### 1. Runtime First

Business logic belongs inside the runtime and kernel layers—not inside the SDK.

**Correct**

* Runtime orchestration
* Kernel implementation
* Engine improvements

**Avoid**

* Heavy logic inside SDK facades
* Controller-specific implementations
* Prompt-only solutions

---

### 2. Preserve Public APIs

The following SDKs are considered public:

* MemorySDK
* KnowledgeSDK
* PlanningSDK
* ReflectionSDK
* IdentitySDK
* InferenceSDK
* ExecutionSDK
* ProjectSDK
* SettingsSDK

Avoid breaking method signatures during the Developer Preview.

---

### 3. Write Deterministic Code

Shree AI OS is designed around deterministic execution.

Prefer:

* Typed models
* Immutable records
* Clear runtime ownership
* Structured results

Avoid hidden global state and unpredictable side effects.

---

## Repository Structure

```text
src/main/java/com/shreeai/os/

platform/
 ├── sdk/
 ├── runtime/
 ├── kernels/
 ├── llm/
 └── core/

application/
 ├── shree-playground/
 └── shree-developer-intelligence/
```

---

## Pull Request Checklist

Before opening a PR, ensure:

* Code compiles
* Existing tests pass
* New behavior includes tests
* No public API is broken
* Documentation is updated when needed

Checklist:

* [ ] `mvn clean test`
* [ ] No compilation warnings introduced
* [ ] Public SDK unchanged (or documented)
* [ ] Documentation updated
* [ ] Clear PR description

---

## Reporting Bugs

Please include:

* Java version
* Operating system
* Maven version
* Reproduction steps
* Expected behavior
* Actual behavior
* Stack trace (if available)

Use GitHub Issues instead of discussions for reproducible bugs.

---

## Suggesting Features

During Developer Preview, feature requests are welcome, but they should explain:

1. Problem being solved
2. Why existing SDK is insufficient
3. Proposed developer API
4. Example usage

Focus on developer ergonomics rather than adding new AI capabilities.

---

## Coding Style

* Java 21
* Constructor injection
* Immutable models where possible
* Clear method names
* Small focused classes
* JUnit 5 tests

Follow the existing project conventions instead of introducing new patterns.

---

## Documentation

If your contribution changes behavior, update the appropriate document:

| Document                        | Purpose                        |
| ------------------------------- | ------------------------------ |
| `README.md`                     | Public project overview        |
| `PLATFORM_IDENTITY.md`          | Runtime architecture           |
| `DEVELOPER_CAPABILITIES.md`     | SDK reference                  |
| `WORKING_STATUS.md`             | Verified implementation status |
| `QUICKSTART_DEVELOPER_GUIDE.md` | Developer tutorial             |

Documentation should always reflect the real source code.

---

## Community

Be respectful and constructive.

We're building Shree AI OS as a long-term developer platform, and thoughtful feedback is more valuable than large feature requests.

Thank you for contributing ❤️
