package com.shreeai.os.platform.tools.impl;

import com.shreeai.os.platform.tools.api.Tool;
import com.shreeai.os.platform.tools.model.ToolRequest;
import com.shreeai.os.platform.tools.model.ToolResponse;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

/**
 * Constitutional Browser Tool.
 *
 * Safe read-only web fetch.
 */
public final class BrowserTool implements Tool {

    private final HttpClient client =
            HttpClient.newHttpClient();

    @Override
    public String id() {
        return "browser";
    }

    @Override
    public String name() {
        return "Browser Tool";
    }

    @Override
    public String description() {
        return "Read-only HTML fetch.";
    }

    @Override
    public ToolResponse execute(ToolRequest request) {

        String url = String.valueOf(
                request.arguments().getOrDefault("url", "")
        );

        if (url.isBlank()) {
            return ToolResponse.failure("URL required");
        }

        try {

            HttpRequest httpRequest =
                    HttpRequest.newBuilder()
                            .uri(URI.create(url))
                            .GET()
                            .build();

            HttpResponse<String> response =
                    client.send(
                            httpRequest,
                            HttpResponse.BodyHandlers.ofString()
                    );

            return ToolResponse.success(
                    "Page fetched",
                    Map.of(
                            "status", response.statusCode(),
                            "html", response.body()
                    )
            );

        } catch (Exception e) {

            return ToolResponse.failure(e.getMessage());

        }
    }
}