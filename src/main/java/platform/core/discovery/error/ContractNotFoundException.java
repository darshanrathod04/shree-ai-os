package platform.core.discovery.error;

import java.time.Instant;

/**
 * <b>ContractNotFoundException</b>
 *
 * <p>Thrown when a requested contract is not found in the platform.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Signals that a contract lookup returned no result.</li>
 *   <li>Extends {@link DiscoveryException} to maintain the single base exception hierarchy.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> KERNEL-006</p>
 *
 * @see DiscoveryException
 * @see DiscoveryErrorCode#DISCOVERY_CONTRACT_NOT_FOUND
 */
public class ContractNotFoundException extends DiscoveryException {

    /**
     * Constructs a new {@code ContractNotFoundException} with the given contract identifier.
     *
     * @param contractId the contract identifier that was not found (must not be null)
     * @throws NullPointerException if {@code contractId} is null
     */
    public ContractNotFoundException(String contractId) {
        this(contractId, (String) null);
    }

    /**
     * Constructs a new {@code ContractNotFoundException} with the given contract identifier and message.
     *
     * @param contractId the contract identifier that was not found (must not be null)
     * @param message    the detail message (may be null)
     * @throws NullPointerException if {@code contractId} is null
     */
    public ContractNotFoundException(String contractId, String message) {
        super(createError(contractId, message));
    }

    /**
     * Constructs a new {@code ContractNotFoundException} with the given contract identifier and cause.
     *
     * @param contractId the contract identifier that was not found (must not be null)
     * @param cause      the underlying cause (may be null)
     * @throws NullPointerException if {@code contractId} is null
     */
    public ContractNotFoundException(String contractId, Throwable cause) {
        super(createError(contractId, null), cause);
    }

    /**
     * Constructs a new {@code ContractNotFoundException} with the given contract identifier, message, and cause.
     *
     * @param contractId the contract identifier that was not found (must not be null)
     * @param message    the detail message (may be null)
     * @param cause      the underlying cause (may be null)
     * @throws NullPointerException if {@code contractId} is null
     */
    public ContractNotFoundException(String contractId, String message, Throwable cause) {
        super(createError(contractId, message), cause);
    }

    private static DiscoveryError createError(String contractId, String message) {
        String errorMessage = message != null ? message : "Contract '" + contractId + "' was not found";
        return new DiscoveryError(
                DiscoveryErrorCode.DISCOVERY_CONTRACT_NOT_FOUND,
                errorMessage,
                Instant.now()
        );
    }
}