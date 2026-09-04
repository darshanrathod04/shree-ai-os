# Sprint 17.2 — REAL Runtime Audit: AI Chat Placeholder Bug

**Date:** February 9, 2026  
**Type:** Runtime Evidence Report (not a code review)  
**Repository:** `C:\shree-ai-os`  
**Application:** `shree-developer-intelligence` (port 9090)  
**Status:** 🔴 BUG REPRODUCED — 6/6 AI Chat requests return `# KNOWLEDGE_QUERY` placeholder

---

## Executive Summary

All 6 questions routed through the actual REST endpoint `POST /api/developer/chat/ask` return the **same literal answer**:

```
# KNOWLEDGE_QUERY

---
**Project Memory:**
The request was successfully processed through the Shree AI intelligence pipeline.
```

**Root cause:** The Response Synthesizer's `synthesizeKnowledge(...)` renders the request message as the markdown title. `KnowledgeSDK.query()` sets the SDK request message to the literal string `"KNOWLEDGE_QUERY"`. That literal is forwarded to the runtime as the `userInput`. `KnowledgeStage` then has no `keyword` in metadata (only `question`), falls back to `context.getExecutionRequest().getUserInput()` = `"KNOWLEDGE_QUERY"`, searches an empty knowledge graph, finds nothing, and `synthesizeKnowledge` formats the title as `# KNOWLEDGE_QUERY`. **Project Intelligence is never invoked.**

**Confidence reported is 0.9** — a hard-coded baseline, not a real grounding signal.

---

## Phase 1 — Project Intelligence Verification

| Check | Required | Actual | Status |
|-------|----------|--------|--------|
| `classCount` | 797 | **797** | ✅ |
| `endpointCount` | 37 | **37** | ✅ |
| `framework` | SPRING_BOOT | **SPRING_BOOT** | ✅ |
| `buildSystem` | MAVEN | **MAVEN** | ✅ |
| `analyzed` | true | **true** | ✅ |

**Live HTTP request/response:**

```
POST http://localhost:9090/api/developer/workspace/open
Content-Type: application/json

{"path":"C:\\shree-ai-os"}
```

**Response (HTTP 200, ~2.4s):**
```json
{
  "id": "28e8a2fe-345f-4cdf-8412-eda2fb3dfc8a",
  "projectName": "shree-ai-os",
  "projectPath": "C:\\shree-ai-os",
  "classCount": 797,
  "endpointCount": 37,
  "framework": "SPRING_BOOT",
  "buildSystem": "MAVEN",
  "moduleCount": 2,
  "analyzed": true,
  "openedAt": "2026-09-02T14:58:06.255542400Z"
}
```

**Project Intelligence works. The bug is in chat, not analysis.** Session ID: `28e8a2fe-345f-4cdf-8412-eda2fb3dfc8a`

## Phase 2 — AI Chat Runtime Audit (6 Tests)

All 6 tests used the same request format:
```
POST http://localhost:9090/api/developer/chat/ask
Content-Type: application/json
{"sessionId":"28e8a2fe-345f-4cdf-8412-eda2fb3dfc8a","question":"<Q>"}
```

### Test 1 — "Explain the WorkspaceController class"
- Status: 200 | Time: 545.5 ms (first request, JIT warmup)
- Meta: memoryUsed=true, knowledgeUsed=true, confidence=0.9
- **Answer:** `# KNOWLEDGE_QUERY\n\n---\n**Project Memory:**\nThe request was successfully processed through the Shree AI intelligence pipeline.`
- Verdict: 🔴 **FAIL** — no class info returned

### Test 2 — "What endpoints exist in this project?"
- Status: 200 | Time: 11.5 ms
- **Answer:** `# KNOWLEDGE_QUERY\n\n---\n**Project Memory:**\n...`
- Verdict: 🔴 **FAIL** — no endpoint list

### Test 3 — "How does ProjectSDK work?"
- Status: 200 | Time: 16.9 ms
- **Answer:** `# KNOWLEDGE_QUERY\n\n---\n**Project Memory:**\n...`
- Verdict: 🔴 **FAIL**

### Test 4 — "Which classes depend on DefaultRuntimeService?"
- Status: 200 | Time: 13.5 ms
- **Answer:** `# KNOWLEDGE_QUERY\n\n---\n**Project Memory:**\n...`
- Verdict: 🔴 **FAIL**

### Test 5 — "Show authentication flow"
- Status: 200 | Time: 11.3 ms
- **Answer:** `# KNOWLEDGE_QUERY\n\n---\n**Project Memory:**\n...`
- Verdict: 🔴 **FAIL**

### Test 6 — "Explain the UserService class"
- Status: 200 | Time: 12.1 ms
- **Answer:** `# KNOWLEDGE_QUERY\n\n---\n**Project Memory:**\n...`
- Verdict: 🔴 **FAIL** — same literal placeholder

## Phase 3 — Runtime Execution Trace

