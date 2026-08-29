package com.shreeai.os.platform.kernels.tool.model;

import java.util.Map;
import java.util.Objects;

/**
 * <b>ToolRequest</b>
 *
 * <p>Represents a request to execute a tool operation.
 * This immutable value object encapsulates a tool execution intent,
 * specifying which tool type, action, and parameters are required.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Encapsulates tool execution parameters.</li>
 *   <li>Provides immutable execution context.</li>
 *   <li>Defines the action to perform and its parameters.</li>
 *   <li>Contains no execution logic.</li>
 * </ul>
 *
 * <p><b>Design Principles:</b></p>
 * <ul>
 *   <li>Immutable — all fields are final.</li>
 *   <li>Constructor validation — rejects null arguments.</li>
 *   <li>Defensive copying — protects mutable collections.</li>
 *   <li>Value-based equality — implements equals, hashCode, toString.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Tool Kernel — Domain Model</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> TK-102, EIO-ARCH-001</p>
 *
 * @param toolId      the tool instance identifier (must not be {@code null})
 * @param toolType    the tool type (must not be {@code null})
 * @param action      the specific action to execute (must not be {@code null} or blank)
 * @param parameters  additional execution parameters (must not be {@code null})
 *
 * @since 1.0
 */
public final class ToolRequest {

    private final String toolId;
    private final ToolType toolType;
    private final String action;
    private final Map<String, Object> parameters;

    /**
     * Constructs a {@code ToolRequest} with the specified parameters.
     *
     * @param toolId      the tool instance identifier (must not be {@code null})
     * @param toolType    the tool type (must not be {@code null})
     * @param action      the specific action to execute (must not be {@code null} or blank)
     * @param parameters  additional execution parameters (must not be {@code null})
     * @throws IllegalArgumentException if any parameter is {@code null} or empty
     */
    public ToolRequest(
            String toolId,
            ToolType toolType,
            String action,
            Map<String, Object> parameters) {
        if (toolId == null) {
            throw new IllegalArgumentException("ToolRequest toolId must not be null");
        }
        if (toolType == null) {
            throw new IllegalArgumentException("ToolRequest toolType must not be null");
        }
        if (action == null || action.trim().isEmpty()) {
            throw new IllegalArgumentException("ToolRequest action must not be null or empty");
        }
        if (parameters == null) {
            throw new IllegalArgumentException("ToolRequest parameters must not be null");
        }

        this.toolId = toolId;
        this.toolType = toolType;
        this.action = action;
        this.parameters = Map.copyOf(parameters);
    }

    /**
     * Returns the tool instance identifier.
     *
     * @return the tool identifier
     */
    public String toolId() {
        return toolId;
    }

    /**
     * Returns the tool type.
     *
     * @return the tool type
     */
    public ToolType toolType() {
        return toolType;
    }

    /**
     * Returns the specific action to execute.
     *
     * @return the action
     */
    public String action() {
        return action;
    }

    /**
     * Returns an unmodifiable view of the execution parameters.
     *
     * @return an unmodifiable map of parameters
     */
    public Map<String, Object> parameters() {
        return parameters;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ToolRequest that = (ToolRequest) obj;
        return toolId.equals(that.toolId) &&
                toolType == that.toolType &&
                action.equals(that.action) &&
                parameters.equals(that.parameters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(toolId, toolType, action, parameters);
    }

    @Override
    public String toString() {
        return "ToolRequest{" +
                "toolId='" + toolId + '\'' +
                ", toolType=" + toolType +
                ", action='" + action + '\'' +
                ", parameters=" + parameters +
                '}';
    }
}
