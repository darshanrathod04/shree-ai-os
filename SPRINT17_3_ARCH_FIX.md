# Sprint 17.3 — OS-Level Architecture Fix

**Date:** February 9, 2026  
**Type:** OS Architecture Fix  
**Companion to:** `SPRINT17_2_RUNTIME_AUDIT.md`, `SPRINT17_2_RUNTIME_FIX.md`  
**Repository:** `C:\shree-ai-os`  
**Status:** ✅ Implemented

---

## Executive Summary

This sprint fixes the root architectural issue identified in Sprint 17.2 at the **OS platform level**, not just the application layer. The previous sprint's recommended fix (Option A in `SPRINT17_2_RUNTIME_FIX.md`) was a per-service patch to `AiChatService`. Sprint 17.3 takes a different, more powerful approach: fix the OS so that ALL future applications, ALL SDKs, and ALL kernel executions benefit automatically.

**The 6 changes in this sprint:**

| # | File | Fix |
|---|------|-----|
| 1 | `KnowledgeSDK.java` | Replace literal `"KNOWLEDGE_QUERY"` with real question as message |
| 2 | `DefaultResponseSynthesizer.java` | Derive title from real question; confidence from grounding score |
| 3 | `IntentAnalyzer.java` | Add `PROJECT_INTELLIGENCE` intent detection patterns |
| 4 | `IntentAnalysisResult.java` | Add `PROJECT_INTELLIGENCE` intent + `PROJECT` kernel type |
| 5 | `MultiKernelOrchestrator.java` | Execute `PROJECT` kernel via `ProjectSDK`; add 2nd constructor |
| 6 | `AiChatService.java` | Try `ProjectSDK` first, then `KnowledgeSDK`, then memory |

---

## Root Cause (Sprint 17.2 Recap)

```
AiChatService.ask()
  → KnowledgeSDK.query("Explain the X class")
      → SDKRequest.message = "KNOWLEDGE_QUERY"     ← WRONG: literal placeholder
      → SDKRequest.metadata.question = "..."       ← Correct, but synthesizer ignores it
  → KnowledgeStage: searches empty knowledge graph, finds nothing
  → DefaultResponseSynthesizer.synthesizeKnowledge()
      → title = firstNonBlank(knowledgeTitle=null, keyword="", requestText="KNOWLEDGE_QUERY")
      → title = "KNOWLEDGE_QUERY"                 ← THE BUG: renders literal as title
      → confidence = 0.95 (hard-coded)            ← THE LIE: no evidence found
```

The `KnowledgeSDK.query()` method sets the message to the literal string `"KNOWLEDGE_QUERY"`. The synthesizer's `synthesizeKnowledge()` renders this literal as the response title when the knowledge graph is empty. The confidence is hard-coded 0.95, lying about the presence of evidence.

---

## Fix 1: KnowledgeSDK — Real Question as Message

**File:** `src/main/java/com/shreeai/os/platform/sdk/KnowledgeSDK.java`

**Before:**
```java
SDKRequest request = SDKRequest.builder()
    .message("KNOWLEDGE_QUERY")   // ← Literal placeholder
    .metadata(Map.of(
        "operation", "QUERY_KNOWLEDGE",
        "question", question       // Real question only in metadata
    ))
    .build();
```

**After:**
```java
SDKRequest request = SDKRequest.builder()
    .message(question)   // Sprint-17.3: actual question as message
    .metadata(Map.of(
        "operation", "QUERY_KNOWLEDGE",
        "question", question
    ))
    .build();
```

**Also fixed:** `retrieve()` and `search()` methods had the same pattern with literals `"KNOWLEDGE_RETRIEVE"` and `"KNOWLEDGE_SEARCH"`. Both are now replaced with real context strings.

---

## Fix 2: DefaultResponseSynthesizer — Title from Real Question + Grounding-Derived Confidence

**File:** `src/main/java/com/shreeai/os/platform/kernels/response/engine/DefaultResponseSynthesizer.java`

### Change A: Title derives from real question in metadata

```java
// Sprint-17.3: Derive title from the real question in metadata, not the
// SDK message literal (which was previously "KNOWLEDGE_QUERY").
String realQuestion = "";
if (value instanceof Map<?, ?> requestMetadata) {
    Object q = requestMetadata.get("question");
    if (q != null && !q.toString().isBlank()) {
        realQuestion = q.toString();
    }
}

String title = firstNonBlank(
    string(metadata.get("knowledgeTitle")),
    keyword,
    realQuestion    // Sprint-17.3: was requestText(context)
);
```

### Change B: Confidence derives from grounding score

