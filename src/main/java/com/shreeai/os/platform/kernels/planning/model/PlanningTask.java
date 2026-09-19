package com.shreeai.os.platform.kernels.planning.model;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Comparator;
import java.util.Locale;
import java.util.Objects;

/**
 * <b>PlanningTask</b>
 *
 * <p>A single executable node of a {@link TaskGraph}. A planning task is
 * produced by deterministic template expansion of a {@link PlanMilestone}
 * and carries only what P2.2 requires: identity, title, type, and order.</p>
 *
 * <p><b>Deterministic identity:</b> the {@code taskId} is a lower-case
 * SHA-256 hex digest over the canonical key
 * {@code milestone|order|type|title}. The same milestone, title, type, and
 * order therefore always produce the byte-identical identifier on every
 * JVM, every run, and every machine.</p>
 *
 * <p><b>Ordering:</b> {@code order} is the composite execution index -
 * milestone order first, task order within the milestone second. It is
 * always {@code >= 1} and increases monotonically across the whole graph.</p>
 *
 * <p><b>Design Principles:</b></p>
 * <ul>
 *   <li>Java 21 record - immutable by construction.</li>
 *   <li>Deeply immutable - every component is a {@code String}, enum, or
 *       primitive.</li>
 *   <li>Constructor validation - rejects null, blank, non-SHA-256 ids, and
 *       {@code order < 1}.</li>
 *   <li>Data-only - contains no scheduling, resource, or execution logic.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Planning Kernel - P2.2 Task Dependency Graph</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @param taskId the deterministic lower-case SHA-256 hex identifier
 *               (must be 64 characters, must not be null)
 * @param title  the human-readable task title (must not be null or blank)
 * @param type   the deterministic task classification (must not be null)
 * @param order  the composite execution order (must be {@code >= 1})
 *
 * @since P2.2
 * @see TaskType
 * @see TaskGraph
 * @see com.shreeai.os.platform.kernels.planning.engine.TaskDependencyGraphEngine
 */