```
HTTP Request
    │
    ▼
AiChatController.ask(AskRequest)              [controller @ /ask]
    │
    ▼
AiChatService.ask(sessionId, question)         [AiChatService.java:60]
    │ buildKnowledgeQuery(question) → returns question as-is
    │
    ▼
ShreeAI.knowledge().query(knowledgeQuery)
    │ KnowledgeSDK.query():                     [KnowledgeSDK.java:36-47]
    │   SDKRequest:
    │     .message("KNOWLEDGE_QUERY")           ← LITERAL!
    │     .metadata({operation="QUERY_KNOWLEDGE",
    │                question="Explain the..."})  ← real question in metadata
    │
    ▼
ShreeClient.chat(SDKRequest)                    [ShreeClient.java:68-100]
    │ ExecutionRequest:
    │   userInput = "KNOWLEDGE_QUERY"           ← LITERAL
    │   metadata  = {operation, question}
    │
    ▼
RuntimeService.execute(ExecutionRequest)
    │ PipelineContext:
    │   userInput        = "KNOWLEDGE_QUERY"
    │   requestMetadata  = {operation, question}  ← set as attribute
    │
    ▼
KnowledgeStage.execute()                        [KnowledgeStage.java:130-170]
    │ Reads requestMetadata; no "keyword" field
    │ Falls back to userInput = "KNOWLEDGE_QUERY"
    │ QueryNormalizer.normalize("KNOWLEDGE_QUERY")
    │   → searches knowledge graph for "KNOWLEDGE_QUERY"
    │   → finds nothing (graph is empty)
    │ Metadata set:
    │   routedKernel     = "Knowledge Kernel"   ← set
    │   knowledgeResults = []                   ← empty
    │   knowledgeTitle   = "KNOWLEDGE_QUERY"    ← FALLBACK TITLE
    │
    ▼
DefaultResponseSynthesizer.synthesize()         [line 39-59]
    │ isKnowledgeResult() → TRUE (routedKernel="Knowledge Kernel")
    │ → synthesizeKnowledge()
    │
    ▼
synthesizeKnowledge()                           [line 520-620]
    │ keyword = "" (not in requestMetadata)
    │ title = firstNonBlank(
    │   metadata.get("knowledgeTitle"),          ← null
    │   keyword,                                 ← ""
    │   requestText(context)                     ← "KNOWLEDGE_QUERY"
    │ )
    │ → title = "KNOWLEDGE_QUERY"
    │
    │ summary = null
    │ results = []
    │
    │ answer = "# KNOWLEDGE_QUERY\n\n"          ← THE BUG
    │

## Phase 4 — Root Cause Analysis

| Check | Evidence | Result |
|-------|----------|--------|
| **1. Is Project Intelligence invoked?** | `WorkspaceService` analyzed `C:\shree-ai-os` successfully (797 classes, 37 endpoints). But `AiChatService` does NOT call `ProjectSDK` at all. | ❌ **NO** — ProjectSDK is never used in the chat path |
| **2. Is workspace session reaching chat?** | `sessionId` is in the request and passed to `AiChatService`, but `AiChatService` only uses it for memory keying — not to look up the analyzed project. | ❌ **NO** — session is used for memory only |
| **3. Is sessionId lost?** | `sessionId` arrives at `AiChatController`, goes to `AiChatService.ask()`, then to `shreeAi.knowledge().query()`. But `KnowledgeSDK.query()` does NOT include `sessionId` in the SDK request metadata. | ❌ **YES** — sessionId is lost when calling knowledge |
| **4. Is IntelligenceContext empty?** | `AiChatService` never builds an `IntelligenceContext` with project evidence. The workspace open call populates an in-memory map in `WorkspaceService`, but this is not accessible to `AiChatService`. | ❌ **YES** — no project context in chat requests |
| **5. Is Runtime routing to Knowledge instead of Project?** | `KnowledgeSDK.query()` sends `message="KNOWLEDGE_QUERY"` — a literal placeholder. The runtime's `KnowledgeStage` searches the knowledge *graph* (user-ingested documents) for "KNOWLEDGE_QUERY". It finds nothing. `ProjectSDK` is never called. | ❌ **YES** — routed to wrong kernel |
| **6. Which exact line causes the placeholder?** | `KnowledgeSDK.java:39` — `.message("KNOWLEDGE_QUERY")` sets the literal. `synthesizeKnowledge` at `DefaultResponseSynthesizer.java:536` uses `requestText(context)` as title fallback, which returns the literal. Line 555: `answer.append("# ").append(title)` renders it as `# KNOWLEDGE_QUERY`. | ✅ **YES** — identified |

### The 3-Bug Chain

**Bug 1: AiChatService never calls ProjectSDK**

`AiChatService` was designed (per its Javadoc) to "compose `KnowledgeSDK` and `MemorySDK` to produce responses that reference actual project code." But `KnowledgeSDK` searches a **knowledge graph** (user-ingested documents), NOT the **analyzed project structure** (`ProjectSDK`). The two systems are completely separate.

