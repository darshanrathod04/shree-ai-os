package platform.kernels.multiagent.model;

import java.util.Objects;

/**
 * <b>AgentId</b>
 *
 * <p>Canonical identity value object for agents in the Multi-Agent Kernel.</p>
 *
 * <p><b>Ownership:</b> Multi-Agent Kernel — Domain Model</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> MAGENT-102, EIO-ARCH-001</p>
 *
 * <p>AgentId provides globally comparable identity semantics for agents.
 * It is immutable and implements value semantics.</p>
 *
 * @param value the agent identifier value (must not be {@code null} or blank)
 *
 * @since 1.0
 */
public final class AgentId {
    private final String value;

    /**
     * Creates a new AgentId with the specified value.
     *
     * @param value the agent identifier value (must not be {@code null} or blank)
     * @throws NullPointerException     if value is {@code null}
     * @throws IllegalArgumentException if value is blank
     * @since 1.0
     */
    public AgentId(String value) {
        this.value = Objects.requireNonNull(value, "AgentId value must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("AgentId value must not be blank");
        }
    }

    /**
     * Returns the agent identifier value.
     *
     * @return the agent identifier value
     * @since 1.0
     */
    public String value() {
        return value;
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     * Two AgentIds are equal if they have the same value.
     *
     * @param obj the reference object with which to compare
     * @return {@code true} if this object is equal to the obj argument
     * @since 1.0
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        AgentId agentId = (AgentId) obj;
        return value.equals(agentId.value);
    }

    /**
     * Returns a hash code value for the AgentId.
     *
     * @return a hash code value
     * @since 1.0
     */
    @Override
    public int hashCode() {
        return value.hashCode();
    }

    /**
     * Returns a string representation of the AgentId.
     *
     * @return a string representation
     * @since 1.0
     */
    @Override
    public String toString() {
        return "AgentId{" +
                "value='" + value + '\'' +
                '}';
    }
}