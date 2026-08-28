package com.shreeai.os.platform.sdk.events;

/**
 * Developer-facing runtime event listener.
 */
@FunctionalInterface
public interface RuntimeEventListener {

    void onEvent(RuntimeEvent event);
}