package platform.kernels.identity.api;

/**
 * <b>IdentityCommands</b>
 *
 * <p>Defines operations that modify Identity state within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Defines the contract for all Identity state-modifying operations.</li>
 *   <li>Commands request changes — they do not execute them.</li>
 *   <li>Enforces separation between mutation requests and read operations.</li>
 *   <li>Provides a stable contract for other kernels to request Identity changes.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Identity Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Invariant:</b> Commands never modify state directly. They define
 * the contract for requesting changes. Implementations handle execution.</p>
 *
 * <p><b>Constitutional Authority:</b> ADD-104, ADD-105, ADD-106</p>
 *
 * @see platform.kernels.identity.api.IdentityKernel
 * @see platform.kernels.identity.api.IdentityQueries
 */
public interface IdentityCommands {

    /**
     * Creates a new Identity within the platform.
     *
     * <p>This command requests the creation of a new Identity with the
     * specified attributes. The Identity becomes part of the platform's
     * continuity model upon successful creation.</p>
     *
     * <p>The created Identity SHALL be assigned a unique identifier that
     * remains stable for the lifetime of the Identity.</p>
     *
     * @param request the identity creation request containing all required attributes
     * @return the unique identifier assigned to the newly created Identity
     * @throws IllegalArgumentException if {@code request} is {@code null}
     */
    String createIdentity(CreateIdentityRequest request);

    /**
     * Updates the profile of an existing Identity.
     *
     * <p>This command requests an update to the profile attributes of an
     * existing Identity. Profile updates preserve the Identity's continuity
     * while allowing attribute modification.</p>
     *
     * <p>The Identity's timeline SHALL record this update event.</p>
     *
     * @param identityId the unique identifier of the Identity to update
     * @param request    the profile update request containing the new attributes
     * @return {@code true} if the update was accepted, {@code false} if the Identity was not found
     * @throws IllegalArgumentException if {@code identityId} or {@code request} is {@code null}
     */
    boolean updateProfile(String identityId, UpdateProfileRequest request);

    /**
     * Registers a relationship between two Identities.
     *
     * <p>This command requests the creation of a relationship between two
     * Identities. Relationships define how Identities are connected within
     * the platform's social and organizational model.</p>
     *
     * <p>Examples of relationships include: parent-child, ownership, membership,
     * collaboration, dependency, etc.</p>
     *
     * @param request the relationship registration request specifying source, target, and type
     * @return {@code true} if the relationship was registered, {@code false} if validation failed
     * @throws IllegalArgumentException if {@code request} is {@code null}
     */
    boolean registerRelationship(RegisterRelationshipRequest request);

    /**
     * Registers ownership of an asset by an Identity.
     *
     * <p>This command requests the registration of ownership, establishing
     * that a specific Identity owns a particular asset within the platform.</p>
     *
     * <p>Ownership is a first-class concept in the Identity Kernel and enables
     * access control, accountability, and continuity tracking.</p>
     *
     * @param request the ownership registration request specifying the Identity and asset
     * @return {@code true} if ownership was registered, {@code false} if validation failed
     * @throws IllegalArgumentException if {@code request} is {@code null}
     */
    boolean registerOwnership(RegisterOwnershipRequest request);
}