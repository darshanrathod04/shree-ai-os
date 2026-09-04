package com.shreeai.os.platform.kernels.developer.patch.model;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <b>RollbackPlan</b>
 *
 * <p>Immutable metadata describing how to undo a series of applied patches.
 * For each file that was modified, the plan records the file path and the
 * ordered list of undo actions needed to restore the original state.</p>
 *
 * <p>This is <b>metadata only</b>: the plan describes what should be undone
 * but is not automatically executed.</p>
 *
 * <p><b>Ownership:</b> Developer Workflow (Sprint-17)</p>
 *
 * @since Sprint-17
 */
public final class RollbackPlan {

    /** Type of undo action. */
    public enum UndoType { REMOVE_IMPORT, REMOVE_METHOD, REMOVE_FIELD, REMOVE_FILE, RESTORE_FILE }

    private final String planId;
    private final List<RollbackEntry> entries;
    private final Instant createdAt;
    private final Map<String, Object> metadata;

    private RollbackPlan(Builder b) {
        this.planId = b.planId == null ? "rollback-" + System.nanoTime() : b.planId;
        this.entries = List.copyOf(b.entries == null ? List.of() : b.entries);
        this.createdAt = b.createdAt == null ? Instant.now() : b.createdAt;
        this.metadata = Map.copyOf(b.metadata == null ? Map.of() : b.metadata);
    }

    public String planId() { return planId; }
    public List<RollbackEntry> entries() { return entries; }
    public Instant createdAt() { return createdAt; }
    public Map<String, Object> metadata() { return metadata; }

    public int totalActions() {
        return entries.stream().mapToInt(e -> e.actions().size()).sum();
    }

    public int fileCount() { return entries.size(); }

    public boolean isEmpty() { return entries.isEmpty(); }

    public static Builder builder() { return new Builder(); }

    /**
     * <b>RollbackEntry</b> — per-file rollback plan.
     */
    public static final class RollbackEntry {
        private final String filePath;
        private final List<UndoAction> actions;
        private final String originalContent; // for restore

        public RollbackEntry(String filePath, List<UndoAction> actions, String originalContent) {
            this.filePath = Objects.requireNonNull(filePath);
            this.actions = List.copyOf(actions == null ? List.of() : actions);
            this.originalContent = originalContent == null ? "" : originalContent;
        }

        public String filePath() { return filePath; }
        public List<UndoAction> actions() { return actions; }
        public String originalContent() { return originalContent; }
    }

    /**
     * <b>UndoAction</b> — a single undo step.
     */
    public static final class UndoAction {
        private final UndoType type;
        private final String description;
        private final String target; // e.g. method name, import

        public UndoAction(UndoType type, String description, String target) {
            this.type = Objects.requireNonNull(type);
            this.description = description == null ? "" : description;
            this.target = target == null ? "" : target;
        }

        public UndoType type() { return type; }
        public String description() { return description; }
        public String target() { return target; }
    }

    public static final class Builder {
        private String planId;
        private List<RollbackEntry> entries;
        private Instant createdAt;
        private Map<String, Object> metadata;

        public Builder planId(String v) { this.planId = v; return this; }
        public Builder entries(List<RollbackEntry> v) { this.entries = v; return this; }
        public Builder createdAt(Instant v) { this.createdAt = v; return this; }
        public Builder metadata(Map<String, Object> v) { this.metadata = v; return this; }
        public Builder addEntry(RollbackEntry e) {
            if (this.entries == null) this.entries = new java.util.ArrayList<>();
            this.entries.add(e);
            return this;
        }

        public RollbackPlan build() { return new RollbackPlan(this); }
    }
}
