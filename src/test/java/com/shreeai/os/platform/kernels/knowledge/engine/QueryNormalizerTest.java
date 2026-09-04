package com.shreeai.os.platform.kernels.knowledge.engine;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for QueryNormalizer.
 * <p>
 * Verifies that the normalization layer correctly transforms user queries
 * for knowledge retrieval.
 */
class QueryNormalizerTest {

    @Test
    void normalizesWhoIsPrefix() {
        assertEquals("darshan", QueryNormalizer.normalize("who is darshan"));
        assertEquals("darshan", QueryNormalizer.normalize("Who is Darshan"));
        assertEquals("darshan", QueryNormalizer.normalize("  WHO IS DARSHAN  "));
    }

    @Test
    void normalizesWhatIsPrefix() {
        assertEquals("java", QueryNormalizer.normalize("what is java"));
        assertEquals("java", QueryNormalizer.normalize("What is Java"));
    }

    @Test
    void normalizesExplainPrefix() {
        assertEquals("html", QueryNormalizer.normalize("explain html"));
        assertEquals("html", QueryNormalizer.normalize("Explain HTML"));
    }

    @Test
    void normalizesTellMeAboutPrefix() {
        assertEquals("spring boot", QueryNormalizer.normalize("tell me about spring boot"));
        assertEquals("spring boot", QueryNormalizer.normalize("Tell me about Spring Boot"));
    }

    @Test
    void preservesMultiWordEntities() {
        assertEquals("spring boot", QueryNormalizer.normalize("who is spring boot"));
        assertEquals("machine learning", QueryNormalizer.normalize("what is machine learning"));
    }

    @Test
    void handlesAlreadyNormalizedQuery() {
        assertEquals("darshan", QueryNormalizer.normalize("darshan"));
        assertEquals("java", QueryNormalizer.normalize("java"));
        assertEquals("spring boot", QueryNormalizer.normalize("spring boot"));
    }

    @Test
    void handlesNullInput() {
        assertEquals("", QueryNormalizer.normalize(null));
    }

    @Test
    void handlesBlankInput() {
        assertEquals("", QueryNormalizer.normalize(""));
        assertEquals("", QueryNormalizer.normalize("   "));
    }

    @Test
    void handlesEmptyPrefixOnlyQuery() {
        // After removing "who is", the query becomes empty
        assertEquals("", QueryNormalizer.normalize("who is"));
        assertEquals("", QueryNormalizer.normalize("who is "));
        assertEquals("", QueryNormalizer.normalize("what is"));
        assertEquals("", QueryNormalizer.normalize("explain"));
    }
}