The chat service never:
- Calls `ProjectSDK.analyze()`
- Calls `ProjectSDK.findClass()`
- Calls `ProjectSDK.impact()`

The `sessionId` is used only for memory namespacing.

**Bug 2: KnowledgeSDK.query() sends a literal placeholder message**

`KnowledgeSDK.query(question)` at line 39 sets the message to the literal string `"KNOWLEDGE_QUERY"`. The actual question is only in metadata as `question`. The runtime's `KnowledgeStage` reads `requestMetadata.get("question")` correctly, but the synthesizer's `synthesizeKnowledge()` falls back to `requestText(context)` (the literal placeholder) for the title.

**Bug 3: Confidence is hard-coded 0.9, not derived from grounding**

Both `knowledge.confidence()` and `memory.confidence()` return hard-coded 0.9. The response shows 0.9 confidence despite the knowledge graph being empty — making the metric meaningless.

### Why Tests Pass But Runtime Fails

The acceptance test `KnowledgeGroundedChatAndQueryTest` has a **Sprint-10A fix** comment explaining the exact same bug: "KNOWLEDGE_QUERY places the question under `question`, so the stage was searching with the placeholder message 'KNOWLEDGE_QUERY' instead of the actual question."

The test passes because it **ingests data first** (`ai.knowledge().ingest(TITLE, CONTENT)`) and then queries. When the knowledge graph has content, the ranking logic may return the ingested document as the top result. But in the production app, the knowledge graph is **empty** at runtime — no documents are ever ingested.

This means the test is a false positive: it passes because of coincidental ranking on a populated graph, not because the path works correctly.

---

    ▼
SDKResponse.answer
    │
    ▼
AiChatService.buildAnswer(knowledge, memory)    [line 120-132]
    sb.append(knowledge.answer())               ← "# KNOWLEDGE_QUERY\n\n"
    sb.append("\n\n---\n**Project Memory:**\n")
    sb.append(memory.answer())
    │
    ▼ HTTP 200
```


## Phase 5 — Fix

Full fix specification written to: **`SPRINT17_2_RUNTIME_FIX.md`**

See that file for complete root cause, all 3 fix options, code patches, regression risk analysis, and acceptance tests.

**Quick reference:**

| Fix | File | Change | Risk |
|-----|------|--------|------|
| **B (Quick)** | `KnowledgeSDK.java:39` | `.message("KNOWLEDGE_QUERY")` → `.message(question)` | MEDIUM |
| **A (Full)** | `AiChatService.java` | Inject `ProjectSDK`, add `tryProjectIntelligence()` | LOW |
| **C (API)** | `ProjectSDK.java` | Add `explain(question)` | MEDIUM |

**Recommended:** Apply Fix B immediately (1 line) to stop the placeholder. Then implement Fix A for real answers.

### Key Findings from Trace

**Finding 1: `requestMetadata` in PipelineContext**

The `KnowledgeStage` reads `context.getAttribute("requestMetadata")` to extract the question. The SDK puts the question in `request.metadata()`. The `requestMetadata` attribute is set in `DefaultRuntimeService` (line 868) and the synthesizer also reads it at line 525-533.

For `QUERY_KNOWLEDGE`, `keyword` is null (only set for `SEARCH_KNOWLEDGE`), so `keyword` is `""`.

**Finding 2: `requestText(context)` fallback**

```java
String title = firstNonBlank(
    string(metadata.get("knowledgeTitle")),  // null (empty results)
    keyword,                                  // "" (not set for QUERY_KNOWLEDGE)
    requestText(context)                      // "KNOWLEDGE_QUERY"  ← THE LITERAL!
);
```

`requestText(context)` reads `context.getExecutionRequest().getUserInput()`, which is the literal `"KNOWLEDGE_QUERY"` — not the real question.

**Finding 3: Why is `confidence` 0.9?**

In `AiChatService.ask()`:
```java
.confidence(Math.min(knowledge.confidence(), memory.confidence()))
```

Both `knowledge.confidence()` and `memory.confidence()` return hard-coded 0.9. The 0.9 is a hardcoded default, not a real grounding signal.

---


### Phase 2 Summary

| # | Question | Time | Verdict |
|---|----------|------|---------|
| 1 | Explain the WorkspaceController class | 545.5 ms | 🔴 FAIL |
| 2 | What endpoints exist in this project? | 11.5 ms | 🔴 FAIL |
| 3 | How does ProjectSDK work? | 16.9 ms | 🔴 FAIL |
| 4 | Which classes depend on DefaultRuntimeService? | 13.5 ms | 🔴 FAIL |
| 5 | Show authentication flow | 11.3 ms | 🔴 FAIL |
| 6 | Explain the UserService class | 12.1 ms | 🔴 FAIL |

**All 6 returned the identical `# KNOWLEDGE_QUERY` placeholder.** Identical text across radically different questions proves the synthesizer is rendering a static literal, not analyzing anything.

---


---
