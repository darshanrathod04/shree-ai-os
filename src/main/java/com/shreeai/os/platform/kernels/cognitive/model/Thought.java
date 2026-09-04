package com.shreeai.os.platform.kernels.cognitive.model;

public final class Thought {
    private final String goal;
    private final String intent;
    private final String action;
    private final String reasoning;

    private Thought(String goal, String intent, String action, String reasoning) {
        this.goal = goal != null ? goal : "";
        this.intent = intent != null ? intent : "";
        this.action = action != null ? action : "";
        this.reasoning = reasoning != null ? reasoning : "";
    }

    public String getGoal() { return goal; }
    public String getIntent() { return intent; }
    public String getAction() { return action; }
    public String getReasoning() { return reasoning; }

    public static Thought of(String goal, String intent, String action, String reasoning) {
        return new Thought(goal, intent, action, reasoning);
    }

    @Override public String toString() {
        return "Thought{goal=" + goal + ", intent=" + intent + ", action=" + action + ", reasoning=" + reasoning + "}";
    }
}
