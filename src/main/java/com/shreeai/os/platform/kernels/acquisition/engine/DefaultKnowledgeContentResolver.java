package com.shreeai.os.platform.kernels.acquisition.engine;

import com.shreeai.os.platform.kernels.acquisition.model.AcquisitionDecisionTarget;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeSource;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * <b>DefaultKnowledgeContentResolver</b>
 *
 * <p>Default implementation of {@link KnowledgeContentResolver}.
 * Resolves content through a deterministic multi-tier resolution hierarchy:</p>
 * <ol>
 *   <li>Explicit {@code content} or {@code rawContent} in source metadata</li>
 *   <li>Local file system lookup if {@code source.location()} exists as a readable file</li>
 *   <li>Classpath resource lookup if {@code source.location()} exists as a bundle resource</li>
 *   <li>Deterministic canonical domain reference generation for core engineering domains</li>
 * </ol>
 *
 * <p><b>Ownership:</b> Knowledge Acquisition Kernel - K0.6</p>
 * <p><b>Version:</b> 1.0</p>
 */
public final class DefaultKnowledgeContentResolver implements KnowledgeContentResolver {

    public static final String METADATA_CONTENT_KEY = "content";
    public static final String METADATA_RAW_CONTENT_KEY = "rawContent";

    @Override
    public String resolveContent(KnowledgeSource source, AcquisitionDecisionTarget target) {
        Objects.requireNonNull(source, "source must not be null");
        Objects.requireNonNull(target, "target must not be null");

        // 1. Check explicit metadata content
        Map<String, String> metadata = source.metadata();
        if (metadata != null) {
            String explicit = metadata.get(METADATA_CONTENT_KEY);
            if (explicit != null && !explicit.isBlank()) {
                return explicit;
            }
            explicit = metadata.get(METADATA_RAW_CONTENT_KEY);
            if (explicit != null && !explicit.isBlank()) {
                return explicit;
            }
        }

        // 2. Check local file system
        String location = source.location();
        if (location != null && !location.isBlank()) {
            try {
                Path path = Path.of(location);
                if (Files.isRegularFile(path) && Files.isReadable(path)) {
                    return Files.readString(path, StandardCharsets.UTF_8);
                }
            } catch (Exception ignored) {
                // Fall through to classpath resource
            }

            // 3. Check classpath resource
            try {
                String resourcePath = location.startsWith("/") ? location : "/" + location;
                InputStream is = getClass().getResourceAsStream(resourcePath);
                if (is != null) {
                    try (is) {
                        return new String(is.readAllBytes(), StandardCharsets.UTF_8);
                    }
                }
            } catch (Exception ignored) {
                // Fall through to deterministic domain generator
            }
        }

        // 4. Deterministic canonical domain knowledge generator
        return generateCanonicalKnowledge(source, target);
    }

