package com.shreeai.os.platform.kernels.identity.api;

/**
 * <b>IdentityResult</b>
 *
 * <p>Result object for Identity lookup operations.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Encapsulates the result of an Identity lookup operation.</li>
 *   <li>Provides a consistent pattern for success/failure indication.</li>
 *   <li>Enables type-safe handling of Identity retrieval results.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Identity Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Invariant:</b> This is a pure data contract with no business logic.</p>
 *
 * <p><b>Constitutional Authority:</b> ADD-104</p>
 *
 * @param success whether the operation succeeded
 * @param identity the Identity if found, null otherwise
 * @param failureMessage the reason for failure if not successful
 */
public record IdentityResult(
    boolean success,
    Identity identity,
    String failureMessage
) {
    /**
     * Creates a successful result with an Identity.
     *
     * @param identity the found Identity
     * @return a successful IdentityResult
     */
    public static IdentityResult success(Identity identity) {
        return new IdentityResult(true, identity, null);
    }

    /**
     * Creates a failed result with a failure message.
     *
     * @param failureMessage the reason for failure
     * @return a failed IdentityResult
     */
    public static IdentityResult failure(String failureMessage) {
        return new IdentityResult(false, null, failureMessage);
    }
}