package com.shreeai.os.platform.production;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * ConversationOptimizer detects lightweight replies that should
 * NEVER invoke heavy prompt builders or LLM calls.
 *
 * These are simple acknowledgments or navigation commands
 * that can be handled instantly.
 *
 * Logs: [MODE]
 */
@Component
public class ConversationOptimizer {

    private static final Logger log = LoggerFactory.getLogger(ConversationOptimizer.class);

    private static final Set<String> LIGHTWEIGHT_ACKS = Set.of(
            "ok", "okay", "k", "kk", "thanks", "thank you", "ty",
            "yes", "y", "yeah", "yep", "sure", "no", "n", "nope",
            "good", "nice", "great", "awesome", "cool", "fine",
            "got it", "understood", "i see", "makes sense",
            "stop", "exit", "quit", "bye", "goodbye", "cya",
            "hello", "hi", "hey", "hii", "heyy"
    );

    private static final Set<String> LIGHTWEIGHT_NAV = Set.of(
            "continue", "next", "repeat", "back", "previous",
            "resume", "go on", "keep going", "advance"
    );

    /**
     * Check if the input is a lightweight acknowledgment
     * that doesn't need an LLM call.
     */
    public boolean isLightweightAck(String input) {
        if (input == null || input.isBlank()) return false;
        String t = input.toLowerCase().trim();
        // Remove punctuation
        t = t.replaceAll("[.,!?;:]", "").trim();
        return LIGHTWEIGHT_ACKS.contains(t);
    }

    /**
     * Check if the input is a lightweight navigation command.
     */
    public boolean isLightweightNav(String input) {
        if (input == null || input.isBlank()) return false;
        String t = input.toLowerCase().trim();
        t = t.replaceAll("[.,!?;:]", "").trim();
        return LIGHTWEIGHT_NAV.contains(t);
    }

    /**
     * Get an instant response for a lightweight input.
     * Returns null if the input is not lightweight.
     */
    public String getInstantResponse(String input) {
        if (input == null) return null;
        String t = input.toLowerCase().trim().replaceAll("[.,!?;:]", "").trim();

        if (Set.of("ok", "okay", "k", "kk").contains(t)) {
            log.info("[MODE] Lightweight ACK: '{}'", t);
            return "Got it! 👍";
        }
        if (Set.of("thanks", "thank you", "ty").contains(t)) {
            log.info("[MODE] Lightweight ACK: '{}'", t);
            return "You're welcome! 😊";
        }
        if (Set.of("yes", "y", "yeah", "yep", "sure").contains(t)) {
            log.info("[MODE] Lightweight ACK: '{}'", t);
            return "Okay! What would you like to do?";
        }
        if (Set.of("no", "n", "nope").contains(t)) {
            log.info("[MODE] Lightweight ACK: '{}'", t);
            return "Alright, let me know if you need anything.";
        }
        if (Set.of("good", "nice", "great", "awesome", "cool", "fine").contains(t)) {
            log.info("[MODE] Lightweight ACK: '{}'", t);
            return "Glad to hear that! 😊";
        }
        if (Set.of("got it", "understood", "i see", "makes sense").contains(t)) {
            log.info("[MODE] Lightweight ACK: '{}'", t);
            return "Great! Moving on...";
        }
        if (Set.of("hello", "hi", "hey", "hii", "heyy").contains(t)) {
            log.info("[MODE] Lightweight GREETING: '{}'", t);
            return null; // Let greeting handler process
        }
        if (Set.of("stop", "exit", "quit", "bye", "goodbye", "cya").contains(t)) {
            log.info("[MODE] Lightweight EXIT: '{}'", t);
            return "Goodbye! See you later. 👋";
        }

        return null;
    }
}