public record PlanningTask(
        String taskId,
        String title,
        TaskType type,
        int order) {

    /** The smallest permitted {@link #order()} value. */
    public static final int MIN_ORDER = 1;

    /** The exact length of a SHA-256 hex digest. */
    public static final int TASK_ID_LENGTH = 64;

    /** Lower-case hexadecimal alphabet. */
    private static final char[] HEX_DIGITS = "0123456789abcdef".toCharArray();

    /**
     * Creates a validated, deeply-immutable planning task.
     *
     * @throws NullPointerException     if {@code taskId}, {@code title}, or
     *                                  {@code type} is null
     * @throws IllegalArgumentException if {@code title} is blank, if
     *                                  {@code order} is below
     *                                  {@link #MIN_ORDER}, or if
     *                                  {@code taskId} is not a 64-character
     *                                  lower-case SHA-256 hex digest
     */
    public PlanningTask {
        taskId = Objects.requireNonNull(taskId, "taskId must not be null").trim();
        title = Objects.requireNonNull(title, "title must not be null").trim();
        Objects.requireNonNull(type, "type must not be null");
        if (title.isEmpty()) {
            throw new IllegalArgumentException("title must not be blank");
        }
        if (order < MIN_ORDER) {
            throw new IllegalArgumentException("order must be >= " + MIN_ORDER + ": " + order);
        }
        if (!isValidTaskId(taskId)) {
            throw new IllegalArgumentException(
                    "taskId must be a " + TASK_ID_LENGTH
                            + "-character lower-case SHA-256 hex digest: " + taskId);
        }
    }

    /**
     * Creates a planning task whose identifier is derived deterministically
     * from the supplied milestone and task descriptors.
     *
     * @param milestoneName the milestone that produced this task (must not be
     *                      null)
     * @param title         the task title (must not be null or blank)
     * @param type          the task classification (must not be null)
     * @param order         the composite execution order (must be
     *                      {@code >= 1})
     * @return a new deterministic planning task (never null)
     * @throws NullPointerException     if any reference argument is null
     * @throws IllegalArgumentException if {@code title} is blank or
     *                                  {@code order} is below
     *                                  {@link #MIN_ORDER}
     */
    public static PlanningTask create(
            String milestoneName,
            String title,
            TaskType type,
            int order) {
        return new PlanningTask(
                computeTaskId(milestoneName, title, type, order),
                title, type, order);
    }

    /**
     * Computes the deterministic SHA-256 identifier for a task descriptor.
     *
     * <p>The canonical key is
     * {@code normalize(milestoneName) + '|' + order + '|' + type + '|' + normalize(title)},
     * where normalisation trims, collapses whitespace, and lower-cases using
     * {@link Locale#ROOT}. The key is hashed over UTF-8 bytes.</p>
     *
     * @param milestoneName the originating milestone name (must not be null)
     * @param title         the task title (must not be null)
     * @param type          the task classification (must not be null)
     * @param order         the composite execution order
     * @return the lower-case SHA-256 hex digest (never null, 64 characters)
     * @throws NullPointerException if any reference argument is null
     */
    public static String computeTaskId(
            String milestoneName,
            String title,
            TaskType type,
            int order) {
        Objects.requireNonNull(milestoneName, "milestoneName must not be null");
        Objects.requireNonNull(title, "title must not be null");
        Objects.requireNonNull(type, "type must not be null");
        return sha256Hex(
                normalize(milestoneName) + '|'
                        + order + '|'
                        + type.name() + '|'
                        + normalize(title));
    }

    /**
     * Normalises a descriptor for deterministic hashing: trim, collapse all
     * whitespace runs to a single space, and lower-case using
     * {@link Locale#ROOT}.
     *
     * @param value the raw descriptor (must not be null)
     * @return the normalised descriptor (never null)
     * @throws NullPointerException if {@code value} is null
     */
    public static String normalize(String value) {
        return Objects.requireNonNull(value, "value must not be null")
                .trim()
                                .replaceAll("\\s+", " ")
                .toLowerCase(Locale.ROOT);
    }

    /**
     * Hashes the given input with SHA-256 and returns the lower-case hex
     * digest.
     *
     * @param input the input text (must not be null)
     * @return the lower-case SHA-256 hex digest (never null, 64 characters)
     * @throws NullPointerException  if {@code input} is null
     * @throws IllegalStateException if SHA-256 is unavailable on this JVM
     */
    public static String sha256Hex(String input) {
        Objects.requireNonNull(input, "input must not be null");
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(TASK_ID_LENGTH);
            for (byte b : digest) {
                hex.append(HEX_DIGITS[(b >> 4) & 0x0f])
                        .append(HEX_DIGITS[b & 0x0f]);
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available on this JVM", e);
        }
    }

    /**
     * Reports whether the candidate is a valid {@link #taskId()} value.
     *
     * @param candidate the candidate identifier (may be null)
     * @return {@code true} when the candidate is a 64-character lower-case
     *         SHA-256 hex digest
     */
    public static boolean isValidTaskId(String candidate) {
        if (candidate == null || candidate.length() != TASK_ID_LENGTH) {
            return false;
        }
        for (int i = 0; i < candidate.length(); i++) {
            char c = candidate.charAt(i);
            boolean hex = (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f');
            if (!hex) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the canonical, stable ordering comparator for planning tasks:
     * composite order first, then title, then identifier as the final total
     * tie-break.
     *
     * <p>Title comparison uses natural {@link String} ordering (UTF-16 code
     * units) so the result is identical on every locale and platform.</p>
     *
     * @return the canonical comparator (never null)
     */
    public static Comparator<PlanningTask> canonicalOrder() {
        return Comparator
                .comparingInt(PlanningTask::order)
                .thenComparing(PlanningTask::title)
                .thenComparing(PlanningTask::taskId);
    }

    /**
     * Returns the first twelve characters of the identifier, for stable
     * human-readable diagnostics.
     *
     * @return the short identifier (never null)
     */
    public String shortId() {
        return taskId.substring(0, 12);
    }

    @Override
    public String toString() {
        return "PlanningTask{order=" + order
                + ", type=" + type
                + ", id=" + shortId()
                + ", title='" + title + "'}";
    }
}