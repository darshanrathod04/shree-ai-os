package com.shreeai.os.platform.kernels.tool.validation;

import com.shreeai.os.platform.kernels.tool.model.ToolRequest;
import com.shreeai.os.platform.kernels.tool.model.ToolType;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <b>ToolValidator</b> — Stateless validator for Tool Kernel requests.
 *
 * <p><b>Ownership:</b> Tool Kernel — Validation</p>
 * <p><b>Version:</b> 1.0</p>
 */
public final class ToolValidator {

    private static final Map<ToolType, List<String>> SUPPORTED_ACTIONS = Map.of(
            ToolType.GIT, List.of("clone", "commit", "push", "pull", "branch", "status", "log", "diff"),
            ToolType.FILES, List.of("read", "write", "delete", "list", "move", "copy", "exists"),
            ToolType.BROWSER, List.of("navigate", "click", "type", "screenshot", "extract", "scroll"),
            ToolType.DB, List.of("query", "insert", "update", "delete", "schema", "tables")
    );

    public ToolValidator() {
        // Stateless
    }

    /**
     * Validates a tool request. Checks null constraints, action presence,
     * and that the action is supported for the given tool type.
     *
     * @param request the request to validate (may be {@code null})
     * @return the validation result
     */
    public ToolValidationResult validate(ToolRequest request) {
        List<String> violations = new ArrayList<>();

        if (request == null) {
            violations.add("ToolRequest must not be null");
            return ToolValidationResult.invalid(violations, Instant.now(), Map.of());
        }

        if (request.toolId() == null || request.toolId().trim().isEmpty()) {
            violations.add("toolId must not be null or empty");
        }
        if (request.toolType() == null) {
            violations.add("toolType must not be null");
        }
        if (request.action() == null || request.action().trim().isEmpty()) {
            violations.add("action must not be null or empty");
        }

        if (violations.isEmpty()) {
            List<String> supported = SUPPORTED_ACTIONS.get(request.toolType());
            if (supported != null && !supported.contains(request.action().toLowerCase())) {
                violations.add("Action '" + request.action() + "' is not supported for tool type " +
                        request.toolType());
            }
        }

        Instant now = Instant.now();
        if (violations.isEmpty()) {
            return ToolValidationResult.valid(now);
        }
        return ToolValidationResult.invalid(violations, now,
                Map.of("toolType", request.toolType(), "action", request.action()));
    }

    /**
     * Returns the supported actions for a given tool type.
     *
     * @param toolType the tool type
     * @return the list of supported actions, or empty list if none
     */
    public List<String> getSupportedActions(ToolType toolType) {
        return SUPPORTED_ACTIONS.getOrDefault(toolType, List.of());
    }
}
