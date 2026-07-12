package platform.validation;

import java.time.Instant;
import java.util.List;

/**
 * Pipeline report for diagnostics.
 *
 * <p>Contains information about the validation pipeline configuration
 * and current state for monitoring and troubleshooting.</p>
 *
 * @author Shree AI OS Team
 * @version 1.0
 * @since Sprint 5.3
 */
public final class ValidationPipelineReport {

    private final int ruleCount;
    private final List<RuleDescriptor> rules;
    private final Instant startupTimestamp;
    private final String pipelineVersion;
    private final String validationMode;

    /**
     * Create a pipeline report.
     *
     * @param ruleCount the number of rules in the pipeline
     * @param rules the list of rule descriptors
     * @param startupTimestamp when the pipeline was initialized
     * @param pipelineVersion the pipeline version
     * @param validationMode the validation mode (SHADOW, PRODUCTION, etc.)
     */
    public ValidationPipelineReport(
            int ruleCount,
            List<RuleDescriptor> rules,
            Instant startupTimestamp,
            String pipelineVersion,
            String validationMode
    ) {
        this.ruleCount = ruleCount;
        this.rules = rules;
        this.startupTimestamp = startupTimestamp;
        this.pipelineVersion = pipelineVersion;
        this.validationMode = validationMode;
    }

    /**
     * Get the rule count.
     */
    public int getRuleCount() {
        return ruleCount;
    }

    /**
     * Get the list of rule descriptors.
     */
    public List<RuleDescriptor> getRules() {
        return rules;
    }

    /**
     * Get the startup timestamp.
     */
    public Instant getStartupTimestamp() {
        return startupTimestamp;
    }

    /**
     * Get the pipeline version.
     */
    public String getPipelineVersion() {
        return pipelineVersion;
    }

    /**
     * Get the validation mode.
     */
    public String getValidationMode() {
        return validationMode;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ValidationPipelineReport{\n");
        sb.append("  ruleCount=").append(ruleCount).append("\n");
        sb.append("  pipelineVersion='").append(pipelineVersion).append("'\n");
        sb.append("  validationMode='").append(validationMode).append("'\n");
        sb.append("  startupTimestamp=").append(startupTimestamp).append("\n");
        sb.append("  rules=[\n");
        for (RuleDescriptor rule : rules) {
            sb.append("    ").append(rule).append("\n");
        }
        sb.append("  ]\n");
        sb.append("}");
        return sb.toString();
    }
}