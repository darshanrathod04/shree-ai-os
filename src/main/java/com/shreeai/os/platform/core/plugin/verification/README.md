# Plugin Verification

## Overview

Before any plugin can be loaded into Shree AI OS, it must pass verification. Think of it like:

- Android verifies APKs
- VS Code verifies extensions
- IntelliJ verifies plugins

Shree AI OS does the same.

## Responsibilities

The verifier answers:

- Is this plugin safe?
- Is metadata valid?
- Are dependencies satisfied?
- Is version compatible?
- Can this plugin run?

**Nothing else.** No loading. No execution. Only verification.

## Architecture

```
platform.core.plugin.verification
├── PluginVerifier.java           — Orchestrator
├── VerificationResult.java       — Complete report
├── VerificationIssue.java        — Single finding
├── VerificationSeverity.java     — INFO / WARNING / ERROR
├── PluginCompatibilityChecker.java — Runtime compatibility
└── PluginDependencyChecker.java  — Dependency availability
```

## Verification Pipeline

```
PluginDescriptor
        │
        ▼
PluginVerifier
        │
        ├── Metadata
        ├── Version
        ├── Dependencies
        ├── Compatibility
        └── Duplicate IDs
        │
        ▼
VerificationResult
```

### 1. Metadata Check

Validates required fields:
- **Plugin ID** — must start with a letter, contain only letters/digits/dots/hyphens/underscores
- **Plugin Name** — must be between 1 and 128 characters
- **Version** — must follow semantic versioning (X.Y.Z)
- **Provider** — must not be null or empty

### 2. Version Check

Enforces semantic versioning:
- Accept: `1.0.0`, `2.5.1`, `0.9.8`
- Reject: `abc`, `one.two`, `1`, `1.0.beta`

### 3. Dependency Check

Verifies that all declared dependencies are available in the registry.

### 4. Compatibility Check

Checks:
- Minimum Java version
- Minimum platform version
- Maximum platform version
- Plugin API version

### 5. Duplicate Check

Rejects plugins with IDs that are already registered.

## VerificationResult

Instead of returning a boolean, returns a complete report:

```
VALID
Warnings
--------
- Plugin uses deprecated API
- Plugin compiled for Java 21
- Plugin has optional dependency missing
```

```
INVALID
Errors
------
- Plugin ID missing
- Version malformed
- Dependency "memory-engine" missing
```

## Design Principles

- **Stateless** — No mutable state
- **Thread-safe** — All classes immutable
- **Deterministic** — Same input always produces the same output
- **Zero side effects** — No logging, no loading, no filesystem, no network
- **No Spring, Lombok, or JPA** — Pure Java 21

## Usage Example

```java
// Create checkers
PluginDependencyChecker depChecker = new PluginDependencyChecker(
    Set.of("memory", "llm", "scheduler")
);
PluginCompatibilityChecker compatChecker = new PluginCompatibilityChecker(
    "21", "1.0.0", "1.0.0"
);

// Create verifier
PluginVerifier verifier = new PluginVerifier(
    depChecker, compatChecker, Set.of()
);

// Verify a plugin
VerificationResult result = verifier.verify(descriptor);

if (result.isValid()) {
    // Plugin is safe to load
} else {
    for (VerificationIssue issue : result.issues()) {
        System.out.println(issue.severity() + ": " + issue.message());
    }
}