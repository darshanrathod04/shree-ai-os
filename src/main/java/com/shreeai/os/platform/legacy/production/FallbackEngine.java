package com.shreeai.os.platform.legacy.production;

import com.shreeai.os.platform.legacy.context.ConversationContext;
import com.shreeai.os.platform.legacy.skills.ChatSkill;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * FallbackEngine provides graceful fallback when a requested mode
 * is unavailable. Instead of "No active course", it redirects to
 * ChatSkill for general explanations.
 *
 * Logs: [FALLBACK]
 */
@Component
public class FallbackEngine {

    private static final Logger log = LoggerFactory.getLogger(FallbackEngine.class);

    private final ChatSkill chatSkill;

    public FallbackEngine(ChatSkill chatSkill) {
        this.chatSkill = chatSkill;
        log.info("[FALLBACK] FallbackEngine initialized");
    }

    /**
     * Handle fallback when teaching/learning is unavailable.
     * Redirects to ChatSkill so the user gets a natural response.
     *
     * @param input     the original user input
     * @param context   the conversation context
     * @param intent    the original intent that failed
     * @param reason    why the fallback was triggered
     * @return a natural chat response
     */
    public String fallback(String input, ConversationContext context, String intent, String reason) {
        log.info("[FALLBACK] Intent='{}' fell back to ChatSkill. Reason: {}", intent, reason);
        return chatSkill.execute("CHAT", context);
    }

    /**
     * Check if an input is a general knowledge question that should be
     * answered by ChatSkill even when a course is active.
     * e.g. "What is Java" should NOT show "No active course"
     */
    public boolean isGeneralKnowledgeQuestion(String input) {
        if (input == null || input.isBlank()) return false;
        String t = input.toLowerCase().trim();
        // Teaching questions that are general knowledge, not lesson-specific
        return t.startsWith("what is ")
                || t.startsWith("what's ")
                || t.startsWith("what are ")
                || t.startsWith("who is ")
                || t.startsWith("tell me about ")
                || t.startsWith("explain ")
                || t.startsWith("how does ")
                || t.startsWith("how do ")
                || t.startsWith("describe ")
                || t.startsWith("define ")
                || t.startsWith("what does ")
                || t.startsWith("what was ")
                || t.startsWith("when was ")
                || t.startsWith("where is ");
    }
}