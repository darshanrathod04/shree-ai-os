# Shree AI OS

### Build Intelligent Java Applications with Memory, Knowledge, Planning & Multi-Agent Runtime

> **Developer Preview v1.0** • Java 21 • Spring Boot • Privacy-First AI Runtime

---

## What is Shree AI OS?

Shree AI OS is an **AI Runtime Platform for Java** that helps developers build intelligent applications inside their own JVM.

Instead of treating an LLM as the center of your application, Shree AI OS provides a deterministic runtime with memory, knowledge retrieval, planning, reasoning, reflection, identity, and real-time streaming. Your application interacts with a structured runtime—not directly with prompts.

**Design Principle:** *The LLM is the final response generator, not the decision maker.*

---

## Why Shree AI OS?

Traditional AI applications:

`User → Prompt → LLM → Response`

Shree AI OS:

`User → Runtime → Memory → Knowledge → Planning → Reasoning → Reflection → LLM → Grounded Response`

This architecture allows your application to remain explainable, testable, and extensible.

---

## Core Features

| Capability               | Description                                                     |
| ------------------------ | --------------------------------------------------------------- |
| **Memory SDK**           | Store and recall episodic, semantic and conversational memories |
| **Knowledge SDK**        | Hybrid RAG with document ingestion and citation-based retrieval |
| **Planning SDK**         | Create, refine and validate structured execution plans          |
| **Reasoning Engine**     | Deterministic evidence-based reasoning pipeline                 |
| **Reflection Engine**    | Self-evaluation, analytics and learning history                 |
| **Identity SDK**         | Identity resolution with request-scoped context                 |
| **Project SDK**          | Analyze Java projects and understand architecture               |
| **Real Token Streaming** | Live streaming from Gemini, OpenAI and Ollama                   |
| **BYOK**                 | Bring Your Own API Key with runtime hot reload                  |
| **Event Bus**            | Publish/subscribe runtime events for intelligent workflows      |

---

## 5-Layer Architecture

`Application`

`↓`

`SDK Layer`

`Memory • Knowledge • Planning • Reflection • Identity`

`↓`

`Runtime Orchestration`

`Intent Router • Multi-Agent • Event Bus • LLM Router`

`↓`

`Kernel Layer`

`Memory • Knowledge • Planning • Execution • Cognitive`

`↓`

`Providers`

`Gemini • OpenAI • Ollama • In-Memory`

---

## Quick Start

### 1. Add the dependency

```xml
<dependency>
    <groupId>io.github.darshanrathod04</groupId>
    <artifactId>shree-ai-os</artifactId>
    <version>1.0.4-developer-preview</version>
</dependency>
```

### 2. Create the runtime

```java
import com.shreeai.os.platform.ShreeAI;

ShreeAI shree = ShreeAI.builder()
        .build();
```

### 3. Chat

```java
var response = shree.chat(
    "Create a 30 minute strength workout"
);

System.out.println(response.message());
```

---

## Memory Example

```java
shree.memory().store(
    "preferred-language",
    "Java"
);

var memories = shree.memory().recall("language");
```

---

## Knowledge RAG Example

```java
shree.knowledge().ingest(
    "Java is a programming language created by James Gosling."
);

var results = shree.knowledge().search(
    "programming language"
);
```

---

## Planning Example

```java
var plan = shree.planning()
        .createPlanTyped(
            "api",
            "Build REST API",
            PlanningScope.APPLICATION,
            PlanningConstraints.defaults()
        );
```

---

## Real Streaming

```java
shree.chatStream(
    "Explain JVM architecture",
    token -> System.out.print(token)
);
```

Supports live streaming from:

* Google Gemini
* OpenAI
* Ollama

with automatic provider fallback.

---

## Build Applications Like

* AI Customer Support
* Medical Intelligence
* Financial Analytics
* Developer Copilot
* Fitness Coach
* Education Platform
* Enterprise Knowledge Assistant

---

## Public SDKs

| SDK           | Purpose                       |
| ------------- | ----------------------------- |
| MemorySDK     | Memory management             |
| KnowledgeSDK  | Document ingestion & search   |
| PlanningSDK   | Planning & execution          |
| ReasoningSDK  | Deterministic reasoning       |
| ReflectionSDK | Reflection & analytics        |
| InferenceSDK  | Structured inference          |
| IdentitySDK   | Identity resolution           |
| ExecutionSDK  | Workflow execution            |
| ProjectSDK    | Java project intelligence     |
| SettingsSDK   | BYOK & provider configuration |

---

## Runtime Highlights

* 11-stage orchestration pipeline
* Multi-agent execution
* Hybrid RAG retrieval
* Real token streaming
* BYOK hot reload
* Tenant boundary enforcement
* Runtime event bus
* Project intelligence

---

## Documentation

| Guide                             | Description                      |
| --------------------------------- | -------------------------------- |
| **PLATFORM_IDENTITY.md**          | Runtime architecture & lifecycle |
| **DEVELOPER_CAPABILITIES.md**     | Complete SDK reference           |
| **WORKING_STATUS.md**             | Verified implementation status   |
| **QUICKSTART_DEVELOPER_GUIDE.md** | End-to-end developer tutorial    |

---

## Project Status

**Developer Preview v1.0**

Current focus:

* Stable public SDK
* Production-ready runtime APIs
* Developer feedback
* Performance & usability improvements

No breaking API changes are planned during the Developer Preview unless required by critical feedback.

---

## Philosophy

> **AI should be infrastructure, not just prompts.**

Shree AI OS combines deterministic software engineering with modern language models to help developers build intelligent, grounded, and extensible Java applications.

---

**Founder:** Darshan Rathod

**Language:** Java 21

**Status:** Developer Preview v1.0
