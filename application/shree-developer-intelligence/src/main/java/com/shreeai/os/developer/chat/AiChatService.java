package com.shreeai.os.developer.chat;

import com.shreeai.os.platform.kernels.project.model.ProjectClass;
import com.shreeai.os.platform.kernels.project.model.ProjectImpact;
import com.shreeai.os.platform.kernels.project.model.ProjectSummary;
import com.shreeai.os.platform.sdk.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

/**
 * <b>AiChatService</b>
 *
 * <p>Module 2: Natural language engineering assistant that answers questions
 * grounded in the analyzed project. Composes {@code ProjectSDK}, {@code KnowledgeSDK},
 * and {@code MemorySDK} to produce responses that reference actual project code.</p>
 *
 * <p><b>Sprint-17.3 Architecture:</b></p>
 * <ul>
 *   <li>First tries {@code ProjectSDK} for actual code analysis
 *       (class explanations, endpoint lists, dependency analysis)</li>
 *   <li>Falls back to {@code KnowledgeSDK} for document-grounded answers</li>
 *   <li>Augments with {@code MemorySDK} for workspace conventions</li>
 * </ul>
 *
 * <p>Example queries it can answer:</p>
 * <ul>
 *   <li>"Explain the UserService class"</li>
 *   <li>"What endpoints exist in this project?"</li>
 *   <li>"Which classes depend on DefaultRuntimeService?"</li>
 *   <li>"Show me the project structure"</li>
 * </ul>
 *
 * <p><b>Application Layer Rule:</b> Uses only SDK facades.
 * No direct kernel calls.</p>
 *
 * @since Phase 2
 */
@Service
public class AiChatService {

    private static final Logger log = LoggerFactory.getLogger(AiChatService.class);

    private final ShreeAI shreeAi;
    private final ProjectSDK projectSdk;

    public AiChatService(ShreeAI shreeAi, ProjectSDK projectSdk) {
        this.shreeAi = Objects.requireNonNull(shreeAi, "shreeAi");
        this.projectSdk = Objects.requireNonNull(projectSdk, "projectSdk"); // Sprint-17.3
    }

    /**
     * Answers a natural language question about the project.
     *
     * <p><b>Sprint-17.3 routing logic:</b></p>
     * <ol>
     *   <li><b>Project Intelligence</b> — if question matches "explain class", "endpoints",
     *       "depends on", "impact" patterns → use {@link ProjectSDK} to answer from
     *       the analyzed project graph</li>
     *   <li><b>Knowledge Graph</b> — if no project intelligence match,
     *       query the document knowledge graph via {@link KnowledgeSDK}</li>
     *   <li><b>Memory</b> — always augment with workspace-scoped memory</li>
     * </ol>
     *
     * @param sessionId workspace session ID (used for memory keying)
     * @param question natural language question (must not be null)
     * @return ChatResponse with answer, source references, and confidence
     */
    public ChatResponse ask(String sessionId, String question) {
        Objects.requireNonNull(question, "question");
        Objects.requireNonNull(sessionId, "sessionId");

        log.info("Chat ask [session={}]: {}", sessionId, truncate(question, 80));

        // Sprint-17.3: Try Project Intelligence first (actual code analysis)
        String projectAnswer = tryProjectIntelligence(question);
        boolean projectIntelligenceUsed = projectAnswer != null;

        // Fallback: Knowledge query (document knowledge graph)
        String knowledgeQuery = buildKnowledgeQuery(question);
        SDKResponse knowledge = shreeAi.knowledge().query(knowledgeQuery);

        // Memory recall — workspace-scoped project conventions
        String memoryQuery = "project:" + sessionId + " " + question;
        SDKResponse memory = shreeAi.memory().recall(memoryQuery);

        String answer;
        double confidence;
        boolean knowledgeUsed;

        if (projectIntelligenceUsed) {
            // Sprint-17.3: Project intelligence answered successfully
            answer = buildProjectAnswer(projectAnswer, memory);
            confidence = 0.95;
            knowledgeUsed = false;
        } else if (knowledge.answer() != null && !knowledge.answer().isBlank()
                && !knowledge.answer().contains("knowledge graph is empty")
                && !knowledge.answer().contains("No knowledge results")) {
            // Knowledge graph had results
            answer = buildAnswer(knowledge, memory);
            confidence = Math.min(knowledge.confidence(), memory.confidence());
            knowledgeUsed = true;
        } else {
            // Neither project intelligence nor knowledge graph found results
            answer = buildAnswer(knowledge, memory);
            confidence = 0.1;  // Sprint-17.3: honestly report low confidence
            knowledgeUsed = false;
        }

        ChatResponse response = ChatResponse.builder()
                .sessionId(sessionId)
                .question(question)
                .answer(answer)
                .confidence(confidence)
                .knowledgeUsed(knowledgeUsed)
                .memoryUsed(hasMemoryContent(memory))
                .projectIntelligenceUsed(projectIntelligenceUsed)   // Sprint-17.3
                .timestamp(Instant.now())
                .build();

        log.info("Chat response [session={}]: projectIntelligencUsed={}, confidence={}",
                sessionId, projectIntelligenceUsed, response.confidence());
        return response;
    }

