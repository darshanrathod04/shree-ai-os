package com.shreeai.os.platform.validation;

/**
 * Descriptor for a validation rule.
 *
 * <p>Contains metadata about a validation rule for diagnostics
 * and pipeline reporting.</p>
 *
 * @author Shree AI OS Team
 * @version 1.0
 * @since Sprint 5.3
 */
public final class RuleDescriptor {

    private final String ruleName;
    private final int priority;
    private final String beanName;
    private final boolean enabled;
    private final String description;

    /**
     * Create a rule descriptor.
     *
     * @param ruleName the rule name
     * @param priority the execution priority (lower = earlier)
     * @param beanName the Spring bean name
     * @param enabled whether the rule is enabled
     * @param description human-readable description
     */
    public RuleDescriptor(
            String ruleName,
            int priority,
            String beanName,
            boolean enabled,
            String description
    ) {
        this.ruleName = ruleName;
        this.priority = priority;
        this.beanName = beanName;
        this.enabled = enabled;
        this.description = description;
    }

    /**
     * Get the rule name.
     */
    public String getRuleName() {
        return ruleName;
    }

    /**
     * Get the execution priority.
     */
    public int getPriority() {
        return priority;
    }

    /**
     * Get the Spring bean name.
     */
    public String getBeanName() {
        return beanName;
    }

    /**
     * Check if the rule is enabled.
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Get the description.
     */
    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "RuleDescriptor{" +
                "ruleName='" + ruleName + '\'' +
                ", priority=" + priority +
                ", beanName='" + beanName + '\'' +
                ", enabled=" + enabled +
                ", description='" + description + '\'' +
                '}';
    }
}