package com.shreeai.os.platform.kernels.reasoning.model;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * <b>UncertaintyGraph</b>
 *
 * <p>The canonical uncertainty artifact of the Reasoning Kernel: certainty
 * nodes, uncertainty issues and an overall certainty score in one immutable,
 * stably ordered structure. It contains no decisions and no planning - it
 * only evaluates what is genuinely certain, uncertain or unknown.</p>
 *
 * <p><b>Canonical ordering:</b> nodes by node id, issues by type, node id then
 * explanation - identical inputs always produce structurally equal graphs.</p>
 *
 * <p><b>Ownership:</b> Reasoning Kernel - R5 Uncertainty Modeling</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @param nodes            the ordered immutable uncertainty node list (never
 *                         null)
 * @param issues           the ordered immutable uncertainty issue list (never
 *                         null)
 * @param overallCertainty the locked overall certainty within {@code [0.0,
 *                         1.0]}
 */
public record UncertaintyGraph(
        List<UncertaintyNode> nodes,
        List<UncertaintyIssue> issues,
        double overallCertainty
) {

    /**
     * Compact constructor that validates, canonically orders and defensively
     * copies all three fields.
     *
     * @throws NullPointerException     if any list is null
     * @throws IllegalArgumentException if overallCertainty is outside
     *                                  {@code [0.0, 1.0]}
     */
    public UncertaintyGraph {
        Objects.requireNonNull(nodes, "nodes must not be null");
        Objects.requireNonNull(issues, "issues must not be null");
        if (overallCertainty < 0.0 || overallCertainty > 1.0) {
            throw new IllegalArgumentException(
                    "overallCertainty must be within [0.0, 1.0]: " + overallCertainty);
        }
        nodes = nodes.stream()
                .sorted(Comparator.comparing(UncertaintyNode::nodeId))
                .toList();
        issues = issues.stream()
                .sorted(Comparator.comparing((UncertaintyIssue i) -> i.type().name())
                        .thenComparing(UncertaintyIssue::nodeId)
                        .thenComparing(UncertaintyIssue::explanation))
                .toList();
    }

    /**
     * Returns the number of certainty nodes in the graph.
     *
     * @return the node count (never negative)
     */
    public int nodeCount() {
        return nodes.size();
    }

    /**
     * Returns the number of uncertainty issues in the graph.
     *
     * @return the issue count (never negative)
     */
    public int issueCount() {
        return issues.size();
    }

    @Override
    public String toString() {
        return String.format("UncertaintyGraph{nodes=%d, issues=%d, overallCertainty=%.4f}",
                nodes.size(), issues.size(), overallCertainty);
    }
}