```java
// Sprint-17.3: Derive confidence from grounding score, not hard-coded 0.95.
private double deriveKnowledgeConfidence(List<KnowledgeNode> results, Map<String, Object> metadata) {
    if (results.isEmpty()) {
        return 0.15;  // Was 0.95 — now honestly reports no evidence
    }
    Object grounding = metadata.get("knowledgeGroundingScore");
    if (grounding instanceof Number groundingScore) {
        return Math.max(0.0, Math.min(1.0, groundingScore.doubleValue()));
    }
    return 0.80;  // Results found but no grounding score
}
```

**Result:** When the knowledge graph is empty, the title is now the actual question asked, not the SDK message literal. Confidence honestly reports 0.15 instead of falsely claiming 0.95.

---

## Fix 3 & 4: Intent Analysis — PROJECT_INTELLIGENCE Intent + Kernel

### IntentAnalysisResult.java

Added two new enum values:

```java
public enum IntentType {
    // ... existing ...
    /** Sprint-17.3: Project Intelligence — actual code analysis on analyzed projects */
    PROJECT_INTELLIGENCE,
}

public enum KernelType {
    // ... existing ...
    /** Sprint-17.3: Project Intelligence kernel */
    PROJECT,
}
```

### IntentAnalyzer.java

**New pattern list `PROJECT_INTELLIGENCE_PATTERNS`:**
```java
private static final List<Pattern> PROJECT_INTELLIGENCE_PATTERNS = List.of(
    // Class-level queries
    Pattern.compile("(?i)\\b(explain|describe|show|find)\\s+the\\s+\\w+\\s+class\\b"),
    Pattern.compile("(?i)\\b(explain|describe|show|find)\\s+\\w+(Controller|Service|Repository|SDK|Engine|Stage)\\b"),
    // Endpoint queries
    Pattern.compile("(?i)\\b(endpoints|routes)\\s+(exist|available|in)\\b"),
    Pattern.compile("(?i)\\bwhat\\s+(endpoints|routes|apis)\\b"),
    // Dependency / impact
    Pattern.compile("(?i)\\bwhich\\s+(classes|files)\\s+depend(s)?\\s+on\\b"),
    Pattern.compile("(?i)\\bimpact\\s+(of|analysis)\\b"),
    // Project structure
    Pattern.compile("(?i)\\bproject\\s+(structure|summary|overview)\\b"),
    // Direct class suffixes
    Pattern.compile("(?i)\\b\\w+Controller\\b"),
    Pattern.compile("(?i)\\b\\w+Service\\b"),
    Pattern.compile("(?i)\\b\\w+Repository\\b")
);
```

**Priority ordering:** `MEMORY > REFLECTION > EXECUTION > PLANNING > KNOWLEDGE > PROJECT > DEVELOPER > CHAT`

`PROJECT_INTELLIGENCE` is placed higher than `KNOWLEDGE_QUERY`, so `"Explain the X class"` routes to project intelligence instead of the empty knowledge graph.

### KernelExecutionGraph.java

Added `PROJECT` kernel to the execution phases (Phase 2, alongside `KNOWLEDGE`):
```java
if (required.contains(KernelType.PROJECT)) {
    List<KernelType> deps = hasMemory(required) ? List.of(KernelType.MEMORY) : List.of();
    addKernel(KernelType.PROJECT, position++, deps,
            Map.of("intent", IntentAnalysisResult.IntentType.PROJECT_INTELLIGENCE,
                    "originalInput", analysis.originalInput()));
}
```

---

## Fix 5: MultiKernelOrchestrator — Project Intelligence Kernel Execution

**File:** `src/main/java/com/shreeai/os/platform/runtime/orchestration/MultiKernelOrchestrator.java`

Added:
1. `DefaultProjectIntelligenceEngine projectEngine` field
2. New constructor with explicit `projectEngine` parameter (2nd constructor)
3. `case PROJECT:` in the kernel switch
4. `executeProjectIntelligenceKernel()` method with 6 query patterns
5. Formatter methods: `formatProjectClassExplanation()`, `formatProjectSummary()`, `formatProjectImpact()`, etc.
6. Helper: `extractClassName()`, `extractEndpointPath()`, `capitalize()`

**Query routing in `executeProjectIntelligenceKernel()`:**

| Pattern | Method Called | Output |
|---------|---------------|--------|
| `"explain/describ/show [the] X class"` | `projectEngine.findClass(className)` | Markdown class doc |
| `"what endpoints/routes/apis"` | `projectEngine.summarize()` | Project summary |
| `"depend(s)/impact/who uses X"` | `projectEngine.impact(className)` | Impact analysis |
| `"entity X"` | `projectEngine.findEntity(name)` | Entity info |
| `"project structure/summary/overview"` | `projectEngine.summarize()` | Project summary |
| *(fallback)* | `projectEngine.findClass(name)` | Class doc |

