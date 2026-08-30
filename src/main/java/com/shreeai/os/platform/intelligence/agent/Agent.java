package com.shreeai.os.platform.intelligence.agent;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * <b>Agent</b>
 *
 * <p>Immutable model of a routable agent within the platform. An agent has a
 * stable id, a human-readable name, an optional description, a set of
 * declared {@link AgentCapability}s, and a lifecycle {@link AgentStatus}.</p>
 *
 * <p><b>Ownership:</b> Intelligence — Agent Registry</p>
 * <p><b>Version:</b> 3.0</p>
 *
 * @since 3.0
 */
public final class Agent {

    private final String id;
    private final String name;
    private final String description;
    private final Set<AgentCapability> capabilities;
    private final AgentStatus status;
    private final Instant registeredAt;

    private Agent(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.description = builder.description;
        this.capabilities = Collections.unmodifiableSet(
                new LinkedHashSet<>(builder.capabilities));
        this.status = builder.status;
        this.registeredAt = builder.registeredAt;
    }

    /** @return the stable agent id (never null) */
    public String id() {
        return id;
    }

    /** @return the human-readable name (never null) */
    public String name() {
        return name;
    }

    /** @return the agent description (never null) */
    public String description() {
        return description;
    }

    /** @return the declared capabilities (never null) */
    public Set<AgentCapability> capabilities() {
        return capabilities;
    }

    /** @return the current lifecycle status */
    public AgentStatus status() {
        return status;
    }

    /** @return when the agent was registered */
    public Instant registeredAt() {
        return registeredAt;
    }

    /**
     * @return whether this agent declares the given capability
     */
    public boolean hasCapability(AgentCapability capability) {
        return capability != null && capabilities.contains(capability);
    }

    /**
     * @return whether this agent is eligible for dispatch (ACTIVE)
     */
    public boolean isDispatchable() {
        return status == AgentStatus.ACTIVE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Agent agent)) {
            return false;
        }
        return id.equals(agent.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Agent{id='" + id + "', name='" + name + "', status=" + status + '}';
    }

    /** Creates a new builder. */
    public static Builder builder() {
        return new Builder();
    }

    /** Creates an agent with the given id, name, and capabilities. */
    public static Agent of(String id, String name, AgentCapability... capabilities) {
        return builder()
                .id(id)
                .name(name)
                .capabilities(Set.copyOf(new LinkedHashSet<>(
                        java.util.Arrays.asList(capabilities == null
                                ? new AgentCapability[0]
                                : capabilities))))
                .build();
    }

    /** Fluent builder for {@link Agent}. */
    public static final class Builder {

        private String id;
        private String name;
        private String description = "";
        private Set<AgentCapability> capabilities = new LinkedHashSet<>();
        private AgentStatus status = AgentStatus.ACTIVE;
        private Instant registeredAt = Instant.now();

        private Builder() {
        }

        public Builder id(String id) {
            this.id = Objects.requireNonNull(id, "id must not be null");
            return this;
        }

        public Builder name(String name) {
            this.name = Objects.requireNonNull(name, "name must not be null");
            return this;
        }

        public Builder description(String description) {
            this.description = description == null ? "" : description;
            return this;
        }

        public Builder capabilities(Set<AgentCapability> capabilities) {
            this.capabilities = capabilities == null
                    ? new LinkedHashSet<>()
                    : new LinkedHashSet<>(capabilities);
            return this;
        }

        public Builder status(AgentStatus status) {
            this.status = Objects.requireNonNull(status, "status must not be null");
            return this;
        }

        public Builder registeredAt(Instant registeredAt) {
            this.registeredAt = Objects.requireNonNull(registeredAt, "registeredAt must not be null");
            return this;
        }

        public Agent build() {
            if (id == null) {
                throw new IllegalArgumentException("id must not be null");
            }
            if (name == null) {
                throw new IllegalArgumentException("name must not be null");
            }
            return new Agent(this);
        }
    }
}
