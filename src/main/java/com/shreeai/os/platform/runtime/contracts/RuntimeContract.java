package com.shreeai.os.platform.runtime.contracts;

/**
 * <b>RuntimeContract</b>
 *
 * <p>Defines the contract that governs all execution within a Runtime instance.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Specifies the capabilities and constraints of the Runtime.</li>
 *   <li>Enables contract enforcement during execution request validation.</li>
 *   <li>Provides a stable contract that kernels can depend on.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Runtime Kernel</p>
 * <p><b>Invariant:</b> A RuntimeContract must always be in a valid, internally consistent state.</p>
 */
public final class RuntimeContract {

    private final String contractVersion;
    private final boolean supportsSessions;
    private final boolean supportsPipelines;
    private final int maxPipelineStageDepth;

    private RuntimeContract(Builder builder) {
        this.contractVersion = builder.contractVersion;
        this.supportsSessions = builder.supportsSessions;
        this.supportsPipelines = builder.supportsPipelines;
        this.maxPipelineStageDepth = builder.maxPipelineStageDepth;
    }

    /**
     * Returns the version identifier of this contract.
     *
     * @return the contract version
     */
    public String contractVersion() {
        return contractVersion;
    }

    /**
     * Returns whether this Runtime supports session-based execution.
     *
     * @return true if session support is enabled
     */
    public boolean supportsSessions() {
        return supportsSessions;
    }

    /**
     * Returns whether this Runtime supports pipeline-based execution.
     *
     * @return true if pipeline support is enabled
     */
    public boolean supportsPipelines() {
        return supportsPipelines;
    }

    /**
     * Returns the maximum allowed depth for pipeline stage chains.
     *
     * @return max pipeline stage depth
     */
    public int maxPipelineStageDepth() {
        return maxPipelineStageDepth;
    }

    /**
     * Creates a new builder for RuntimeContract.
     *
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Fluent builder for {@link RuntimeContract}.
     */
    public static final class Builder {

        private String contractVersion = "1.0.0";
        private boolean supportsSessions = true;
        private boolean supportsPipelines = true;
        private int maxPipelineStageDepth = 10;

        private Builder() {
        }

        /**
         * Sets the contract version.
         *
         * @param contractVersion the version string
         * @return this builder
         */
        public Builder contractVersion(String contractVersion) {
            this.contractVersion = contractVersion;
            return this;
        }

        /**
         * Enables or disables session support.
         *
         * @param supportsSessions whether sessions are supported
         * @return this builder
         */
        public Builder supportsSessions(boolean supportsSessions) {
            this.supportsSessions = supportsSessions;
            return this;
        }

        /**
         * Enables or disables pipeline support.
         *
         * @param supportsPipelines whether pipelines are supported
         * @return this builder
         */
        public Builder supportsPipelines(boolean supportsPipelines) {
            this.supportsPipelines = supportsPipelines;
            return this;
        }

        /**
         * Sets the maximum pipeline stage depth.
         *
         * @param maxPipelineStageDepth max depth for pipeline stages
         * @return this builder
         */
        public Builder maxPipelineStageDepth(int maxPipelineStageDepth) {
            this.maxPipelineStageDepth = maxPipelineStageDepth;
            return this;
        }

        /**
         * Builds a new RuntimeContract.
         *
         * @return a new contract instance
         */
        public RuntimeContract build() {
            return new RuntimeContract(this);
        }
    }
}