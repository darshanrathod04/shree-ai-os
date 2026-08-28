package com.shreeai.os.platform.tools.registry;

import com.shreeai.os.platform.tools.api.Tool;

import java.util.Collection;
import java.util.Optional;

/**
 * Constitutional registry for executable tools.
 */
public interface ToolRegistry {

    void register(Tool tool);

    Optional<Tool> find(String toolId);

    Collection<Tool> getAll();

    boolean contains(String toolId);

}