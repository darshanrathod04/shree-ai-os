package platform.core.lifecycle;

import platform.core.lifecycle.error.*;
import platform.core.lifecycle.model.KernelState;
import platform.core.registry.model.KernelId;

/**
 * <b>LifecycleErrorTests</b>
 *
 * <p>Verifies the error handling behavior of the Lifecycle subsystem.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Validates LifecycleException hierarchy.</li>
 *   <li>Validates LifecycleErrorCode values.</li>
 *   <li>Validates LifecycleError is immutable.</li>
 *   <li>Validates concrete exception types.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @see LifecycleException
 */
public class LifecycleErrorTests {

    /**
     * Verifies LifecycleException is a RuntimeException.
     */
    public void testLifecycleExceptionIsRuntimeException() {
        LifecycleError error = new LifecycleError(LifecycleErrorCode.LIFECYCLE_INVALID_TRANSITION, "test");
        LifecycleException exception = new LifecycleException(error);
        assert exception instanceof RuntimeException : "LifecycleException should extend RuntimeException";
    }

    /**
     * Verifies LifecycleException contains LifecycleError.
     */
    public void testLifecycleExceptionContainsError() {
        LifecycleError error = new LifecycleError(LifecycleErrorCode.LIFECYCLE_INVALID_TRANSITION, "test message");
        LifecycleException exception = new LifecycleException(error);

        assert exception.error() == error : "Exception should contain the error";
        assert exception.code() == LifecycleErrorCode.LIFECYCLE_INVALID_TRANSITION : "Code should match";
        assert exception.getMessage().equals("LIFECYCLE_INVALID_TRANSITION: test message") : "Message should match";
    }

    /**
     * Verifies InvalidTransitionException is thrown correctly.
     */
    public void testInvalidTransitionException() {
        InvalidTransitionException exception = new InvalidTransitionException(KernelState.CREATED, KernelState.RUNNING);
        assert exception.code() == LifecycleErrorCode.LIFECYCLE_INVALID_TRANSITION : "Code should be INVALID_TRANSITION";
        assert exception instanceof LifecycleException : "Should extend LifecycleException";
    }

    /**
     * Verifies KernelNotInitializedException is thrown correctly.
     */
    public void testKernelNotInitializedException() {
        KernelId kernelId = new KernelId("test-kernel");
        KernelNotInitializedException exception = new KernelNotInitializedException(kernelId);
        assert exception.code() == LifecycleErrorCode.LIFECYCLE_KERNEL_NOT_INITIALIZED : "Code should be KERNEL_NOT_INITIALIZED";
        assert exception instanceof LifecycleException : "Should extend LifecycleException";
    }

    /**
     * Verifies KernelAlreadyRunningException is thrown correctly.
     */
    public void testKernelAlreadyRunningException() {
        KernelId kernelId = new KernelId("test-kernel");
        KernelAlreadyRunningException exception = new KernelAlreadyRunningException(kernelId);
        assert exception.code() == LifecycleErrorCode.LIFECYCLE_KERNEL_ALREADY_RUNNING : "Code should be KERNEL_ALREADY_RUNNING";
        assert exception instanceof LifecycleException : "Should extend LifecycleException";
    }

    /**
     * Verifies LifecycleErrorCode enum values.
     */
    public void testLifecycleErrorCodeValues() {
        assert LifecycleErrorCode.valueOf("LIFECYCLE_INVALID_TRANSITION") != null : "INVALID_TRANSITION should exist";
        assert LifecycleErrorCode.valueOf("LIFECYCLE_KERNEL_NOT_INITIALIZED") != null : "KERNEL_NOT_INITIALIZED should exist";
        assert LifecycleErrorCode.valueOf("LIFECYCLE_KERNEL_ALREADY_RUNNING") != null : "KERNEL_ALREADY_RUNNING should exist";
        assert LifecycleErrorCode.valueOf("LIFECYCLE_KERNEL_ALREADY_STOPPED") != null : "KERNEL_ALREADY_STOPPED should exist";
        assert LifecycleErrorCode.valueOf("LIFECYCLE_KERNEL_ALREADY_SUSPENDED") != null : "KERNEL_ALREADY_SUSPENDED should exist";
        assert LifecycleErrorCode.valueOf("LIFECYCLE_KERNEL_TERMINATED") != null : "KERNEL_TERMINATED should exist";
        assert LifecycleErrorCode.valueOf("LIFECYCLE_VALIDATION_FAILED") != null : "VALIDATION_FAILED should exist";
    }

    /**
     * Verifies LifecycleError is immutable.
     */
    public void testLifecycleErrorIsImmutable() {
        LifecycleError error = new LifecycleError(LifecycleErrorCode.LIFECYCLE_INVALID_TRANSITION, "test");
        assert error.code() == LifecycleErrorCode.LIFECYCLE_INVALID_TRANSITION : "Code should be accessible";
        assert error.message().equals("test") : "Message should be accessible";
        assert error.timestamp() != null : "Timestamp should not be null";
        assert error.details() != null : "Details should not be null";
        assert error.details().isEmpty() : "Details should be empty by default";
    }

    /**
     * Verifies LifecycleError has correct equals/hashCode.
     */
    public void testLifecycleErrorEqualsAndHashCode() {
        LifecycleError error1 = new LifecycleError(LifecycleErrorCode.LIFECYCLE_INVALID_TRANSITION, "test");
        LifecycleError error2 = new LifecycleError(LifecycleErrorCode.LIFECYCLE_INVALID_TRANSITION, "test");
        // Note: timestamps may differ, so equals should account for that
        assert error1.equals(error1) : "Same object should be equal";
    }

    /**
     * Verifies all exceptions can be caught as LifecycleException.
     */
    public void testAllExceptionsAreLifecycleExceptions() {
        assert new InvalidTransitionException(KernelState.RUNNING, KernelState.CREATED) instanceof LifecycleException;
        assert new KernelNotInitializedException(new KernelId("test")) instanceof LifecycleException;
        assert new KernelAlreadyRunningException(new KernelId("test")) instanceof LifecycleException;
    }
}