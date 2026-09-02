package com.shreeai.os.platform.kernels.execution.model;

public interface Capability {
    String getName();
    CapabilityHealthStatus getHealthStatus();
    CapabilityExecutionType getExecutionType();
    boolean isAvailable();
}
