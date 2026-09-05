# Quickstart Developer Guide — 5-Minute Tutorial

> **Goal:** Get a working Shree AI OS application in under 5 minutes. Every code sample below compiles against the verified public API.

---

## Prerequisites

- **Java 21** (the project targets Java 21)
- **Maven 3.8+** (or use the included `mvnw` wrapper)
- An LLM API key (optional) — the runtime works without one using `InMemoryLlmProvider`

---

## Step 1: Add the Dependency

Add this to your `pom.xml`:

```xml
<dependency>
    <groupId>io.github.darshanrathod04</groupId>
    <artifactId>shree-ai-os</artifactId>
    <version>1.0.3-developer-preview</version>
</dependency>
```

**Note:** Shree AI OS is a Spring Boot 4.0.2 application, so Spring Boot dependencies are pulled in transitively. You don't need to add Spring Boot explicitly unless you're building a standalone Spring app.

---

## Step 2: Your First Chat (30 seconds)

Create a file `HelloShree.java`:

```java
import com.shreeai.os.platform.ShreeAI;
import com.shreeai.os.platform.sdk.chat.ChatResponse;

public class HelloShree {
    public static void main(String[] args) {
        ShreeAI shree = ShreeAI.builder()
            .configuration(RuntimeConfiguration.defaults())
            .build();

        ChatResponse reply = shree.chat("What is Java?");
        System.out.println(reply.getMessage());

        shree.close();
    }
}
```

**What happens:**
1. `ShreeAI.builder()` creates a `ShreeBuilder`.
2. `.build()` instantiates a `DefaultRuntimeService`, calls `initialize()` then `start()`.
3. `.chat()` builds an `IntelligenceContextBuilder` payload, submits it to the runtime, and returns the response.
4. `.close()` stops the runtime and releases resources.

**Run it:**
```bash
mvn compile exec:java -Dexec.mainClass="HelloShree"
```

**Expected output** (without an API key, uses `InMemoryLlmProvider`):
```
Java is a high-level, object-oriented programming language...
```

---

## Step 3: Configure an LLM Provider (1 minute)

### Option A: OpenAI

Set the environment variable:
```bash
export OPENAI_API_KEY=sk-...
```

Then:
```java
ShreeAI shree = ShreeAI.builder()
    .apiKey(System.getenv("OPENAI_API_KEY"))
    .build();
```

### Option B: Google Gemini

```bash
export GEMINI_API_KEY=your-gemini-key
```

### Option C: Ollama (local)

```bash
export SHREE_LLM_OLLAMA=true
```

**What happens:** `buildDefaultLlmRouter()` (called during `DefaultRuntimeService` initialization) detects the environment variable and registers the corresponding provider. The router chains providers with auto-fallback (e.g., `openai,in-memory` if OpenAI is available, otherwise `in-memory`).

---

## Step 4: Store and Recall Memories (30 seconds)

```java
ShreeAI shree = ShreeAI.builder().build();

// Store a memory
shree.memory().store("user-name", "Alice");

// Recall memories
List<MemoryEntry> memories = shree.memory().recall("name");
for (MemoryEntry entry : memories) {
    System.out.println(entry.getKey() + " = " + entry.getValue());
}
// Output: user-name = Alice

shree.close();
```

**Runtime path:** `MemorySDK.store()` → `DefaultMemoryService.store()` (direct delegation). The memory is stored in an in-memory repository.

---

## Step 5: Ingest and Search Knowledge (30 seconds)

```java
ShreeAI shree = ShreeAI.builder().build();

// Ingest knowledge
KnowledgeEntry entry = shree.knowledge().ingest(
    "Java is a programming language created by James Gosling in 1995."
);

// Search the knowledge graph
List<KnowledgeEntry> results = shree.knowledge().search("programming language");
for (KnowledgeEntry e : results) {
    System.out.println(e.getContent());
}

shree.close();
```

**Runtime path:** `KnowledgeSDK.ingest()` → `DefaultKnowledgeService.ingest(content, tenantId="default")`. The entry is stored in the in-memory knowledge graph.

---

## Step 6: Create and Execute a Plan (1 minute)

```java
ShreeAI shree = ShreeAI.builder().build();

// Create a plan
Plan plan = shree.planning().createPlan("Build a REST API for user management");
System.out.println("Plan created with " + plan.getSteps().size() + " steps");

// Execute the plan
ExecutionResult result = shree.planning().executePlan(plan);
System.out.println("Execution status: " + result.getStatus());

shree.close();
```

**Runtime path:** `PlanningSDK.createPlan()` → `DefaultPlanningService.createPlan(goal)`. The plan is stored in memory. `PlanningSDK.executePlan()` → `DefaultExecutionService.execute(plan)`.

---

## Step 7: Analyze a Project (1 minute)

```java
import java.nio.file.Path;
import java.nio.file.Paths;

ShreeAI shree = ShreeAI.builder().build();

// Analyze a Java project
Path projectRoot = Paths.get("./my-java-project");
ProjectAnalysis analysis = shree.project().analyze(projectRoot);
System.out.println("Project has " + analysis.getClassCount() + " classes");

// Find a specific controller
JavaClassInfo controller = shree.project().findController("UserController");
if (controller != null) {
    System.out.println("Found controller: " + controller.getFullyQualifiedName());
}

// Get a summary
ProjectSummary summary = shree.project().summarize();
System.out.println(summary.getDescription());

shree.close();
```

