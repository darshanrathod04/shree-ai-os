# Sprint 17.2 — Runtime Fix Specification

**Date:** February 9, 2026  
**Companion to:** `SPRINT17_2_RUNTIME_AUDIT.md`  
**Status:** Fix specification (not yet implemented)

---

## Root Cause

The chat system has a fundamental architectural mismatch:

- `AiChatService` calls `KnowledgeSDK.query()` — designed for a **knowledge graph** of ingested documents
- The **project analysis** data (797 classes, 37 endpoints) lives in `ProjectSDK` — never called
- The knowledge graph is **empty** in production
- The only path to actual project code analysis is `ProjectSDK`, not `KnowledgeSDK`

The `KnowledgeSDK.query()` also sets the message to the literal string `"KNOWLEDGE_QUERY"`, which the response synthesizer renders as `# KNOWLEDGE_QUERY` when no knowledge results are found.

---

---

## Fix Option A — Full Project Intelligence (Recommended)

**Files to modify:**

| File | Change | Type |
|------|--------|------|
| `application/.../developer/chat/AiChatService.java` | Inject `ProjectSDK`, add `tryProjectIntelligence()` | Service logic |
| `application/.../developer/infrastructure/ShreeAiOsConfig.java` | Already has `ProjectSDK` bean ✅ | None needed |

**Constructor change (line 44-48):**

```java
private final ShreeAI shreeAi;
private final ProjectSDK projectSdk;  // ADD

public AiChatService(ShreeAI shreeAi, ProjectSDK projectSdk) {
    this.shreeAi = Objects.requireNonNull(shreeAi, "shreeAi");
    this.projectSdk = Objects.requireNonNull(projectSdk, "projectSdk");  // ADD
}
```

**`ask()` method change (lines 60-87):**

Replace the body of `ask()` with:

```java
public ChatResponse ask(String sessionId, String question) {
    Objects.requireNonNull(question, "question");
    Objects.requireNonNull(sessionId, "sessionId");
    log.info("Chat ask [session={}]: {}", sessionId, truncate(question, 80));

    // 1. Try project intelligence first (actual code analysis)
    String projectAnswer = tryProjectIntelligence(sessionId, question);

    // 2. Knowledge graph fallback
    String knowledgeQuery = buildKnowledgeQuery(question);
    SDKResponse knowledge = shreeAi.knowledge().query(knowledgeQuery);
    SDKResponse memory = shreeAi.memory().recall("project:" + sessionId + " " + question);

    String answer;
    double confidence;
    boolean knowledgeUsed;

    if (projectAnswer != null) {
        answer = buildProjectAnswer(projectAnswer, memory);
        confidence = 0.95;
        knowledgeUsed = false;
    } else if (knowledge.answer() != null
            && !knowledge.answer().isBlank()
            && !knowledge.answer().startsWith("# KNOWLEDGE_QUERY")) {
        answer = buildAnswer(knowledge, memory);
        confidence = Math.min(knowledge.confidence(), memory.confidence());
        knowledgeUsed = true;
    } else {
        answer = buildAnswer(knowledge, memory);
        confidence = 0.1;
        knowledgeUsed = false;
    }

    return ChatResponse.builder()
            .sessionId(sessionId)
            .question(question)
            .answer(answer)
            .confidence(confidence)
            .knowledgeUsed(knowledgeUsed)
            .memoryUsed(hasMemoryContent(memory))
            .timestamp(Instant.now())
            .build();
}
```

**New methods to add:**

```java
private String tryProjectIntelligence(String sessionId, String question) {
    String q = question.toLowerCase(Locale.ROOT);

    // "explain X class", "what is X"
    if (q.contains("explain") || q.contains("class") || q.contains("what is")) {
        String className = extractClassName(question);
        if (className != null) {
            ProjectClass cls = projectSdk.findClass(className);
            if (cls != null) return formatClassExplanation(cls);
        }
    }

    // "which class depends on X", "what depends on"
    if (q.contains("depend") || q.contains("impact")) {
        String className = extractClassName(question);
        if (className != null) {
            ProjectImpact imp = projectSdk.impact(className);
            if (imp != null) return formatImpact(imp);
        }
    }

    return null;
}

private String extractClassName(String question) {
    int classIdx = question.toLowerCase(Locale.ROOT).indexOf(" class");
    if (classIdx > 0) {
        String before = question.substring(0, classIdx).trim();
        String[] parts = before.split("\\s+");
        if (parts.length > 0) return parts[parts.length - 1];
    }
    if (question.contains("Controller") || question.contains("Service")
            || question.contains("Repository") || question.contains("SDK")) {
        for (String word : question.split("\\s+")) {
            if (word.endsWith("Controller") || word.endsWith("Service")
                    || word.endsWith("Repository") || word.endsWith("SDK")) {
                return word;
            }
        }
    }
    return null;
}

private String formatClassExplanation(ProjectClass cls) {
    StringBuilder sb = new StringBuilder();
    sb.append("# ").append(cls.getName()).append("\n\n");
    if (cls.getPackageName() != null) {
        sb.append("**Package:** `").append(cls.getPackageName()).append("`\n\n");
    }
    if (cls.getDescription() != null && !cls.getDescription().isBlank()) {
        sb.append(cls.getDescription()).append("\n\n");
    }
    return sb.toString();
}

private String formatImpact(ProjectImpact impact) {
    StringBuilder sb = new StringBuilder();
    sb.append("# Impact Analysis: ").append(impact.getTargetClass()).append("\n\n");
    if (impact.getAffectedFiles() != null && !impact.getAffectedFiles().isEmpty()) {
        sb.append("**Affected files (").append(impact.getAffectedFiles().size()).append("):**\n\n");
        for (String file : impact.getAffectedFiles().stream().limit(10).toList()) {
            sb.append("- `").append(file).append("`\n");
        }
    }
    if (impact.getDependencyDepth() > 0) {
        sb.append("\n**Dependency depth:** ").append(impact.getDependencyDepth()).append("\n");
    }
    return sb.toString();
}

private String buildProjectAnswer(String projectAnswer, SDKResponse memory) {
    StringBuilder sb = new StringBuilder(projectAnswer);
    if (hasMemoryContent(memory)) {
        sb.append("\n\n---\n**Project Memory:**\n").append(memory.answer());
    }
    return sb.toString();
}
```