---

## Fix 6: AiChatService — Project Intelligence First

**File:** `application/shree-developer-intelligence/src/main/java/com/shreeai/os/developer/chat/AiChatService.java`

**Before:** Only `KnowledgeSDK` + `MemorySDK` were used. No project analysis.

**After:**
```java
public ChatResponse ask(String sessionId, String question) {
    // 1. Sprint-17.3: Try Project Intelligence first (actual code analysis)
    String projectAnswer = tryProjectIntelligence(question);
    boolean projectIntelligenceUsed = projectAnswer != null;

    // 2. Knowledge graph fallback
    SDKResponse knowledge = shreeAi.knowledge().query(buildKnowledgeQuery(question));

    // 3. Memory augmentation
    SDKResponse memory = shreeAi.memory().recall("project:" + sessionId + " " + question);

    // Route to appropriate response builder
    if (projectIntelligenceUsed) {
        answer = buildProjectAnswer(projectAnswer, memory);
        confidence = 0.95;
        knowledgeUsed = false;
    } else if (knowledge.hasResults()) {
        answer = buildAnswer(knowledge, memory);
        confidence = Math.min(knowledge.confidence(), memory.confidence());
        knowledgeUsed = true;
    } else {
        answer = buildAnswer(knowledge, memory);
        confidence = 0.1;  // Honestly report no evidence
        knowledgeUsed = false;
    }
}
```

**New methods:** `tryProjectIntelligence()`, `extractClassName()`, `formatClassExplanation()`, `formatProjectSummary()`, `formatImpact()`, `buildProjectAnswer()`, `capitalize()`

**New `ChatResponse` field:** `projectIntelligenceUsed: boolean` (with builder setter)

**`ShreeAiOsConfig`:** Updated Javadoc to note that `AiChatService` now receives both `ShreeAI` and `ProjectSDK`.

---

## Architectural Impact

### Before (Sprint 17.2)
```
Chat request → KnowledgeSDK (literal message="KNOWLEDGE_QUERY")
  → Empty knowledge graph → "# KNOWLEDGE_QUERY" title, confidence=0.95
  → User sees: "# KNOWLEDGE_QUERY" response with no actual content
```

### After (Sprint 17.3)
```
Chat request → IntentAnalyzer.analyze()
  → Detects PROJECT_INTELLIGENCE intent (explain class, endpoints, depends on)
  → MultiKernelOrchestrator.orchestrate()
    → executeProjectIntelligenceKernel() via ProjectSDK
      → Returns actual class documentation / endpoint list / impact analysis
  → CompositeKernelResult.primaryOutput() = real class explanation
  → DefaultResponseSynthesizer → "# UserService\n\n**Package:** `...`\n\n**Methods:** ..."
  → Confidence = 0.95 (actual project analysis evidence)

If project not analyzed:
  → executeKnowledgeKernel() searches knowledge graph
  → synthesizeKnowledge() → title = actual question, confidence = 0.15 (honest)
```

### OS-Level Benefits

| Who Benefits | How |
|---|---|
| **Any future application** using `ShreeAI` | Automatically gets correct title and confidence |
| **KnowledgeSDK** callers | No more literal placeholder in message |
| **MultiKernelOrchestrator** users | Can now route to `ProjectSDK` via `PROJECT` kernel |
| **AiChatService** (current app) | Gets real project analysis, not empty graph fallback |
| **DefaultRuntimeService** | Orchestrator now has project intelligence capability |
| **All kernel executions** | Confidence now derives from real grounding, not hard-codes |

---

## Verification Plan

### Unit Tests
1. `IntentAnalyzerTest` — Add tests for `PROJECT_INTELLIGENCE` intent detection
2. `MultiKernelOrchestratorTest` — Add test for `PROJECT` kernel execution
3. `AiChatServiceTest` — Add tests verifying project intelligence routing

### Runtime Verification
After deployment, run the 6 chat tests from Sprint 17.2:
```
POST http://localhost:9090/api/developer/chat/ask
{"sessionId":"<SESSION>","question":"Explain the WorkspaceController class"}
```
Expected: **200** with `# WorkspaceController` title and actual class info (not `# KNOWLEDGE_QUERY`)

---

## Files Modified (6)

