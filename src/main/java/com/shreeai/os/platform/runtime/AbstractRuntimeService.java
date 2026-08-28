package com.shreeai.os.platform.runtime;

public abstract class AbstractRuntimeService implements RuntimeService {

    private RuntimeState state = RuntimeState.CREATED;

    @Override
    public void initialize() {
        state = RuntimeState.INITIALIZED;
        System.out.println("[INIT] " + getName());
    }

    @Override
    public void start() {
        state = RuntimeState.STARTED;
        System.out.println("[START] " + getName());
    }

    @Override
    public void verify() {
        state = RuntimeState.VERIFIED;
        System.out.println("[VERIFY] " + getName());
    }

    @Override
    public void shutdown() {
        state = RuntimeState.STOPPED;
        System.out.println("[STOP] " + getName());
    }

    public RuntimeState getState() {
        return state;
    }
}