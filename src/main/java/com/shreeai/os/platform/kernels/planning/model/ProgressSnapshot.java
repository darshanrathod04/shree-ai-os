package com.shreeai.os.platform.kernels.planning.model;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Deeply immutable progress snapshot. Missing tasks mean NOT_STARTED. */
public record ProgressSnapshot(List<ProgressEntry> progress) {
    public ProgressSnapshot {
        progress = List.copyOf(progress);
        Set<String> ids = new HashSet<>();
        for (ProgressEntry entry : progress) {
            if (!ids.add(entry.taskId())) {
                throw new IllegalArgumentException("duplicate progress task: " + entry.taskId());
            }
        }
    }

    public static ProgressSnapshot empty() {
        return new ProgressSnapshot(List.of());
    }
}