**Important:** `ProjectSDK.analyze()` takes `java.nio.file.Path`, **not `String`**. Use `Paths.get(...)` to convert.

**Runtime path:** `ProjectSDK.analyze()` → `ProjectIntelligenceService.analyze(projectRoot)`. The service scans the project directory and extracts Java class metadata.

---

## Step 8: Multi-Intent Orchestration (30 seconds)

```java
ShreeAI shree = ShreeAI.builder().build();

// Submit a multi-intent request
ExecutionRequest request = ExecutionRequest.builder()
    .addIntent("MEMORY_RECALL", Map.of("query", "user preferences"))
    .addIntent("SEARCH_KNOWLEDGE", Map.of("query", "Java"))
    .build();

ExecutionResult result = shree.submit(request);
System.out.println("Orchestrated " + result.getIntentResults().size() + " intents");

shree.close();
```

**Runtime path:** `ShreeClient.submit()` → `DefaultRuntimeService.submit()` → `IntentAnalyzer` (detects multi-intent) → `MultiKernelOrchestrator.orchestrate()` (fans out to memory + knowledge kernels in parallel) → aggregates results.

---

## Step 9: Subscribe to Events (30 seconds)

```java
ShreeAI shree = ShreeAI.builder().build();

// Subscribe to knowledge ingestion events
shree.eventBus().subscribe(EventType.KNOWLEDGE_INGESTED, event -> {
    System.out.println("Knowledge ingested: " + event.getEntryId());
});

// Trigger an event
shree.knowledge().ingest("Python is a programming language.");
// Output: Knowledge ingested: <some-uuid>

shree.close();
```

**Runtime path:** `ShreeAI` creates a `RuntimeEventBus` instance → `ShreeClient` calls `runtime.bindEventBus(eventBus)` → `DefaultRuntimeService` registers internal consumers → your subscription receives events.

---

## Step 10: Real Provider Token Streaming (30 seconds)

```java
ShreeAI shree = ShreeAI.builder()
    .apiKey(System.getenv("OPENAI_API_KEY"))
    .build();

shree.chatStream("Tell me a story about Java", new StreamingListener() {
    @Override public void onStart() { System.out.print(">>> "); }
    @Override public void onToken(String token) { System.out.print(token); }
    @Override public void onComplete(String complete) {
        System.out.println("\n[stream complete, " + complete.length() + " chars]");
    }
    @Override public void onError(Throwable t) { t.printStackTrace(); }
});

shree.close();
```

**What happens:** `ShreeClient.chatStream()` calls `Runtime.streamText()`, which invokes `llmRouter.stream(LlmRequest)`. The LLM provider (e.g., `OpenAiProvider`) consumes the SSE stream and yields each token via `StreamingListener.onToken()` in real time. Without a configured API key, `InMemoryLlmProvider` yields a deterministic token list.

---

## Step 11: Configure BYOK with Hot Reload (30 seconds)

```java
ShreeAI shree = ShreeAI.builder().build();

// Configure a custom API key — takes effect immediately (hot reload)
ProviderSettings openai = shree.settings().configureApiKey(ProviderType.OPENAI, "sk-custom-key");
System.out.println("Key configured: " + openai.maskedKey());  // sk-****

// List all configured providers
List<ProviderSettings> providers = shree.settings().providers();
for (ProviderSettings p : providers) {
    System.out.println(p.providerType() + " = " + p.maskedKey());
}

shree.close();
```

**What happens:** `SettingsSDK.configureApiKey()` calls `ByokSettingsService.save()`, which fires a `CHANGE_EVENT`. `DefaultRuntimeService.rebuildLlmRouter()` is registered as a listener — it receives the event and prepends the new provider to the LLM chain. The next request uses the new key.

**Note:** `ProviderType` is an enum in `com.shreeai.os.platform.sdk.settings`. Use `ProviderType.OPENAI`, `ProviderType.GEMINI`, `ProviderType.OLLAMA`, etc. See [DEVELOPER_CAPABILITIES.md](DEVELOPER_CAPABILITIES.md) §10 for full details.

---

## Complete Example: Putting It All Together

