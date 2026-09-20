# Quickstart Developer Guide — 5-Minute Tutorial

> **Goal:** Get a production-grade Shree AI OS application running in under 5 minutes. Every code snippet below is verified against the `1.0.6-developer-preview` release.

---

## 1. Prerequisites

- **Java 21+** (Eclipse Temurin / Adoptium recommended)
- **Maven 3.8+** (or the included `./mvnw` wrapper)
- **Docker** (optional, required if running PostgreSQL + pgvector locally)
- An API Key (Google Gemini, OpenAI) or run offline using the built-in deterministic `InMemoryLlmProvider`

---

## 2. Step 1: Add Maven Dependency

Add the following dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>io.github.darshanrathod04</groupId>
    <artifactId>shree-ai-os</artifactId>
    <version>1.0.6-developer-preview</version>
</dependency>
```

---

## 3. Step 2: Initialize Shree AI OS

Create a standard Java class `QuickstartApp.java`:

```java
import com.shreeai.os.platform.sdk.ShreeAI;
import com.shreeai.os.platform.sdk.SDKResponse;

public class QuickstartApp {
    public static void main(String[] args) {
        // Initialize the runtime with your Gemini API key (or use "local" for in-memory)
        ShreeAI shree = ShreeAI.builder()
            .apiKey(System.getenv().getOrDefault("GEMINI_API_KEY", "local"))
            .build();

        // 1. Grounded Chat
        SDKResponse response = shree.chat("What is the core architectural principle of Shree AI OS?");
        System.out.println("Answer: " + response.answer());

        // Always gracefully close the runtime on shutdown
        shree.close();
    }
}
```

Compile and run:
```bash
mvn compile exec:java -Dexec.mainClass="QuickstartApp"
```

---

## 4. Step 3: Grounded Chat with Citations

When knowledge or documents are ingested, Shree AI OS performs hybrid RRF vector retrieval and grounds its answers with verifiable citations:

```java
ShreeAI shree = ShreeAI.builder()
    .apiKey(System.getenv("GEMINI_API_KEY"))
    .build();

// Ingest documentation into pgvector
shree.knowledge().ingest(
    "Shree AI OS uses Reciprocal Rank Fusion (RRF) to combine HNSW semantic vector rankings " +
    "with PostgreSQL full-text search rankings into a single authoritative evidence set."
);

// Execute grounded query
SDKResponse groundedResponse = shree.chat("How does hybrid retrieval work in Shree AI OS?");
System.out.println("Response:\n" + groundedResponse.answer());

// Print citations
if (groundedResponse.citations() != null) {
    groundedResponse.citations().forEach(c -> {
        System.out.println("Citation -> [" + c.title() + "] " + c.excerpt() + " (Score: " + c.score() + ")");
    });
}

shree.close();
```

---

## 5. Step 4: Episodic Memory Recall

Store and semantically recall conversation state, user preferences, and session facts:

```java
ShreeAI shree = ShreeAI.builder().build();

// Store episodic facts
shree.memory().store("user-tech-stack", "Enterprise Java 21, Spring Boot 3.4, and PostgreSQL pgvector");
shree.memory().store("user-region", "us-east-1");

// Semantically recall memories
List<MemoryEntry> memories = shree.memory().recall("What database and runtime does the user use?");
for (MemoryEntry entry : memories) {
    System.out.println("Recalled: " + entry.getKey() + " -> " + entry.getValue());
}

shree.close();
```

---

## 6. Step 5: Planning Graph Dispatch (Topological DAG)

Decompose high-level engineering objectives into structured, topologically ordered execution graphs:

```java
ShreeAI shree = ShreeAI.builder().build();

// Generate a structured execution plan
SDKResponse planResponse = shree.planning().createPlan(
    "plan-001",
    "Deploy microservice to Kubernetes cluster with zero downtime",
    "infrastructure"
);

System.out.println("Planning Status: " + planResponse.answer());
Map<String, Object> payload = planResponse.structuredPayload();
System.out.println("Generated Plan Details: " + payload);

shree.close();
```

---

## 7. Step 6: Real Provider Token Streaming

Stream tokens fragment-by-fragment directly from the underlying LLM provider:

```java
ShreeAI shree = ShreeAI.builder()
    .apiKey(System.getenv("GEMINI_API_KEY"))
    .build();

