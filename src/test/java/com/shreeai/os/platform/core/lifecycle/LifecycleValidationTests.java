package com.shreeai.os.platform.core.lifecycle;

import com.shreeai.os.platform.core.lifecycle.model.KernelHealth;
import com.shreeai.os.platform.core.lifecycle.model.KernelState;
import com.shreeai.os.platform.core.lifecycle.validator.LifecycleValidator;
import com.shreeai.os.platform.core.registry.model.KernelId;
import com.shreeai.os.platform.core.registry.validator.ValidationResult;

import java.time.Instant;
import java.util.Map;

/**
 * <b>LifecycleValidationTests</b>
 *
 * <p>Verifies the validation behavior of the {@link LifecycleValidator}.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Validates that all valid transitions are accepted.</li>
 *   <li>Validates that all invalid transitions are rejected.</li>
 *   <li>Validates that validator is stateless and deterministic.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @see LifecycleValidator
 */
public class LifecycleValidationTests {

    private final LifecycleValidator validator = new LifecycleValidator();

    // ===== Allowed Transitions =====

    /**
     * Verifies CREATED → INITIALIZED is valid.
     */
    public void testCreatedToInitializedIsValid() {
        ValidationResult result = validator.validateTransition(KernelState.CREATED, KernelState.INITIALIZED);
        assert result.isValid() : "CREATED -> INITIALIZED should be valid";
    }

    /**
     * Verifies INITIALIZED → RUNNING is valid.
     */
    public void testInitializedToRunningIsValid() {
        ValidationResult result = validator.validateTransition(KernelState.INITIALIZED, KernelState.RUNNING);
        assert result.isValid() : "INITIALIZED -> RUNNING should be valid";
    }

    /**
     * Verifies RUNNING → SUSPENDED is valid.
     */
    public void testRunningToSuspendedIsValid() {
        ValidationResult result = validator.validateTransition(KernelState.RUNNING, KernelState.SUSPENDED);
        assert result.isValid() : "RUNNING -> SUSPENDED should be valid";
    }

    /**
     * Verifies RUNNING → STOPPED is valid.
     */
    public void testRunningToStoppedIsValid() {
        ValidationResult result = validator.validateTransition(KernelState.RUNNING, KernelState.STOPPED);
        assert result.isValid() : "RUNNING -> STOPPED should be valid";
    }

    /**
     * Verifies RUNNING → FAILED is valid.
     */
    public void testRunningToFailedIsValid() {
        ValidationResult result = validator.validateTransition(KernelState.RUNNING, KernelState.FAILED);
        assert result.isValid() : "RUNNING -> FAILED should be valid";
    }

    /**
     * Verifies SUSPENDED → RUNNING is valid.
     */
    public void testSuspendedToRunningIsValid() {
        ValidationResult result = validator.validateTransition(KernelState.SUSPENDED, KernelState.RUNNING);
        assert result.isValid() : "SUSPENDED -> RUNNING should be valid";
    }

    /**
     * Verifies FAILED → TERMINATED is valid.
     */
    public void testFailedToTerminatedIsValid() {
        ValidationResult result = validator.validateTransition(KernelState.FAILED, KernelState.TERMINATED);
        assert result.isValid() : "FAILED -> TERMINATED should be valid";
    }

    /**
     * Verifies STOPPED → TERMINATED is valid.
     */
    public void testStoppedToTerminatedIsValid() {
        ValidationResult result = validator.validateTransition(KernelState.STOPPED, KernelState.TERMINATED);
        assert result.isValid() : "STOPPED -> TERMINATED should be valid";
    }

    // ===== Rejected Transitions =====

    /**
     * Verifies CREATED → RUNNING is rejected.
     */
    public void testCreatedToRunningIsRejected() {
        ValidationResult result = validator.validateTransition(KernelState.CREATED, KernelState.RUNNING);
        assert !result.isValid() : "CREATED -> RUNNING should be rejected";
    }

    /**
     * Verifies INITIALIZED → TERMINATED is rejected.
     */
    public void testInitializedToTerminatedIsRejected() {
        ValidationResult result = validator.validateTransition(KernelState.INITIALIZED, KernelState.TERMINATED);
        assert !result.isValid() : "INITIALIZED -> TERMINATED should be rejected";
    }

    /**
     * Verifies FAILED → RUNNING is rejected.
     */
    public void testFailedToRunningIsRejected() {
        ValidationResult result = validator.validateTransition(KernelState.FAILED, KernelState.RUNNING);
        assert !result.isValid() : "FAILED -> RUNNING should be rejected";
    }

    /**
     * Verifies TERMINATED → ANY is rejected.
     */
    public void testTerminatedToAnyIsRejected() {
        for (KernelState state : KernelState.values()) {
            ValidationResult result = validator.validateTransition(KernelState.TERMINATED, state);
            assert !result.isValid() : "TERMINATED -> " + state + " should be rejected";
        }
    }

    /**
     * Verifies STOPPED → RUNNING is rejected.
     */
    public void testStoppedToRunningIsRejected() {
        ValidationResult result = validator.validateTransition(KernelState.STOPPED, KernelState.RUNNING);
        assert !result.isValid() : "STOPPED -> RUNNING should be rejected";
    }

    // ===== Deterministic Behavior =====

    /**
     * Verifies validator is deterministic (same inputs, same results).
     */
    public void testValidatorIsDeterministic() {
        ValidationResult first = validator.validateTransition(KernelState.CREATED, KernelState.INITIALIZED);
        ValidationResult second = validator.validateTransition(KernelState.CREATED, KernelState.INITIALIZED);
        assert first.isValid() == second.isValid() : "Validator should be deterministic";
    }

    // ===== KernelId Validation =====

    /**
     * Verifies validateKernelId accepts valid KernelId.
     */
    public void testValidateKernelIdAcceptsValid() {
        KernelId kernelId = new KernelId("valid-kernel");
        ValidationResult result = validator.validateKernelId(kernelId);
        assert result.isValid() : "Valid KernelId should pass validation";
    }

    // ===== KernelHealth Validation =====

    /**
     * Verifies validateHealth accepts valid health.
     */
    public void testValidateHealthAcceptsValid() {
        KernelHealth health = new KernelHealth("HEALTHY", "All systems operational", Instant.now());
        ValidationResult result = validator.validateHealth(health);
        assert result.isValid() : "Valid KernelHealth should pass validation";
    }

    /**
     * Verifies validateHealth rejects null status.
     */
    public void testValidateHealthRejectsNullStatus() {
        KernelHealth health = new KernelHealth(null, "message", Instant.now(), Map.of());
        ValidationResult result = validator.validateHealth(health);
        assert !result.isValid() : "KernelHealth with null status should fail validation";
    }
}