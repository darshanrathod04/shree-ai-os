package com.shreeai.os.platform.tools.mcp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

/**
 * <b>McpClient</b>
 *
 * <p>Model Context Protocol (MCP) JSON-RPC 2.0 client over stdio streams
 * (compatible with Anthropic MCP {@code 2024-11-05} specification). Supports
 * {@code initialize}, {@code tools/list}, and {@code tools/call}.</p>
 */
public final class McpClient implements Closeable {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String PROTOCOL_VERSION = "2024-11-05";

    private final BufferedReader reader;
    private final BufferedWriter writer;
    private final Process process;
    private final AtomicLong idSeq = new AtomicLong(1);

    public McpClient(InputStream in, OutputStream out) {
        this(in, out, null);
    }

    private McpClient(InputStream in, OutputStream out, Process process) {
        this.reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(in), StandardCharsets.UTF_8));
        this.writer = new BufferedWriter(new OutputStreamWriter(Objects.requireNonNull(out), StandardCharsets.UTF_8));
        this.process = process;
    }

    public static McpClient launchStdio(List<String> command) throws IOException {
        Objects.requireNonNull(command, "command must not be null");
        if (command.isEmpty()) {
            throw new IllegalArgumentException("command must not be empty");
        }
        Process p = new ProcessBuilder(command).start();
        return new McpClient(p.getInputStream(), p.getOutputStream(), p);
    }

    public synchronized JsonNode initialize() throws IOException {
        Map<String, Object> params = Map.of(
                "protocolVersion", PROTOCOL_VERSION,
                "capabilities", Map.of(),
                "clientInfo", Map.of("name", "shree-ai-os", "version", "1.1.0")
        );
        JsonNode result = sendRequest("initialize", params);
        sendNotification("notifications/initialized", Map.of());
        return result;
    }

    public synchronized List<McpToolDefinition> listTools() throws IOException {
        JsonNode result = sendRequest("tools/list", Map.of());
        List<McpToolDefinition> defs = new ArrayList<>();
        JsonNode toolsNode = result.path("tools");
        if (toolsNode.isArray()) {
            for (JsonNode t : toolsNode) {
                defs.add(new McpToolDefinition(
                        t.path("name").asText(),
                        t.path("description").asText("")
                ));
            }
        }
        return List.copyOf(defs);
    }

    public synchronized McpCallResult callTool(String name, Map<String, Object> arguments) throws IOException {
        Objects.requireNonNull(name, "name must not be null");
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("name", name);
        params.put("arguments", arguments == null ? Map.of() : arguments);

        JsonNode result = sendRequest("tools/call", params);
        boolean isError = result.path("isError").asBoolean(false);
        StringBuilder text = new StringBuilder();
        JsonNode content = result.path("content");
        if (content.isArray()) {
            for (JsonNode item : content) {
                if (item.has("text")) {
                    if (text.length() > 0) text.append('\n');
                    text.append(item.get("text").asText());
                }
            }
        }
        return new McpCallResult(!isError, text.toString());
    }

    private JsonNode sendRequest(String method, Map<String, Object> params) throws IOException {
        long id = idSeq.getAndIncrement();
        Map<String, Object> req = new LinkedHashMap<>();
        req.put("jsonrpc", "2.0");
        req.put("id", id);
        req.put("method", method);
        req.put("params", params);

        writer.write(MAPPER.writeValueAsString(req));
        writer.newLine();
        writer.flush();

        String line = reader.readLine();
        if (line == null) {
            throw new IOException("MCP server closed stream unexpectedly on method: " + method);
        }
        JsonNode resp = MAPPER.readTree(line);
        if (resp.hasNonNull("error")) {
            throw new IOException("MCP error: " + resp.get("error").path("message").asText(resp.get("error").toString()));
        }
        return resp.path("result");
    }

    private void sendNotification(String method, Map<String, Object> params) throws IOException {
        Map<String, Object> msg = new LinkedHashMap<>();
        msg.put("jsonrpc", "2.0");
        msg.put("method", method);
        msg.put("params", params);
        writer.write(MAPPER.writeValueAsString(msg));
        writer.newLine();
        writer.flush();
    }

    @Override
    public void close() throws IOException {
        try {
            writer.close();
        } finally {
            reader.close();
            if (process != null) {
                process.destroy();
            }
        }
    }

    public record McpToolDefinition(String name, String description) {}

    public record McpCallResult(boolean success, String content) {}
}