shree.chatStream("Explain how pgvector HNSW indexing accelerates vector similarity search", new StreamingListener() {
    @Override
    public void onStart() {
        System.out.print("Stream started >>> ");
    }

    @Override
    public void onToken(String token) {
        System.out.print(token);
    }

    @Override
    public void onComplete(String fullContent) {
        System.out.println("\n<<< Stream completed (" + fullContent.length() + " chars)");
    }

    @Override
    public void onError(Throwable t) {
        System.err.println("Streaming error: " + t.getMessage());
    }
});

shree.close();
```

---

## 8. Step 7: Hot-Reload BYOK Keys

Configure or swap API keys at runtime without restarting the application:

```java
ShreeAI shree = ShreeAI.builder().build();

// Dynamically configure an OpenAI key at runtime
ProviderSettings openaiSettings = shree.settings().configureApiKey(
    ProviderType.OPENAI, 
    "sk-proj-abc123xyz"
);
System.out.println("OpenAI Key Active: " + openaiSettings.maskedKey());

// Dynamically configure a Gemini key
ProviderSettings geminiSettings = shree.settings().configureApiKey(
    ProviderType.GEMINI,
    "AIzaSyD-custom-key"
);
System.out.println("Gemini Key Active: " + geminiSettings.maskedKey());

// Subsequent calls automatically use the newly configured provider
SDKResponse reply = shree.chat("Hello from dynamically configured provider!");
System.out.println(reply.answer());

shree.close();
```

---

## 9. Configuration Properties

Configure the runtime via `application.properties` or standard environment variables:

| Property | Environment Variable | Default | Description |
|---|---|---|---|
| `shree.llm.provider` | `SHREE_LLM_PROVIDER` | `gemini` | Primary provider (`gemini`, `openai`, `ollama`, `in-memory`) |
| `shree.llm.gemini.model` | `SHREE_LLM_GEMINI_MODEL` | `gemini-2.5-flash` | Gemini model (`gemini-2.5-flash`, `gemini-1.5-flash`, etc.) |
| `shree.llm.openai.model` | `SHREE_LLM_OPENAI_MODEL` | `gpt-4o` | OpenAI model identifier |
| `shree.llm.retry.max` | `SHREE_LLM_RETRY_MAX` | `3` | Maximum retry attempts on HTTP 429/503 |
| `shree.llm.retry.backoff-ms` | `SHREE_LLM_RETRY_BACKOFF_MS` | `2000` | Base exponential backoff duration in milliseconds |
| `shree.vector.provider` | `SHREE_VECTOR_PROVIDER` | `pgvector` | Vector store (`pgvector` or `in-memory`) |
| `shree.vector.jdbc.url` | `SHREE_VECTOR_JDBC_URL` | `jdbc:postgresql://localhost:5432/shree` | PostgreSQL JDBC connection URL |
| `shree.vector.jdbc.user` | `SHREE_VECTOR_JDBC_USER` | `postgres` | Database username |
| `shree.vector.jdbc.password` | `SHREE_VECTOR_JDBC_PASSWORD` | `shreeai` | Database password |
| `shree.embedding.provider` | `SHREE_EMBEDDING_PROVIDER` | `onnx` | Embeddings (`onnx` local or `gemini`) |
| `shree.embedding.dimensions`| `SHREE_EMBEDDING_DIMENSIONS`| `384` | Embedding vector dimensionality |

---

## 10. Common Developer Questions

### Q: Does Shree AI OS work offline without internet?
**Yes.** Set `shree.llm.provider=in-memory` (or `apiKey("local")`) and `shree.embedding.provider=onnx`. The entire cognitive pipeline, hybrid vector search, memory recall, and deterministic responses run 100% locally on your JVM with zero network traffic.

### Q: How do I integrate with Spring Boot?
Declare `ShreeAI` as a `@Bean`:
```java
@Configuration
public class ShreeConfiguration {
    @Bean
    public ShreeAI shreeAI(@Value("${gemini.api.key:local}") String apiKey) {
        return ShreeAI.builder()
            .apiKey(apiKey)
            .build();
    }
}
```

---

Platform: **Shree AI OS**  
Document: **Quickstart Developer Guide**  
Version: **1.0.6-developer-preview**  
Author: **Darshan Rathod**
