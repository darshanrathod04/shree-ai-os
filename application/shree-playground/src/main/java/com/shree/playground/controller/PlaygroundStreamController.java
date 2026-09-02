package com.shree.playground.controller;

import com.shreeai.os.platform.sdk.ShreeAI;
import com.shreeai.os.platform.sdk.events.EventType;
import com.shreeai.os.platform.sdk.events.RuntimeEvent;
import com.shreeai.os.platform.sdk.events.RuntimeEventListener;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/playground/stream")

public class PlaygroundStreamController {

    private final ShreeAI ai;

     public PlaygroundStreamController(ShreeAI ai) {
        this.ai = ai;
    }

    @GetMapping
    public SseEmitter stream() {

        SseEmitter emitter = new SseEmitter(0L);

        RuntimeEventListener listener = new RuntimeEventListener() {
            @Override
            public void onEvent(RuntimeEvent event) {
                try {
                    emitter.send(
                            SseEmitter.event()
                                    .id(event.requestId())
                                    .name(event.type().name())
                                    .data(event)
                    );
                } catch (Exception ignored) {
                    cleanup(this);
                }
            }
        };

        // Subscribe to every runtime event
        for (EventType type : EventType.values()) {
            ai.events().on(type, listener);
        }

        emitter.onCompletion(() -> cleanup(listener));
        emitter.onTimeout(() -> cleanup(listener));
        emitter.onError(error -> cleanup(listener));

        return emitter;
    }

    private void cleanup(RuntimeEventListener listener) {
        for (EventType type : EventType.values()) {
            ai.events().off(type, listener);
        }
    }
}