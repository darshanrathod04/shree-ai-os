package com.shreeai.os.platform.tools.api;

import com.shreeai.os.platform.tools.model.ToolRequest;
import com.shreeai.os.platform.tools.model.ToolResponse;

/**
 * Constitutional Tool Contract.
 *
 * Every executable capability in Shree AI OS implements this interface.
 */
public interface Tool {

    String id();

    String name();

    String description();

    ToolResponse execute(ToolRequest request);

}