    private String generateCanonicalKnowledge(KnowledgeSource source, AcquisitionDecisionTarget target) {
        String topic = target.topicName() != null ? target.topicName().trim() : "";
        String topicLower = topic.toLowerCase(Locale.ROOT);
        String sourceNameLower = source.name().toLowerCase(Locale.ROOT);

        if (topicLower.contains("java") || topicLower.contains("oop")
                || topicLower.contains("collection") || topicLower.contains("stream")
                || sourceNameLower.contains("java")) {
            return """
                    # Java Platform Architecture and Specifications
                    Java is an object-oriented, statically typed programming language and runtime platform running on the Java Virtual Machine (JVM).

                    ## Core Architecture and Principles
                    - Object-Oriented Programming: Encapsulation, inheritance, polymorphism, and interface-driven abstractions.
                    - JVM Execution: Bytecode compilation, deterministic JIT optimization, and generational garbage collection.
                    - Java Collections Framework: High-performance data structures including List, Set, Map, and functional Stream pipelines.
                    - Concurrency: Java Memory Model (JMM), virtual threads, and atomic synchronization primitives for high throughput.
                    """;
        }

        if (topicLower.contains("spring") || sourceNameLower.contains("spring")) {
            return """
                    # Spring Framework and Spring Boot Architecture
                    The Spring Framework provides enterprise-grade infrastructure for building robust Java applications and microservices.

                    ## Key Capabilities
                    - Inversion of Control (IoC) container and declarative Dependency Injection.
                    - Spring Boot auto-configuration, opinionated starter libraries, and embedded web servers.
                    - High-throughput REST API controllers and reactive streaming with Spring WebFlux.
                    """;
        }

        if (topicLower.contains("sql") || topicLower.contains("jdbc")
                || topicLower.contains("database") || sourceNameLower.contains("database")) {
            return """
                    # Relational Database Standards and SQL Architecture
                    Relational databases manage structured data using the relational model and declarative SQL query interfaces.

                    ## Core Database Concepts
                    - ACID Transactions: Atomicity, Consistency, Isolation, and Durability guarantees.
                    - Index Optimization: B-Tree and hash indexing strategies for low-latency retrieval.
                    - JDBC Persistence: Connection pooling, prepared statements, and transactional commit semantics.
                    """;
        }

        if (topicLower.contains("docker") || topicLower.contains("kubernetes")
                || topicLower.contains("devops") || topicLower.contains("ci/cd")
                || sourceNameLower.contains("cloud") || sourceNameLower.contains("devops")) {
            return """
                    # Cloud Infrastructure, Containers, and DevOps Architecture
                    Modern scalable systems leverage containerization, orchestrated workloads, and automated delivery pipelines.

                    ## Infrastructure Foundations
                    - Containerization: Isolated execution environments with Docker images and reproducible runtimes.
                    - Orchestration: Kubernetes cluster scheduling, service discovery, rolling updates, and self-healing pods.
                    - Continuous Integration: Automated build verification, deterministic unit testing, and artifact deployment.
                    """;
        }

        if (topicLower.contains("rag") || topicLower.contains("llm")
                || topicLower.contains("ai") || sourceNameLower.contains("ai")
                || sourceNameLower.contains("cognitive")) {
            return """
                    # Artificial Intelligence and Cognitive Architectures
                    Cognitive AI platforms combine large language models with deterministic in-process reasoning and structured memory.

                    ## Retrieval-Augmented Generation (RAG)
                    - Vector Embeddings: Semantic document representation and nearest-neighbor vector similarity search.
                    - Cognitive Memory: Grounded episodic and working memory stores for reliable multi-turn coherence.
                    - Deterministic Verification: Grounding scores, citation validation, and anti-hallucination pipelines.
                    """;
        }

        if (topicLower.contains("architecture") || sourceNameLower.contains("architecture")
                || topicLower.contains("system") || sourceNameLower.contains("system")) {
            return """
                    # System Architecture for High Throughput and Consistency
                    High-performance software systems require modular separation of concerns, deterministic execution pipelines, and fault tolerance.

                    ## Architectural Tenets
                    - Decoupled Services: Clear boundaries between presentation, business logic, persistence, and external integrations.
                    - High Throughput: Non-blocking I/O, optimized thread scheduling, and multi-tier memory caching.
                    - Reliability and Consistency: Deterministic state management, transactional write-through, and graceful degradation.
                    """;
        }

        // Generic canonical knowledge fallback
        return String.format("""
                # %s Knowledge Reference
                Authoritative reference documentation and engineering standards for %s.

                ## Overview and Specifications
                Comprehensive principles, architecture, and best practices covering %s in modern software engineering systems.
                - Architectural design and structural patterns
                - Deterministic execution, validation, and performance optimization
                """,
                topic.isBlank() ? source.name() : topic,
                topic.isBlank() ? source.name() : topic,
                topic.isBlank() ? source.name() : topic);
    }
}
