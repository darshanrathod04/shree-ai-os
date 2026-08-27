# SDK Design Report
## EO-V1-SDK1-001 - Shree AI OS SDK Foundation

**Report Date:** 2026-08-07  
**Engineering Order:** EO-V1-SDK1-001  
**Status:** COMPLETE

---

## Design Overview

The SDK is designed as a clean adapter layer between external developers and the Shree AI OS platform. It exposes only stable public APIs while keeping all internal kernel implementations hidden.

---

## Architecture

```
Developer
    ↓
ShreeAI (entry point)
    ↓
ShreeClient (core client)
    ↓
SDKConfiguration / SDKRequest / SDKResponse
    ↓
Runtime (internal - not exposed)
    ↓
Platform Kernels (internal - not exposed)
```

---

## Public API

### ShreeAI

```java
ShreeAI ai = ShreeAI.builder()
        .apiKey("local")
        .build();

SDKResponse response = ai.chat("Hello");
```

### ShreeBuilder

```java
ShreeAI.builder()
    .apiKey("local")
    .runtime(runtime)
    .configuration(config)
    .build();
```

### ShreeClient

```java
client.chat("message");
client.chat(SDKRequest);
client.configuration();
client.runtime();
```

---

## Models

### SDKRequest (Immutable)

- message (required)
- context
- metadata (Map)
- sessionId
- userId

### SDKResponse (Immutable)

- answer (required)
- confidence (0.0-1.0)
- reasoningAvailable
- metadata
- timestamp

### SDKConfiguration (Immutable)

- apiKey
- timeout
- locale
- debug
- runtimeMode
- version

---

## Exception Hierarchy

```
SDKException (RuntimeException)
    ├── ConfigurationException
    └── ValidationException
```

---

## Version Module

```java
SDKVersion.VERSION = "1.0.0-V1"
SDKVersion.BUILD = "V1-RT1"
SDKVersion.COMPATIBILITY = "Shree AI OS V1"
```

---

## Design Principles

1. **Small API** - Only 6 public classes
2. **Clean API** - No kernel classes exposed
3. **Stable API** - Immutable models, builder patterns
4. **Simple API** - `ai.chat("Hello")` works without kernel knowledge
5. **Safe API** - All exceptions wrapped in SDKException hierarchy

---

## Status: COMPLETE