```java
import com.shreeai.os.platform.ShreeAI;
import com.shreeai.os.platform.sdk.chat.ChatResponse;
import com.shreeai.os.platform.sdk.memory.MemoryEntry;
import com.shreeai.os.platform.sdk.knowledge.KnowledgeEntry;
import com.shreeai.os.platform.sdk.planning.Plan;
import com.shreeai.os.platform.sdk.planning.ExecutionResult;
import com.shreeai.os.platform.runtime.event.EventType;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class ShreeCompleteExample {
    public static void main(String[] args) {
        // 1. Build and start the runtime
        ShreeAI shree = ShreeAI.builder()
            .apiKey(System.getenv("OPENAI_API_KEY"))  // optional
            .build();

        // 2. Subscribe to events
        shree.eventBus().subscribe(EventType.KNOWLEDGE_INGESTED, event -> {
            System.out.println("[EVENT] Knowledge ingested: " + event.getEntryId());
        });

        // 3. Store memories
        shree.memory().store("user-name", "Bob");
        shree.memory().store("user-role", "developer");

        // 4. Ingest knowledge
        shree.knowledge().ingest("Maven is a build automation tool for Java projects.");
        shree.knowledge().ingest("Spring Boot is a framework for building Java applications.");

        // 5. Recall memories
        List<MemoryEntry> memories = shree.memory().recall("user");
        System.out.println("Recalled " + memories.size() + " memories");

        // 6. Search knowledge
        List<KnowledgeEntry> results = shree.knowledge().search("Java");
        System.out.println("Found " + results.size() + " knowledge entries");

        // 7. Create and execute a plan
        Plan plan = shree.planning().createPlan("Deploy a Java app to production");
        ExecutionResult result = shree.planning().executePlan(plan);
        System.out.println("Plan execution: " + result.getStatus());

        // 8. Chat with the runtime
        ChatResponse reply = shree.chat("What did I store about the user?");
        System.out.println("Chat response: " + reply.getMessage());

        // 8b. Stream real tokens from the LLM
        shree.chatStream("Summarize the project in one sentence", new StreamingListener() {
            @Override public void onStart() { }
            @Override public void onToken(String token) { System.out.print(token); }
            @Override public void onComplete(String complete) { System.out.println(); }
            @Override public void onError(Throwable t) { t.printStackTrace(); }
        });

        // 9. Multi-intent orchestration
        ExecutionRequest request = ExecutionRequest.builder()
            .addIntent("MEMORY_RECALL", Map.of("query", "user"))
            .addIntent("SEARCH_KNOWLEDGE", Map.of("query", "deployment"))
            .build();
        ExecutionResult orchestrated = shree.submit(request);
        System.out.println("Orchestrated " + orchestrated.getIntentResults().size() + " intents");

        // 10. Lifecycle management
        RuntimeStatus status = shree.status();
        System.out.println("Runtime state: " + status.getState());

        // 11. Shutdown
        shree.close();
    }
}
```

**Run it:**
```bash
mvn compile exec:java -Dexec.mainClass="ShreeCompleteExample"
```

---

## Reference Applications

Two reference applications are included in the repository:

1. **`shree-playground`** — A Spring Boot application that exposes Shree AI OS via REST endpoints. Use it to test the runtime via HTTP.
2. **`shree-developer-intelligence`** — A developer-focused application that uses `ProjectSDK` to analyze Java projects.

To run the playground:
```bash
cd application/shree-playground
mvn spring-boot:run
```

Then test it:
```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "What is Java?"}'
```

---

## Common Pitfalls

### 1. `ProjectSDK.analyze()` takes `Path`, not `String`

❌ **Wrong:**
```java
shree.project().analyze("./my-project");
```

✅ **Correct:**
```java
import java.nio.file.Path;
import java.nio.file.Paths;

Path projectRoot = Paths.get("./my-project");
shree.project().analyze(projectRoot);
```

### 2. Don't forget to close the runtime

❌ **Wrong:**
```java
ShreeAI shree = ShreeAI.builder().build();
shree.chat("Hello");
// Runtime never stops
```

✅ **Correct:**
```java
ShreeAI shree = ShreeAI.builder().build();
try {
    shree.chat("Hello");
} finally {
    shree.close();
}
```

### 3. BYOK now uses hot reload (no rebuild needed)

If you call `shree.settings().configureApiKey(ProviderType.OPENAI, "sk-...")`, the new key is registered with the LLM router immediately via `rebuildLlmRouter()`. There is no need to set `OPENAI_API_KEY` before building the runtime:

```java
ShreeAI shree = ShreeAI.builder().build();
shree.start();

// Set the key at any time — it takes effect on the next LLM call
shree.settings().configureApiKey(ProviderType.OPENAI, "sk-...");

// Subsequent calls use the new key
ChatResponse reply = shree.chat("Hello");
```

---

## Next Steps

- **[PLATFORM_IDENTITY.md](PLATFORM_IDENTITY.md)** — Understand the 5-layer architecture and 11-stage pipeline
- **[DEVELOPER_CAPABILITIES.md](DEVELOPER_CAPABILITIES.md)** — Browse the complete SDK catalog
- **[WORKING_STATUS.md](WORKING_STATUS.md)** — See what's verified, partial, or decorative

---

## Summary

In 5 minutes, you've learned how to:

✅ Add the Maven dependency
✅ Build and start the runtime
✅ Chat with the LLM (or use the in-memory fallback)
✅ Store and recall memories
✅ Ingest and search knowledge
✅ Create and execute plans
✅ Analyze Java projects
✅ Orchestrate multi-intent requests
✅ Subscribe to events
✅ Stream real LLM tokens (not simulated)
✅ Configure BYOK with hot reload
✅ Manage the runtime lifecycle

**The LLM is the last step, not the first.** Everything before it is deterministic Java code. That's the core design principle of Shree AI OS.
