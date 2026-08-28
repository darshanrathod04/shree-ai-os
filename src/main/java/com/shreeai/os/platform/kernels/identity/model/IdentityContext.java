package com.shreeai.os.platform.kernels.identity.model;

import com.shreeai.os.platform.kernels.identity.api.IdentityType;

import java.time.Instant;
import java.util.Objects;

/**
 * Immutable runtime identity context.
 *
 * This becomes the canonical identity object flowing through every kernel.
 */
public final class IdentityContext {

    private final IdentityId identityId;
    private final IdentityType identityType;
    private final String sessionId;
    private final String applicationId;
    private final String workspaceId;
    private final boolean authenticated;
    private final Instant resolvedAt;

    private IdentityContext(Builder builder) {
        this.identityId = Objects.requireNonNull(builder.identityId);
        this.identityType = Objects.requireNonNull(builder.identityType);
        this.sessionId = builder.sessionId;
        this.applicationId = builder.applicationId;
        this.workspaceId = builder.workspaceId;
        this.authenticated = builder.authenticated;
        this.resolvedAt = Objects.requireNonNull(builder.resolvedAt);
    }

    public static Builder builder() {
        return new Builder();
    }

    public IdentityId identityId() {
        return identityId;
    }

    public IdentityType identityType() {
        return identityType;
    }

    public String sessionId() {
        return sessionId;
    }

    public String applicationId() {
        return applicationId;
    }

    public String workspaceId() {
        return workspaceId;
    }

    public boolean authenticated() {
        return authenticated;
    }

    public Instant resolvedAt() {
        return resolvedAt;
    }

    public static final class Builder {

        private IdentityId identityId;
        private IdentityType identityType = IdentityType.AGENT;
        private String sessionId;
        private String applicationId = "SHREE_RUNTIME";
        private String workspaceId = "DEFAULT";
        private boolean authenticated = true;
        private Instant resolvedAt = Instant.now();

        public Builder identityId(IdentityId value) {
            this.identityId = value;
            return this;
        }

        public Builder identityType(IdentityType value) {
            this.identityType = value;
            return this;
        }

        public Builder sessionId(String value) {
            this.sessionId = value;
            return this;
        }

        public Builder applicationId(String value) {
            this.applicationId = value;
            return this;
        }

        public Builder workspaceId(String value) {
            this.workspaceId = value;
            return this;
        }

        public Builder authenticated(boolean value) {
            this.authenticated = value;
            return this;
        }

        public Builder resolvedAt(Instant value) {
            this.resolvedAt = value;
            return this;
        }

        public IdentityContext build() {
            return new IdentityContext(this);
        }
    }
}