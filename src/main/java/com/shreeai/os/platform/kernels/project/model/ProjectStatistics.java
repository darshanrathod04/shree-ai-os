package com.shreeai.os.platform.kernels.project.model;

/**
 * <b>ProjectStatistics</b> — immutable counters for a project.
 */
public final class ProjectStatistics {

    private final int classCount;
    private final int interfaceCount;
    private final int enumCount;
    private final int recordCount;
    private final int controllerCount;
    private final int serviceCount;
    private final int repositoryCount;
    private final int entityCount;
    private final int configurationCount;
    private final int beanCount;
    private final int endpointCount;
    private final int methodCount;

    private ProjectStatistics(Builder b) {
        this.classCount = b.classCount;
        this.interfaceCount = b.interfaceCount;
        this.enumCount = b.enumCount;
        this.recordCount = b.recordCount;
        this.controllerCount = b.controllerCount;
        this.serviceCount = b.serviceCount;
        this.repositoryCount = b.repositoryCount;
        this.entityCount = b.entityCount;
        this.configurationCount = b.configurationCount;
        this.beanCount = b.beanCount;
        this.endpointCount = b.endpointCount;
        this.methodCount = b.methodCount;
    }

    public int classCount() { return classCount; }
    public int interfaceCount() { return interfaceCount; }
    public int enumCount() { return enumCount; }
    public int recordCount() { return recordCount; }
    public int controllerCount() { return controllerCount; }
    public int serviceCount() { return serviceCount; }
    public int repositoryCount() { return repositoryCount; }
    public int entityCount() { return entityCount; }
    public int configurationCount() { return configurationCount; }
    public int beanCount() { return beanCount; }
    public int endpointCount() { return endpointCount; }
    public int methodCount() { return methodCount; }

    public static ProjectStatistics empty() { return new Builder().build(); }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private int classCount;
        private int interfaceCount;
        private int enumCount;
        private int recordCount;
        private int controllerCount;
        private int serviceCount;
        private int repositoryCount;
        private int entityCount;
        private int configurationCount;
        private int beanCount;
        private int endpointCount;
        private int methodCount;

        public Builder classCount(int v) { this.classCount = v; return this; }
        public Builder interfaceCount(int v) { this.interfaceCount = v; return this; }
        public Builder enumCount(int v) { this.enumCount = v; return this; }
        public Builder recordCount(int v) { this.recordCount = v; return this; }
        public Builder controllerCount(int v) { this.controllerCount = v; return this; }
        public Builder serviceCount(int v) { this.serviceCount = v; return this; }
        public Builder repositoryCount(int v) { this.repositoryCount = v; return this; }
        public Builder entityCount(int v) { this.entityCount = v; return this; }
        public Builder configurationCount(int v) { this.configurationCount = v; return this; }
        public Builder beanCount(int v) { this.beanCount = v; return this; }
        public Builder endpointCount(int v) { this.endpointCount = v; return this; }
        public Builder methodCount(int v) { this.methodCount = v; return this; }

        public ProjectStatistics build() { return new ProjectStatistics(this); }
    }
}
