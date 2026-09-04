package com.shreeai.os.developer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * <b>Shree Developer Intelligence</b>
 *
 * <p>A desktop/web AI application that understands an entire codebase and
 * helps developers safely build software. Built on Shree AI OS v1.0.</p>
 *
 * <p><b>Phase 2 MVP Modules:</b></p>
 * <ol>
 *   <li>Project Workspace — open, analyze, and persist project sessions</li>
 *   <li>AI Chat — natural language engineering assistant grounded in project code</li>
 *   <li>Developer Workflow — autonomous workflow from instruction to generated files</li>
 *   <li>Safe Apply — diff preview, compile validation, and rollback for patch application</li>
 *   <li>Project Memory — workspace-scoped memory for coding conventions and team preferences</li>
 * </ol>
 *
 * <p><b>Architecture:</b></p>
 * <pre>
 * UI Layer (Next.js /shree-ai-os-web)
 *     ↓
 * Shree Developer Intelligence REST API (this app · port 8081)
 *     ↓
 * Shree AI OS SDK (shree.project(), shree.knowledge(), shree.memory(), ...)
 *     ↓
 * Shree AI OS Kernels (Project Intelligence, Knowledge, Memory, ...)
 * </pre>
 *
 * <p><b>SDK Surface Used:</b></p>
 * <ul>
 *   <li>{@code shree.project().analyze(path)} — project structure analysis</li>
 *   <li>{@code shree.project().build(path, instruction)} — autonomous workflow</li>
 *   <li>{@code shree.project().apply(path, instruction)} — safe patch application</li>
 *   <li>{@code shree.knowledge().query(question)} — grounded code answers</li>
 *   <li>{@code shree.memory().store(...)} — workspace-scoped memory</li>
 *   <li>{@code shree.memory().recall(query)} — memory retrieval</li>
 * </ul>
 *
 * @since Phase 2
 */
@SpringBootApplication
public class DeveloperIntelligenceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DeveloperIntelligenceApplication.class, args);
    }
}
