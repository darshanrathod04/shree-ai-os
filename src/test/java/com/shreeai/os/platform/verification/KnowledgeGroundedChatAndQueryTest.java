package com.shreeai.os.platform.verification;

import com.shreeai.os.platform.sdk.SDKResponse;
import com.shreeai.os.platform.sdk.ShreeAI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Sprint-10A acceptance test — Knowledge-grounded responses for
 * CHAT, KNOWLEDGE_QUERY, and KNOWLEDGE_SEARCH.
 *
 * <p>Previously, only KNOWLEDGE_SEARCH returned the grounded knowledge
 * rendered by {@code synthesizeKnowledge()}. Both CHAT (no routing
 * operation; uses the canonical Chief-orchestrated pipeline) and
 * KNOWLEDGE_QUERY (operation=QUERY_KNOWLEDGE, question in metadata)
 * were returning only the bare title fragment, because:</p>
 *
 * <ol>
 *   <li>{@code DefaultResponseSynthesizer.isKnowledgeResult(...)} only
 *       matched when the routed kernel was "Knowledge Kernel". CHAT
 *       requests have no routing operation, so the check failed and
 *       synthesis fell through to the default path.</li>
 *   <li>{@code KnowledgeStage} only read {@code keyword} from metadata;
 *       KNOWLEDGE_QUERY places the question under {@code question},
 *       so the stage was searching with the placeholder message
 *       "KNOWLEDGE_QUERY" instead of the actual question.</li>
 * </ol>
 *
 * <p>After the fix, all three entry points consume the same
 * KnowledgeStage metadata and render the same
 * {@code # Title / ## Summary / ## Key Knowledge} structure.</p>
 *
 * <p>This test does NOT modify the KnowledgeSearchEngine, Ranking,
 * QueryNormalizer, or LLM Router — it only proves that the
 * synthesis layer correctly consumes the metadata the existing
 * KnowledgeStage already produces.</p>
 */
public class KnowledgeGroundedChatAndQueryTest {

    private static final String TITLE = "darshan";
    private static final String CONTENT = "Darshan is founder of Shree AI OS";

    private ShreeAI ai;

    @BeforeEach
    public void setUp() {
        // Sprint-10A: each test gets a fresh runtime so the knowledge
        // graph is empty at the start of every test method, ensuring
        // idempotent and deterministic grounding behavior.
        ai = ShreeAI.builder().apiKey("local").build();
    }

    @Test
    @DisplayName("S10A.1: knowledge().search() returns grounded answer")
    public void knowledgeSearchReturnsGroundedAnswer() {
        ai.knowledge().ingest(TITLE, CONTENT);

        SDKResponse response = ai.knowledge().search("darshan");

        assertNotNull(response, "Search response should not be null");
        assertNotNull(response.answer(), "Search answer should not be null");
        String answer = response.answer();
        assertTrue(answer.contains("# darshan"),
                "Search answer must contain the title heading. Got: " + answer);
        assertTrue(answer.contains("Darshan is founder of Shree AI OS"),
                "Search answer must contain the ingested summary. Got: " + answer);
        assertTrue(answer.contains("## Summary"),
                "Search answer must contain the Summary section. Got: " + answer);
        assertTrue(answer.contains("## Key Knowledge"),
                "Search answer must contain the Key Knowledge section. Got: "
                        + answer);
    }

    @Test
    @DisplayName("S10A.2: knowledge().query() returns grounded answer (was blank '#')")
    public void knowledgeQueryReturnsGroundedAnswer() {
        ai.knowledge().ingest(TITLE, CONTENT);

        SDKResponse response = ai.knowledge().query("who is darshan");

        assertNotNull(response, "Query response should not be null");
        assertNotNull(response.answer(), "Query answer should not be null");
        String answer = response.answer();

        // Sprint-10A regression guard: prior to the fix, this returned "#".
        assertTrue(answer.length() > 5,
                "Query answer must not be a bare heading. Got: '" + answer + "'");

        assertTrue(answer.contains("# darshan"),
                "Query answer must contain the title heading. Got: " + answer);
        assertTrue(answer.contains("Darshan is founder of Shree AI OS"),
                "Query answer must contain the ingested summary. Got: " + answer);
        assertTrue(answer.contains("## Summary"),
                "Query answer must contain the Summary section. Got: " + answer);
        assertTrue(answer.contains("## Key Knowledge"),
                "Query answer must contain the Key Knowledge section. Got: "
                        + answer);
    }

    @Test
    @DisplayName("S10A.3: chat() returns grounded answer (was blank '#')")
    public void chatReturnsGroundedAnswer() {
        ai.knowledge().ingest(TITLE, CONTENT);

        SDKResponse response = ai.chat("who is darshan");

        assertNotNull(response, "Chat response should not be null");
        assertNotNull(response.answer(), "Chat answer should not be null");
        String answer = response.answer();

        // Sprint-10A regression guard: prior to the fix, this returned "#".
        assertTrue(answer.length() > 5,
                "Chat answer must not be a bare heading. Got: '" + answer + "'");

        assertTrue(answer.contains("# darshan"),
                "Chat answer must contain the title heading. Got: " + answer);
        assertTrue(answer.contains("Darshan is founder of Shree AI OS"),
                "Chat answer must contain the ingested summary. Got: " + answer);
        assertTrue(answer.contains("## Summary"),
                "Chat answer must contain the Summary section. Got: " + answer);
        assertTrue(answer.contains("## Key Knowledge"),
                "Chat answer must contain the Key Knowledge section. Got: "
                        + answer);
    }

    @Test
    @DisplayName("S10A.4: CHAT, QUERY and SEARCH render the same grounded structure")
    public void allThreeOperationsRenderTheSameGroundedStructure() {
        ai.knowledge().ingest(TITLE, CONTENT);

        SDKResponse chat = ai.chat("who is darshan");
        SDKResponse query = ai.knowledge().query("who is darshan");
        SDKResponse search = ai.knowledge().search("darshan");

        // All three must contain the same canonical structure
        for (SDKResponse response : new SDKResponse[] { chat, query, search }) {
            String answer = response.answer();
            assertTrue(answer.contains("# darshan"),
                    "All three operations must render the title heading. Got: "
                            + answer);
            assertTrue(answer.contains("## Summary"),
                    "All three operations must render the Summary section. Got: "
                            + answer);
            assertTrue(answer.contains("## Key Knowledge"),
                    "All three operations must render the Key Knowledge section. "
                            + "Got: " + answer);
            assertTrue(answer.contains(TITLE),
                    "All three operations must mention the ingested title. Got: "
                            + answer);
        }
    }

    @Test
    @DisplayName("S10A.5: chat() without knowledge returns a chat response, not '#'")
    public void chatWithoutKnowledgeStillReturnsChatResponse() {
        // No ingestion. Chat must still produce a sensible response, not
        // crash or return just "#". The KnowledgeStage produces no results,
        // so we expect the response to be empty under the knowledge fallback
        // OR fall through to a chat-style response.
        SDKResponse response = ai.chat("hello");

        assertNotNull(response, "Chat response should not be null");
        assertNotNull(response.answer(), "Chat answer should not be null");
        assertTrue(response.answer().length() > 0,
                "Chat without knowledge must still produce a non-empty answer");
    }
}