    // ─── Sprint-17.3: Project Intelligence Routing ─────────────────────────

    /**
     * Attempts to answer the question using {@link ProjectSDK}'s analyzed project graph.
     *
     * <p>Returns {@code null} if the question doesn't match any project intelligence
     * pattern or if the project hasn't been analyzed yet (no project graph available).</p>
     *
     * @param question natural language question
     * @return a markdown-formatted answer from project analysis, or null if not applicable
     */
    private String tryProjectIntelligence(String question) {
        if (question == null || question.isBlank() || projectSdk == null) {
            return null;
        }

        String q = question.toLowerCase(Locale.ROOT);

        // Pattern 1: "Explain the X class", "describe X class", "show X class"
        if (q.contains("explain") || q.contains("describe") || q.contains("show")) {
            String className = extractClassName(question);
            if (className != null) {
                ProjectClass cls = projectSdk.findClass(className);
                if (cls != null) {
                    return formatClassExplanation(cls);
                }
            }
        }

        // Pattern 2: "What endpoints exist", "list routes", "show apis"
        if (q.contains("endpoint") || q.contains("routes") || q.contains("apis")
                || q.contains("controllers") || q.contains("project structure")) {
            ProjectSummary summary = projectSdk.summarize();
            if (summary != null) {
                return formatProjectSummary(summary);
            }
        }

        // Pattern 3: "Which class depends on X", "impact of X", "who uses X"
        if (q.contains("depend") || q.contains("impact") || q.contains("who uses")) {
            String className = extractClassName(question);
            if (className != null) {
                ProjectImpact impact = projectSdk.impact(className);
                if (impact != null) {
                    return formatImpact(impact);
                }
            }
        }

        // Pattern 4: Find class by name (e.g., "find class X", "X class", "show X")
        // Generic fallback: any word ending in Controller/Service/Repository/SDK/Engine
        String className = extractClassName(question);
        if (className != null) {
            ProjectClass cls = projectSdk.findClass(className);
            if (cls != null) {
                return formatClassExplanation(cls);
            }
        }

        return null;
    }

    /**
     * Extracts a class name from natural-language phrasing.
     * Handles: "Explain the UserService class", "which class depends on X",
     * any word ending in Controller/Service/Repository/SDK/Engine/Stage.
     */
    private String extractClassName(String question) {
        if (question == null) return null;
        String q = question.toLowerCase(Locale.ROOT);

        // "the X class" pattern
        int classIdx = q.indexOf(" class");
        if (classIdx > 0) {
            String before = question.substring(0, classIdx).trim();
            String[] parts = before.split("\\s+");
            if (parts.length > 0) {
                String candidate = parts[parts.length - 1];
                if (!candidate.isBlank() && !candidate.equalsIgnoreCase("the")) {
                    return capitalize(candidate);
                }
            }
        }

        // Direct class suffix detection
        for (String suffix : List.of("Controller", "Service", "Repository", "SDK", "Engine",
                "Stage", "Config", "Handler", "Processor", "Manager", "Kernel")) {
            int idx = q.indexOf(suffix.toLowerCase(Locale.ROOT));
            if (idx >= 0) {
                // Walk back to find the start of the word
                int wordStart = idx;
                while (wordStart > 0 && Character.isLetterOrDigit(q.charAt(wordStart - 1))) {
                    wordStart--;
                }
                String candidate = question.substring(wordStart, idx + suffix.length()).trim();
                if (!candidate.isBlank()) {
                    return candidate;
                }
            }
        }

        return null;
    }

