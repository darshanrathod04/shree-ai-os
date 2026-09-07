# Platform Identity

### What Shree AI OS Is

**Developer Preview v1.0.5**

Shree AI OS is a **deterministic AI orchestration runtime for Java**. It enables Java applications to build intelligent systems where memory, knowledge retrieval, planning, reasoning, reflection, identity, and execution are coordinated by a runtime before any language model generates a response.

The platform follows one core principle:

> **The LLM is the final response generator, not the decision maker.**

---

# Platform Overview

Shree AI OS is distributed as a single Maven library for **Java 21+** and integrates naturally into Spring Boot or any JVM application.

A typical request flows through the runtime like this:

1. Application submits a request through the SDK.
2. Runtime resolves intent and loads context.
3. Memory and Knowledge provide grounded information.
4. Planning and Reasoning determine the execution path.
5. Reflection validates the outcome.
6. The LLM generates the final natural-language response.

This architecture separates **deterministic software logic** from **probabilistic language generation**, making intelligent applications more explainable and testable.

---

# Five-Layer Architecture

| Layer           | Responsibility                                                                             |
| --------------- | ------------------------------------------------------------------------------------------ |
| **Application** | Your Java application, REST API, or desktop/server system                                  |
| **SDK**         | Memory, Knowledge, Planning, Reasoning, Reflection, Identity, Execution, Project, Settings |
| **Runtime**     | Intent routing, orchestration, event bus, multi-agent coordination, LLM routing            |
| **Kernel**      | Core services for memory, knowledge, planning, execution, cognitive intelligence           |
| **Providers**   | OpenAI, Gemini, Ollama, OpenAI-compatible providers, In-Memory fallback                    |

Architecture summary:

```text
Application
      │
      ▼
SDK Layer
(Memory • Knowledge • Planning • Reflection • Identity)
      │
      ▼
Runtime Orchestration
(Intent Router • Multi-Agent • Event Bus • LLM Router)
      │
      ▼
Kernel Services
(Memory • Knowledge • Planning • Execution • Cognitive)
      │
      ▼
LLM Providers
(OpenAI • Gemini • Ollama • In-Memory)
```

---

# Runtime Orchestration

The runtime is the operating layer of Shree AI OS. Rather than sending prompts directly to a model, every request passes through coordinated services responsible for routing, planning, execution, and validation.

Core runtime components include:

* **ShreeClient** — Public entry point for chat, streaming, and execution requests.
* **DefaultRuntimeService** — Central orchestration engine.
* **RuntimeIntentRouter** — Deterministic capability routing.
* **ChiefIntelligenceAgent** — 11-stage execution pipeline.
* **MultiKernelOrchestrator** — Parallel execution of multiple intents.
* **RuntimeEventBus** — Publish/subscribe event system.
* **LlmRouter** — Provider selection with automatic fallback.

---

# The 11-Stage Intelligence Pipeline

When deterministic routing is insufficient, the runtime executes a complete orchestration pipeline.

| Stage | Purpose             |
| ----- | ------------------- |
| 1     | Identity Resolution |
| 2     | Context Loading     |
| 3     | Memory Recall       |
| 4     | Knowledge Retrieval |
| 5     | Reasoning           |
| 6     | Inference           |
| 7     | Planning            |
| 8     | Action Execution    |
| 9     | Reflection          |
| 10    | Memory Storage      |
| 11    | Chief Review        |

Reflection can trigger selective retries before the final response is generated, allowing the runtime to improve execution quality without exposing that complexity to the application.

---

# LLM Provider Layer

Shree AI OS supports multiple providers behind a unified routing interface.

| Provider          | Purpose                                |
| ----------------- | -------------------------------------- |
| OpenAI            | Cloud inference                        |
| Gemini            | Cloud inference                        |
| Ollama            | Local models                           |
| OpenAI-Compatible | Custom endpoints                       |
| In-Memory         | Deterministic fallback for development |

If multiple providers are configured, the runtime automatically falls back according to the configured provider chain.

Example:

```text
openai → gemini → in-memory
```

Applications continue working even if the primary provider becomes unavailable.

---

# Event-Driven Runtime

Every major runtime capability can publish events through the built-in event bus.

Examples include:

* Knowledge ingestion
* Memory updates
* Planning completion
* Execution lifecycle
* Reflection results

Applications can subscribe without coupling directly to kernel implementations.

```java
shree.eventBus().subscribe(
    EventType.KNOWLEDGE_INGESTED,
    event -> System.out.println(event.getEntryId())
);
```

---

# Tenant & Identity Model

Shree AI OS uses **request-scoped identity and tenant isolation**.

Each execution carries:

* Identity ID
* Session ID
* Application ID
* Workspace ID
* Tenant context

Tenant boundaries are enforced inside the runtime before protected operations execute, preventing cross-tenant access within the same runtime instance.

---

# Public Entry Point

Add the library:

```xml
<dependency>
    <groupId>io.github.darshanrathod04</groupId>
    <artifactId>shree-ai-os</artifactId>
    <version>1.0.5-developer-preview</version>
</dependency>
```

Create the runtime:

```java
ShreeAI shree = ShreeAI.builder()
    .apiKey(System.getenv("OPENAI_API_KEY"))
    .build();

ChatResponse reply = shree.chat("Plan a 30 minute workout");

shree.close();
```

The runtime initializes automatically and manages its complete lifecycle internally.

---

# Design Philosophy

Traditional AI applications typically follow:

```text
User → Prompt → LLM → Response
```

Shree AI OS introduces deterministic orchestration:

```text
User
 ↓
Identity
 ↓
Memory
 ↓
Knowledge
 ↓
Reasoning
 ↓
Planning
 ↓
Execution
 ↓
Reflection
 ↓
LLM
 ↓
Grounded Response
```

The language model becomes one component of the system rather than the system itself.

---

# What Shree AI OS Is Not

To clarify the platform scope:

* **Not** a vector database
* **Not** a model training or fine-tuning platform
* **Not** a chat wrapper around OpenAI
* **Not** limited to Spring Boot
* **Not** a SaaS platform

It is a **Java runtime for deterministic AI orchestration**.

---

# Summary

Shree AI OS provides a stable runtime that combines deterministic software engineering with modern language models.

Its public platform includes:

* Memory & Knowledge
* Planning & Execution
* Reasoning & Reflection
* Identity & Tenant Context
* Multi-Agent Runtime
* Event Bus
* Real Token Streaming
* BYOK Provider Routing

**Everything before the LLM is deterministic Java infrastructure.** That is the defining identity of Shree AI OS.
