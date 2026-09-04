package com.shreeai.os.platform.services;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * <b>PlaygroundSessionTest</b>
 *
 * <p>12 test cases for PlaygroundSessionService.</p>
 *
 * @since v1.0
 */
public class PlaygroundSessionTest {

    private final PlaygroundSessionService service = new PlaygroundSessionService();

    @Test
    void createSession_returnsUniqueId() {
        String id1 = service.createSession();
        String id2 = service.createSession();
        assertNotNull(id1);
        assertNotNull(id2);
        assertNotEquals(id1, id2);
        assertEquals(2, service.sessionCount());
    }

    @Test
    void getSession_existingSession() {
        String id = service.createSession();
        Optional<PlaygroundSessionService.Session> session = service.getSession(id);
        assertTrue(session.isPresent());
        assertEquals(id, session.get().id());
    }

    @Test
    void getSession_unknownId_returnsEmpty() {
        Optional<PlaygroundSessionService.Session> session = service.getSession("unknown");
        assertTrue(session.isEmpty());
    }

    @Test
    void addConversation_appendsToHistory() {
        String id = service.createSession();
        service.addConversation(id, "My name is Darshan", "Hello Darshan!");

        List<PlaygroundSessionService.MessageTurn> history = service.getHistory(id);
        assertEquals(2, history.size());
        assertEquals("user", history.get(0).role());
        assertEquals("My name is Darshan", history.get(0).content());
        assertEquals("assistant", history.get(1).role());
        assertTrue(history.get(1).isAssistant());
    }

    @Test
    void session_remembersContextAcrossRequests() {
        String id = service.createSession();
        service.addConversation(id, "My name is Darshan", "Hello Darshan!");
        service.addConversation(id, "What is my name?", "Your name is Darshan.");

        List<String> ctx = service.getContext(id, 4);
        assertEquals(4, ctx.size());
        assertTrue(ctx.get(0).contains("My name is Darshan"));
        assertTrue(ctx.get(3).contains("Darshan"));
    }

    @Test
    void getContextAsString_concatenates() {
        String id = service.createSession();
        service.addConversation(id, "Hello", "Hi there");

        String ctx = service.getContextAsString(id, 2);
        assertTrue(ctx.contains("user: Hello"));
        assertTrue(ctx.contains("assistant: Hi there"));
    }

    @Test
    void hasSession_returnsTrueForExisting() {
        String id = service.createSession();
        assertTrue(service.hasSession(id));
        assertFalse(service.hasSession("nonexistent"));
    }

    @Test
    void deleteSession_removesSession() {
        String id = service.createSession();
        assertTrue(service.deleteSession(id));
        assertFalse(service.hasSession(id));
    }

    @Test
    void deleteSession_unknown_returnsFalse() {
        assertFalse(service.deleteSession("unknown"));
    }

    @Test
    void messageTurn_isUserAndAssistant() {
        var userTurn = new PlaygroundSessionService.MessageTurn("user", "Hello", java.time.Instant.now());
        var assistantTurn = new PlaygroundSessionService.MessageTurn("assistant", "Hi", java.time.Instant.now());

        assertTrue(userTurn.isUser());
        assertFalse(userTurn.isAssistant());
        assertTrue(assistantTurn.isAssistant());
        assertFalse(assistantTurn.isUser());
    }

    @Test
    void messageTurn_contains_caseInsensitive() {
        var turn = new PlaygroundSessionService.MessageTurn("user", "My name is DARSHAN", java.time.Instant.now());
        assertTrue(turn.contains("darshan"));
        assertTrue(turn.contains("DARSHAN"));
        assertFalse(turn.contains("Alice"));
    }

    @Test
    void session_turnCount() {
        String id = service.createSession();
        service.addConversation(id, "msg1", "resp1");
        service.addConversation(id, "msg2", "resp2");
        service.addConversation(id, "msg3", "resp3");

        Optional<PlaygroundSessionService.Session> session = service.getSession(id);
        assertTrue(session.isPresent());
        assertEquals(6, session.get().turnCount());
    }
}
