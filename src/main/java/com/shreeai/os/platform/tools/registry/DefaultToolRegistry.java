package com.shreeai.os.platform.tools.registry;

import com.shreeai.os.platform.tools.api.Tool;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe implementation of ToolRegistry.
 */
public final class DefaultToolRegistry implements ToolRegistry {

    private final Map<String, Tool> tools = new ConcurrentHashMap<>();

    @Override
    public void register(Tool tool) {

        if (tool == null) {
            throw new IllegalArgumentException("Tool cannot be null");
        }

        tools.put(tool.id(), tool);
    }

    @Override
    public Optional<Tool> find(String toolId) {
        return Optional.ofNullable(tools.get(toolId));
    }

    @Override
    public Collection<Tool> getAll() {
        return Collections.unmodifiableCollection(tools.values());
    }

    @Override
    public boolean contains(String toolId) {
        return tools.containsKey(toolId);
    }
}