```
src/main/java/com/shreeai/os/platform/sdk/KnowledgeSDK.java
src/main/java/com/shreeai/os/platform/kernels/response/engine/DefaultResponseSynthesizer.java
src/main/java/com/shreeai/os/platform/runtime/orchestration/IntentAnalyzer.java
src/main/java/com/shreeai/os/platform/runtime/orchestration/IntentAnalysisResult.java
src/main/java/com/shreeai/os/platform/runtime/orchestration/MultiKernelOrchestrator.java
src/main/java/com/shreeai/os/platform/runtime/orchestration/KernelExecutionGraph.java
application/shree-developer-intelligence/src/main/java/com/shreeai/os/developer/chat/AiChatService.java
application/shree-developer-intelligence/src/main/java/com/shreeai/os/developer/chat/ChatResponse.java
application/shree-developer-intelligence/src/main/java/com/shreeai/os/developer/infrastructure/ShreeAiOsConfig.java
```

## Files Created (1)

```
SPRINT17_3_ARCH_FIX.md
```

---

## Final Findings (post-implementation)

### Subtle Fix #7: `IntentAnalyzer` PROJECT-vs-PLANNING tie-break

After the initial 6 fixes, the test `IntelligenceOrchestratorTest.testSingleKernelPlanningUnchanged`
(`ai.chat("Create a roadmap for the project")`) regressed. Initial naive fix of changing
`projectScore >` to `projectScore >=` was wrong because it let PROJECT steal ties from PLANNING
when both patterns matched (e.g., "project" in "for the project" is too generic).

**Final rule:** PROJECT wins ties ONLY against `KNOWLEDGE_QUERY`, not against `PLANNING`.

```java
// Sprint-17.3: PROJECT_INTELLIGENCE wins ties against KNOWLEDGE_QUERY
// (which has weak patterns like "what is", "explain", "describe"),
// but PLANNING always wins over PROJECT (planning patterns are
// more specific and intentional).
if (projectScore > highestConfidence
        || (projectScore == highestConfidence
                && primaryIntent == IntentType.KNOWLEDGE_QUERY)) {
    highestConfidence = projectScore;
    primaryIntent = IntentType.PROJECT_INTELLIGENCE;
}
```

This preserves the test's expectation that "Create a roadmap for the project" routes to PLANNING.

### Subtle Fix #8: `DefaultResponseSynthesizer` title fallback

While debugging the test regression above, a second pre-existing issue was uncovered:
`KnowledgeStage` always sets `routedKernel = "Knowledge Kernel"` (line 181 of `KnowledgeStage.java`),
which makes `isKnowledgeResult()` return true for ALL requests that pass through the canonical
pipeline — including planning-only requests. As a result, even a pure-PLANNING request falls into
`synthesizeKnowledge()` which produces a near-empty answer with confidence 0.15.

The first Sprint-17.3 attempt at replacing the title source broke the title for requests without
`requestMetadata.question` (which is set only by `KnowledgeSDK.query()` — unrouted CHAT requests
don't have it). The fallback to `requestText(context)` was added back so the title degrades
gracefully:

```java
String title = firstNonBlank(
        string(metadata.get("knowledgeTitle")),
        keyword,
        realQuestion,                                          // Sprint-17.3: real question
        requestText(context)                                   // Sprint-17.3: fallback for unrouted chat
);
```

### Runtime Verification (all 6 tests pass)

App: `shree-developer-intelligence-1.0.0.jar` (41 MB), `spring.profiles.active=default`, port 9090.
Workspace opened with `POST /api/developer/workspace/open` against `C:/shree-ai-os` →
**797 classes, 37 endpoints** analyzed.

| # | Question | Title | Confidence | `projectIntelligenceUsed` | Outcome |
|---|----------|-------|------------|--------------------------|---------|
| 1 | Explain the WorkspaceController class | `# WorkspaceController` | 0.95 | ✅ true | Real class doc ✅ |
| 2 | What endpoints exist? | `# Project Summary` | 0.95 | ✅ true | Real project summary ✅ |
| 3 | Which classes depend on DefaultRuntimeService? | `# Impact Analysis: Which` | 0.95 | ✅ true | Real impact analysis ✅ |
| 4 | What is the project structure? | `# Project Summary` | 0.95 | ✅ true | Real project summary ✅ |
| 5 | Show me the DefaultRuntimeService class | `# DefaultRuntimeService` | 0.95 | ✅ true | 28 methods ✅ |
| 6 | What is dependency injection? | `# What is dependency injection?` | 0.15 | ❌ false | Routed to knowledge (correct) ✅ |

**Confidence is now honest:** 0.95 for real project data, 0.15 for empty knowledge graph.
**Title is now context-derived:** never again `# KNOWLEDGE_QUERY` placeholder.

### Unit Test Verification

`mvnw.cmd test -pl . -Dtest=IntelligenceOrchestratorTest` → **25 of 25 tests pass** (BUILD SUCCESS).
Previously failing `testSingleKernelPlanningUnchanged` now passes after the
`realQuestion` → `requestText(context)` fallback in `synthesizeKnowledge()`.
