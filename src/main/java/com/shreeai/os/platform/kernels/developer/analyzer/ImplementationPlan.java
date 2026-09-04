package com.shreeai.os.platform.kernels.developer.analyzer;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <b>ImplementationPlan</b>
 *
 * <p>Immutable structured implementation plan produced by {@link ImplementationPlanner}.
 * Contains ordered phases, each with an objective, affected files, dependencies,
 * verification criteria, and risk notes.</p>
 *
 * <p><b>Ownership:</b> Developer Agent (Sprint-14)</p>
 *
 * @since Sprint-14
 */
public final class ImplementationPlan {

    private final String request;
    private final List<Phase> phases;
    private final Map<String, Object> metadata;
    private final double confidence;
    private final List<String> risks;

    private ImplementationPlan(Builder b) {
        this.request = Objects.requireNonNull(b.request, "request");
        this.phases = List.copyOf(b.phases == null ? List.of() : b.phases);
        this.metadata = Map.copyOf(b.metadata == null ? Map.of() : b.metadata);
        this.confidence = Math.max(0.0, Math.min(1.0, b.confidence));
        this.risks = List.copyOf(b.risks == null ? List.of() : b.risks);
    }

    public String request() { return request; }
    public List<Phase> phases() { return phases; }
    public Map<String, Object> metadata() { return metadata; }
    public double confidence() { return confidence; }
    public List<String> risks() { return risks; }

    public static Builder builder() { return new Builder(); }

    /**
     * <b>Phase</b> — a single ordered implementation step.
     */
    public static final class Phase {

        private final int number;
        private final String objective;
        private final String description;
        private final List<String> affectedFiles;
        private final List<String> dependencies;
        private final List<String> verificationCriteria;
        private final List<String> riskNotes;

        private Phase(Builder b) {
            this.number = b.number;
            this.objective = Objects.requireNonNull(b.objective, "objective");
            this.description = b.description == null ? "" : b.description;
            this.affectedFiles = List.copyOf(b.affectedFiles == null ? List.of() : b.affectedFiles);
            this.dependencies = List.copyOf(b.dependencies == null ? List.of() : b.dependencies);
            this.verificationCriteria = List.copyOf(b.verificationCriteria == null ? List.of() : b.verificationCriteria);
            this.riskNotes = List.copyOf(b.riskNotes == null ? List.of() : b.riskNotes);
        }

        public int number() { return number; }
        public String objective() { return objective; }
        public String description() { return description; }
        public List<String> affectedFiles() { return affectedFiles; }
        public List<String> dependencies() { return dependencies; }
        public List<String> verificationCriteria() { return verificationCriteria; }
        public List<String> riskNotes() { return riskNotes; }

        public static Builder builder() { return new Builder(); }

        public static final class Builder {
            private int number;
            private String objective;
            private String description;
            private List<String> affectedFiles;
            private List<String> dependencies;
            private List<String> verificationCriteria;
            private List<String> riskNotes;

            public Builder number(int v) { this.number = v; return this; }
            public Builder objective(String v) { this.objective = v; return this; }
            public Builder description(String v) { this.description = v; return this; }
            public Builder affectedFiles(List<String> v) { this.affectedFiles = v; return this; }
            public Builder dependencies(List<String> v) { this.dependencies = v; return this; }
            public Builder verificationCriteria(List<String> v) { this.verificationCriteria = v; return this; }
            public Builder riskNotes(List<String> v) { this.riskNotes = v; return this; }

            public Phase build() { return new Phase(this); }
        }
    }

    public static final class Builder {
        private String request;
        private List<Phase> phases;
        private Map<String, Object> metadata;
        private double confidence = 0.8;
        private List<String> risks;

        public Builder request(String v) { this.request = v; return this; }
        public Builder phases(List<Phase> v) { this.phases = v; return this; }
        public Builder metadata(Map<String, Object> v) { this.metadata = v; return this; }
        public Builder confidence(double v) { this.confidence = v; return this; }
        public Builder risks(List<String> v) { this.risks = v; return this; }

        public ImplementationPlan build() { return new ImplementationPlan(this); }
    }
}
