package com.shreeai.os.platform.services;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <b>PlaygroundSessionService</b>
 *
 * <p>Manages conversation sessions for the Playground. Each session has a UUID
 * and accumulates a history of message turns. Sessions do not affect the
 * kernel's stateless nature — only the Playground SDK layer uses sessions.</p>
 *
 * <p><b>Session model:</b></p>
 * <ul>
 *   <li>Each request to the Playground can be tagged with a session ID</li>
 *   <li>The session stores the message history</li>
 *   <li>Context from previous turns is used to personalize responses</li>
 *   <li>The kernel itself remains stateless</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Services (v1.0)</p>
 *
 * @since v1.0
 */
public class PlaygroundSessionService {

    private final Map<String, Session> sessions = new ConcurrentHashMap<>();

    /**
     * Creates a new session and returns its ID.
     */
    public String createSession() {
        String id = UUID.randomUUID().toString();
        sessions.put(id, new Session(id, Instant.now()));
        return id;
    }

    /**
     * Retrieves a session by ID. Returns Optional.empty() if not found.
     */
    public Optional<Session> getSession(String sessionId) {
        return Optional.ofNullable(sessions.get(sessionId));
    }

    /**
     * Adds a turn to a session. Creates the session if it doesn't exist.
     */
    public void addTurn(String sessionId, MessageTurn turn) {
        Session session = sessions.computeIfAbsent(sessionId,
                k -> new Session(sessionId, Instant.now()));
        session.addTurn(turn);
    }

    /**
     * Adds a user message and assistant response turn to the session.
     */
    public void addConversation(String sessionId, String userMessage, String assistantResponse) {
        addTurn(sessionId, new MessageTurn("user", userMessage, Instant.now()));
        addTurn(sessionId, new MessageTurn("assistant", assistantResponse, Instant.now()));
    }

    /**
     * Returns the message history for a session.
     */
    public List<MessageTurn> getHistory(String sessionId) {
        Session s = sessions.get(sessionId);
        if (s == null) return List.of();
        return s.history();
    }

    /**
     * Returns the context from the last N turns for prompt injection.
     * For example, context("abc", 2) returns the last 2 messages.
     */
    public List<String> getContext(String sessionId, int lastNTurns) {
        List<MessageTurn> history = getHistory(sessionId);
        int from = Math.max(0, history.size() - lastNTurns);
        List<String> ctx = new ArrayList<>();
        for (int i = from; i < history.size(); i++) {
            MessageTurn t = history.get(i);
            ctx.add(t.role() + ": " + t.content());
        }
        return ctx;
    }

    /**
     * Returns the last N turns as a single context string.
     */
    public String getContextAsString(String sessionId, int lastNTurns) {
        return String.join("\n", getContext(sessionId, lastNTurns));
    }

    /**
     * Returns true if a session exists.
     */
    public boolean hasSession(String sessionId) {
        return sessions.containsKey(sessionId);
    }

    /**
     * Deletes a session.
     */
    public boolean deleteSession(String sessionId) {
        return sessions.remove(sessionId) != null;
    }

    /**
     * Returns the total number of active sessions.
     */
    public int sessionCount() {
        return sessions.size();
    }

    /**
     * <b>Session</b> — an active Playground conversation.
     */
    public static final class Session {
        private final String id;
        private final Instant createdAt;
        private final List<MessageTurn> history = Collections.synchronizedList(new ArrayList<>());
        private volatile Instant lastActivity;

        public Session(String id, Instant createdAt) {
            this.id = id;
            this.createdAt = createdAt;
            this.lastActivity = createdAt;
        }

        public String id() { return id; }
        public Instant createdAt() { return createdAt; }
        public Instant lastActivity() { return lastActivity; }
        public List<MessageTurn> history() { return List.copyOf(history); }
        public int turnCount() { return history.size(); }

        void addTurn(MessageTurn turn) {
            history.add(turn);
            lastActivity = Instant.now();
        }
    }

    /**
     * <b>MessageTurn</b> — a single message in a conversation.
     */
    public static final class MessageTurn {
        private final String role;       // "user" or "assistant"
        private final String content;
        private final Instant timestamp;

        public MessageTurn(String role, String content, Instant timestamp) {
            this.role = role == null ? "unknown" : role;
            this.content = content == null ? "" : content;
            this.timestamp = timestamp == null ? Instant.now() : timestamp;
        }

        public String role() { return role; }
        public String content() { return content; }
        public Instant timestamp() { return timestamp; }

        /** Returns true if this is a user message. */
        public boolean isUser() { return "user".equalsIgnoreCase(role); }

        /** Returns true if this is an assistant message. */
        public boolean isAssistant() { return "assistant".equalsIgnoreCase(role); }

        /** Returns true if the content matches a name query. */
        public boolean contains(String text) {
            return content.toLowerCase().contains(text == null ? "" : text.toLowerCase());
        }
    }
}