**Add imports:**

```java
import com.shreeai.os.platform.kernels.project.model.ProjectClass;
import com.shreeai.os.platform.kernels.project.model.ProjectImpact;
import java.util.Locale;
```

**Regression risk:** LOW — adds new code paths; existing knowledge/memory fallback preserved.

---



## Fix Option B — Quick Fix (1-Line)

Apply this immediately to stop the placeholder from appearing.

**File:** `src/main/java/com/shreeai/os/platform/sdk/KnowledgeSDK.java` line 39:

```java
// BEFORE:
.message("KNOWLEDGE_QUERY")
// AFTER:
.message(question)
```

**What this fixes:** The synthesizer renders the message as `# KNOWLEDGE_QUERY`. Changing to the actual question means the heading will be the question text instead.

**What this does NOT fix:** The knowledge graph is still empty. The synthesizer will still say "I couldn't find relevant information" or produce an empty answer — but it will no longer show the literal placeholder.

**Risk:** MEDIUM — changes the message semantics. The runtime may have routing logic that depends on the "KNOWLEDGE_QUERY" message.

---

## Fix Option C — Direct ProjectSDK Full Integration

Add `explain(question)` to `ProjectSDK` and route all chat through it.

**File:** `src/main/java/com/shreeai/os/platform/sdk/ProjectSDK.java` (add method):

```java
/**
 * Answers a natural language question about the analyzed project.
 *
 * @param question the question (e.g. "Explain WorkspaceController")
 * @return a markdown-formatted answer
 * @since Sprint-17.2
 */
public String explain(String question) {
    Objects.requireNonNull(question, "question must not be null");
    String q = question.toLowerCase(Locale.ROOT);

    if (q.contains("explain") || q.contains("class") || q.contains("what is")) {
        String className = extractClassName(question);
        if (className != null) {
            ProjectClass cls = findClass(className);
            if (cls != null) return formatClass(cls);
        }
    }
    if (q.contains("depend") || q.contains("impact")) {
        String className = extractClassName(question);
        if (className != null) {
            ProjectImpact imp = impact(className);
            if (imp != null) return formatImpact(imp);
        }
    }
    return "I couldn't find project information for: " + question;
}
```

**Regression risk:** MEDIUM — changes API contract; all callers affected.

---

## Recommended Fix

**Apply Option A** (full) because:
1. `ProjectSDK` is already analyzed (797 classes, 37 endpoints) and wired as a bean
2. It already has `findClass()`, `impact()`, `findController()` methods ready
3. The chat service Javadoc promises "answers questions grounded in the analyzed project" but never implemented it
4. `KnowledgeSDK` and `ProjectSDK` serve different purposes — chat needs `ProjectSDK`
5. KnowledgeSDK knowledge graph remains available as a secondary source

**Immediate mitigation:** Apply Option B first (1-line change) to stop the placeholder. Then implement Option A for real answers.

---

## Acceptance Tests

```java
@Test
public void chatExplainsWorkspaceController() {
    String sessionId = workspaceService.open("C:\\test\\project");
    ProjectClass cls = projectSdk.findClass("WorkspaceController");
    assertNotNull(cls);

    ChatResponse response = chatService.ask(sessionId,
        "Explain the WorkspaceController class");

    assertNotNull(response.answer());
    assertFalse(response.answer().contains("KNOWLEDGE_QUERY"),
        "Must not contain placeholder: " + response.answer());
    assertTrue(response.answer().length() > 20,
        "Must have real content, got: " + response.answer());
    assertEquals(0.95, response.confidence(), 0.01,
        "Project intelligence should give high confidence");
}

@Test
public void chatExplainsSdk() {
    String sessionId = workspaceService.open("C:\\test\\project");
    ChatResponse response = chatService.ask(sessionId,
        "How does ProjectSDK work?");

    assertNotNull(response.answer());
    assertFalse(response.answer().contains("KNOWLEDGE_QUERY"));
    assertTrue(response.answer().length() > 30);
}

@Test
public void chatShowsImpact() {
    String sessionId = workspaceService.open("C:\\test\\project");
    ChatResponse response = chatService.ask(sessionId,
        "Which classes depend on DefaultRuntimeService?");

    assertNotNull(response.answer());
    assertFalse(response.answer().contains("KNOWLEDGE_QUERY"));
    assertFalse(response.answer().trim().isEmpty());
}

@Test
public void chatGracefulOnUnknownClass() {
    String sessionId = workspaceService.open("C:\\test\\project");
    ChatResponse response = chatService.ask(sessionId,
        "Explain the NonExistentClass");

    assertNotNull(response.answer());
    assertFalse(response.answer().trim().equals("#"));
    assertNotEquals("# KNOWLEDGE_QUERY", response.answer().trim());
}

@Test
public void knowledgeGraphStillWorksAfterOptionA() {
    ai.knowledge().ingest("Architecture", "The app uses a layered architecture");
    SDKResponse search = ai.knowledge().search("architecture");
    assertNotNull(search.answer());
    assertTrue(search.answer().length() > 10);
    assertFalse(search.answer().contains("KNOWLEDGE_QUERY"));
}
```
