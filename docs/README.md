# Shree AI OS

### Build Intelligent Java Applications with Memory, Knowledge, Planning & Multi-Agent Runtime

[![Maven Central](https://img.shields.io/maven-central/v/io.github.darshanrathod04/shree-ai-os?color=007ec6\&label=Maven%20Central)](https://central.sonatype.com/artifact/io.github.darshanrathod04/shree-ai-os)
[![Java](https://img.shields.io/badge/Java-21%2B-blue.svg)](https://openjdk.org/projects/jdk/21/)
[![License](https://img.shields.io/badge/License-Proprietary%20Evaluation-orange.svg)](LICENSE)

> **Developer Preview v1.0.5** • Java 21 • Spring Boot • Maven Central

---

## What is Shree AI OS?

**Shree AI OS** is a privacy-first AI Runtime Platform for Java that enables developers to build intelligent applications inside their own JVM.

Instead of treating prompts as the architecture, Shree AI OS provides a deterministic runtime with memory, knowledge retrieval, planning, reasoning, reflection, identity management, and multi-provider inference.

**Design Principle**

> *The LLM generates language. The runtime makes decisions.*

---

## Why Shree AI OS?

### Traditional AI

`User → Prompt → LLM → Response`

### Shree AI OS

`User → Runtime → Memory → Knowledge → Planning → Reasoning → Reflection → LLM → Grounded Response`

This architecture makes AI applications more explainable, testable, and extensible.

---

## Core Features

| Capability               | Description                                        |
| ------------------------ | -------------------------------------------------- |
| **Memory SDK**           | Episodic, semantic & conversational memory         |
| **Knowledge SDK**        | Hybrid RAG with citation-based retrieval           |
| **Planning SDK**         | Structured execution planning                      |
| **Reasoning Engine**     | Evidence-grounded deterministic reasoning          |
| **Reflection Engine**    | Self-evaluation & execution analytics              |
| **Identity SDK**         | Request-scoped identity resolution                 |
| **Project SDK**          | Java project understanding & architecture analysis |
| **Real Token Streaming** | Live streaming from OpenAI, Gemini & Ollama        |
| **BYOK**                 | Bring Your Own API Key with hot reload             |
| **Runtime Event Bus**    | Publish/subscribe intelligent workflows            |

---

## 5-Layer Architecture

```text
Application
      │
SDK Layer
Memory • Knowledge • Planning • Reflection • Identity
      │
Runtime Orchestration
Intent Router • Multi-Agent • Event Bus • LLM Router
      │
Kernel Layer
Memory • Knowledge • Planning • Execution • Cognitive
      │
Providers
Gemini • OpenAI • Ollama • In-Memory
```

---

# Quick Start

## 1. Install

### Maven

```xml
<dependency>
    <groupId>io.github.darshanrathod04</groupId>
    <artifactId>shree-ai-os</artifactId>
    <version>1.0.5-developer-preview</version>
</dependency>
```

### Gradle

```gradle
implementation("io.github.darshanrathod04:shree-ai-os:1.0.5-developer-preview")
```

---

## 2. Create the Runtime

```java
import com.shreeai.os.platform.sdk.ShreeAI;

ShreeAI shree = ShreeAI.builder()
        .apiKey("local")
        .build();
```

---

## 3. Chat with the Runtime

```java
import com.shreeai.os.platform.sdk.SDKResponse;

SDKResponse response = shree.chat(
    "Create a roadmap for a student management system."
);

System.out.println(response.answer());
System.out.println(response.confidence());
```

---

# SDK Response

```java
String answer = response.answer();
double confidence = response.confidence();
boolean grounded = response.reasoningAvailable();
String metadata = response.metadata();
Map<String, Object> payload = response.structuredPayload();
Instant timestamp = response.timestamp();
```

The structured payload exposes rich runtime context while preserving backward compatibility.

---

# Memory Example

```java
shree.memory().store(
    "preferred-language",
    "Java"
);

var memories = shree.memory().recall("language");
```

---

# Knowledge (Hybrid RAG)

```java
shree.knowledge().ingest(
    "Java is a programming language created by James Gosling."
);

var results = shree.knowledge().search(
    "programming language"
);
```

---

# Planning

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

# Real Token Streaming

```java
shree.chatStream(
    "Explain JVM architecture",
    token -> System.out.print(token)
);
```

Supported providers:

* OpenAI
* Google Gemini
* Ollama

Automatic provider fallback is built into the runtime.

---

# Build Applications Like

* Developer Copilot
* Enterprise Knowledge Assistant
* AI Customer Support
* Medical Intelligence
* Financial Analytics
* Fitness Coach
* Education Platforms

---

# Public SDKs

| SDK           | Purpose                        |
| ------------- | ------------------------------ |
| MemorySDK     | Memory management              |
| KnowledgeSDK  | Document ingestion & retrieval |
| PlanningSDK   | Planning & execution           |
| ReasoningSDK  | Evidence-based reasoning       |
| ReflectionSDK | Runtime reflection & analytics |
| InferenceSDK  | Structured inference           |
| IdentitySDK   | Identity resolution            |
| ExecutionSDK  | Workflow execution             |
| ProjectSDK    | Java project intelligence      |
| SettingsSDK   | BYOK & provider configuration  |

---

# Runtime Highlights

* 11-stage orchestration pipeline
* Multi-agent execution
* Hybrid RAG retrieval
* Deterministic reasoning
* Real token streaming
* BYOK hot reload
* Runtime event bus
* Tenant boundary enforcement

---

## Documentation

| Document                            | Purpose                                         |
|-------------------------------------|-------------------------------------------------|
| PLATFORM_IDENTITY.md                | Platform philosophy & runtime                   |
| QUICKSTART_DEVELOPER_GUIDE.md       | 5-minute tutorial                               |
| DEVELOPER_CAPABILITIES.md           | Complete SDK reference                          |
| WORKING_STATUS.md                   | Verification report                             |
| COGNITIVE_RUNTIME_ARCHITECTURE.md   | Hybrid RAG, ONNX, pgvector & runtime internals  |
---

# Project Status

**Developer Preview v1.0.5**

Current focus:

* Stable public SDK
* Production-ready runtime APIs
* Developer documentation
* Real developer feedback

> During the Developer Preview, the public API is considered **feature-frozen**. Future releases will be driven primarily by real developer feedback.

---

# Philosophy

> **AI should be infrastructure, not just prompts.**

Shree AI OS combines deterministic software engineering with modern language models to help developers build grounded, explainable, and extensible Java applications.

---

**Founder:** Darshan Rathod

**Language:** Java 21

**Distribution:** Maven Central

**Status:** Developer Preview v1.0.5