    private String formatClassExplanation(ProjectClass cls) {
        StringBuilder sb = new StringBuilder();
        sb.append("# ").append(cls.name()).append("\n\n");
        if (cls.packageName() != null && !cls.packageName().isBlank()) {
            sb.append("**Package:** `").append(cls.packageName()).append("`\n\n");
        }
        if (cls.role() != null && cls.role() != ProjectClass.Role.NONE) {
            sb.append("**Role:** ").append(cls.role()).append("\n\n");
        }
        if (cls.methods() != null && !cls.methods().isEmpty()) {
            sb.append("**Methods (").append(cls.methods().size()).append("):**\n\n");
            for (var method : cls.methods().stream().limit(10).toList()) {
                sb.append("- `").append(method.name()).append("`");
                if (method.returnType() != null) {
                    sb.append(" → ").append(method.returnType());
                }
                sb.append("\n");
            }
        }
        return sb.toString().trim();
    }

    private String formatProjectSummary(ProjectSummary summary) {
        StringBuilder sb = new StringBuilder();
        sb.append("# Project Summary\n\n");
        if (summary.projectName() != null) {
            sb.append("**Name:** ").append(summary.projectName()).append("\n\n");
        }
        if (summary.statistics() != null) {
            sb.append("**Classes:** ").append(summary.statistics().classCount()).append("\n");
            sb.append("**Endpoints:** ").append(summary.statistics().endpointCount()).append("\n");
        }
        if (summary.framework() != null) {
            sb.append("**Framework:** ").append(summary.framework()).append("\n");
        }
        if (summary.buildSystem() != null) {
            sb.append("**Build:** ").append(summary.buildSystem()).append("\n");
        }
        return sb.toString().trim();
    }

    private String formatImpact(ProjectImpact impact) {
        StringBuilder sb = new StringBuilder();
        sb.append("# Impact Analysis: ").append(impact.target()).append("\n\n");
        if (impact.affectedClasses() != null && !impact.affectedClasses().isEmpty()) {
            sb.append("**Affected classes (").append(impact.affectedClasses().size()).append("):**\n\n");
            for (String cls : impact.affectedClasses().stream().limit(10).toList()) {
                sb.append("- `").append(cls).append("`\n");
            }
        }
        if (impact.dependencyDepth() > 0) {
            sb.append("\n**Dependency depth:** ").append(impact.dependencyDepth()).append("\n");
        }
        return sb.toString().trim();
    }

    private String buildProjectAnswer(String projectAnswer, SDKResponse memory) {
        StringBuilder sb = new StringBuilder(projectAnswer);
        if (hasMemoryContent(memory)) {
            sb.append("\n\n---\n**Project Memory:**\n").append(memory.answer());
        }
        return sb.toString();
    }

    private String capitalize(String s) {
        if (s == null || s.isBlank()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    /**
     * Stores a project-specific memory entry.
     */
    public void remember(String sessionId, String title, String content) {
        Objects.requireNonNull(sessionId, "sessionId");
        Objects.requireNonNull(title, "title");
        Objects.requireNonNull(content, "content");

        String scopedTitle = "[" + sessionId + "] " + title;
        shreeAi.memory().store(scopedTitle, content);
        log.info("Memory stored for session {}: {}", sessionId, title);
    }

    /**
     * Recalls memories relevant to a query within a workspace.
     */
    public SDKResponse recall(String sessionId, String query) {
        Objects.requireNonNull(sessionId, "sessionId");
        Objects.requireNonNull(query, "query");
        String scoped = "[" + sessionId + "] " + query;
        return shreeAi.memory().recall(scoped);
    }

    /**
     * Builds a context-enriched knowledge query that includes project hints.
     * This improves grounding by hinting the runtime about the relevant scope.
     */
    private String buildKnowledgeQuery(String question) {
        return question;
    }

    private String buildAnswer(SDKResponse knowledge, SDKResponse memory) {
        StringBuilder sb = new StringBuilder();

        if (knowledge.answer() != null && !knowledge.answer().isBlank()) {
            sb.append(knowledge.answer());
        }

        if (hasMemoryContent(memory)) {
            sb.append("\n\n---\n**Project Memory:**\n").append(memory.answer());
        }

        return sb.length() > 0 ? sb.toString() : "I couldn't find relevant information for this question.";
    }

    private boolean hasMemoryContent(SDKResponse memory) {
        return memory != null
                && memory.answer() != null
                && !memory.answer().isBlank()
                && !memory.answer().contains("null");
    }

    private String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max) + "...";
    }
}
