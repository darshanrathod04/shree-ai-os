package com.shreeai.os.platform.kernels.cognitive.model;

public final class CognitiveDecision {
    public enum Action {
        EXECUTE_COURSE,
        START_QUIZ,
        SHOW_ROADMAP,
        GENERATE_RESPONSE,
        ASK_QUESTION,
        NONE
    }
    private final Action action;
    private final String reason;
    private CognitiveDecision(Action action, String reason) {
        this.action = action != null ? action : Action.NONE;
        this.reason = reason != null ? reason : "";
    }
    public Action getAction() { return action; }
    public String getReason() { return reason; }
    public static CognitiveDecision of(Action action, String reason) { return new CognitiveDecision(action, reason); }
    public static CognitiveDecision none() { return new CognitiveDecision(Action.NONE, ""); }
    @Override public String toString() { return "CognitiveDecision{action=" + action + ", reason='" + reason + "'}"; }
}
