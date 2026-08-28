package com.shreeai.os.platform.llm.inmemory;

import com.shreeai.os.platform.llm.LlmProvider;
import com.shreeai.os.platform.llm.LlmRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Spliterators;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Deterministic, zero-dependency {@link LlmProvider} implementation used by
 * unit tests, local demos and the engineering gates.
 *
 * <p>It does <strong>not</strong> perform any network I/O. It synthesises a
 * replayable, fully-deterministic token stream derived from the request model
 * and prompt, so test assertions are stable across runs:</p>
 * <pre>{@code
 * LlmResponse r = new InMemoryLlmProvider()
 *         .complete(LlmRequest.builder().prompt("hello").build());
 * // r.content() == "default echoes: hello"
 * }</pre>
 *
 * <p>This provider intentionally keeps no Spring wiring so the SPI can be
 * validated without a container. The legacy {@code OllamaClient} and the
 * Runtime will later delegate to a real provider behind this same interface.</p>
 *
 * @since Sprint 6.2A-P1
 */
public final class InMemoryLlmProvider implements LlmProvider {

    @Override
    public String providerName() {
        return "in-memory";
    }

    @Override
    public Stream<String> stream(LlmRequest request) {
        String prompt = request == null || request.prompt() == null ? "" : request.prompt();
        String model = request == null || request.model() == null ? "default" : request.model();
        String content = model + " echoes: " + prompt;

        // Deterministically split into token fragments while preserving the
        // exact original spacing. Splitting around spaces keeps spaces as their
        // own tokens so that concatenating the stream reproduces {@code content}
        // byte-for-byte.
        List<String> tokens = splitPreservingSpaces(content);
        return StreamSupport.stream(
                Spliterators.spliterator(tokens.iterator(), tokens.size(),
                        Spliterator.ORDERED | Spliterator.IMMUTABLE | Spliterator.NONNULL),
                false);
    }

    private static List<String> splitPreservingSpaces(String s) {
        List<String> tokens = new ArrayList<>();
        if (s.isEmpty()) {
            return tokens;
        }
        // (?<= ) or (?= ) splits around each single space, keeping them as tokens.
        for (String part : s.split("(?<= )|(?= )", -1)) {
            tokens.add(part);
        }
        return tokens;
    }

    /**
     * Convenience accessor: the single deterministic response this provider
     * would produce for {@code request}. Equivalent to
     * {@code complete(request).content()} but avoids building a response.
     */
    public String echo(LlmRequest request) {
        String prompt = request == null || request.prompt() == null ? "" : request.prompt();
        String model = request == null || request.model() == null ? "default" : request.model();
        return model + " echoes: " + prompt;
    }
}
