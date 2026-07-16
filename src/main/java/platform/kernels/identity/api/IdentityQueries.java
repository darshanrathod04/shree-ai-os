package platform.kernels.identity.api;

import java.util.List;

/**
 * <b>IdentityQueries</b>
 *
 * <p>Defines read-only operations for retrieving Identity data within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Defines the contract for all Identity read-only operations.</li>
 *   <li>Queries retrieve data — they never modify state.</li>
 *   <li>Enforces strict separation between read and write operations.</li>
 *   <li>Provides stable contracts for other kernels to retrieve Identity information.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Identity Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Invariant:</b> Queries never modify state. They are pure read operations
 * that return data without side effects.</p>
 *
 * <p><b>Constitutional Authority:</b> ADD-104, ADD-105, ADD-106</p>
 *
 * @see platform.kernels.identity.api.IdentityKernel
 * @see platform.kernels.identity.api.IdentityCommands
 */
public interface IdentityQueries {

    /**
     * Finds an Identity by its unique identifier.
     *
     * <p>Returns the Identity's basic information if found, or an empty
     * result if no Identity exists with the given identifier.</p>
     *
     * <p>This is the primary method for verifying Identity existence and
     * retrieving basic Identity information.</p>
     *
     * @param identityId the unique identifier of the Identity to find
     * @return an {@link IdentityResult} containing the Identity if found,
     *         or an empty result if not found
     * @throws IllegalArgumentException if {@code identityId} is {@code null}
     */
    IdentityResult findIdentity(String identityId);

    /**
     * Retrieves the complete profile of an Identity.
     *
     * <p>Returns the full profile information for the specified Identity,
     * including all attributes, metadata, and configuration.</p>
     *
     * <p>The profile represents the current state of the Identity's attributes
     * and is distinct from the timeline, which tracks changes over time.</p>
     *
     * @param identityId the unique identifier of the Identity
     * @return an {@link IdentityProfile} containing the complete profile,
     *         or {@code null} if the Identity was not found
     * @throws IllegalArgumentException if {@code identityId} is {@code null}
     */
    IdentityProfile getProfile(String identityId);

    /**
     * Retrieves the timeline of an Identity.
     *
     * <p>Returns the chronological sequence of events that have occurred
     * for the specified Identity. The timeline provides a complete audit
     * trail of the Identity's history within the platform.</p>
     *
     * <p>Events are ordered chronologically from earliest to most recent.</p>
     *
     * @param identityId the unique identifier of the Identity
     * @return an {@link IdentityTimeline} containing the chronological event history,
     *         or {@code null} if the Identity was not found
     * @throws IllegalArgumentException if {@code identityId} is {@code null}
     */
    IdentityTimeline getTimeline(String identityId);

    /**
     * Retrieves all relationships for an Identity.
     *
     * <p>Returns the complete set of relationships in which the specified
     * Identity participates. This includes both incoming and outgoing
     * relationships.</p>
     *
     * <p>Relationships define how Identities are connected within the
     * platform's social and organizational model.</p>
     *
     * @param identityId the unique identifier of the Identity
     * @return a {@link List} of {@link IdentityRelationship} containing all relationships,
     *         or {@code null} if the Identity was not found
     * @throws IllegalArgumentException if {@code identityId} is {@code null}
     */
    List<IdentityRelationship> getRelationships(String identityId);

    /**
     * Retrieves all ownership records for an Identity.
     *
     * <p>Returns the complete set of assets owned by the specified Identity.
     * Ownership enables access control, accountability, and continuity
     * tracking within the platform.</p>
     *
     * @param identityId the unique identifier of the Identity
     * @return an {@link IdentityOwnership} containing all ownership records,
     *         or {@code null} if the Identity was not found
     * @throws IllegalArgumentException if {@code identityId} is {@code null}
     */
    IdentityOwnership getOwnership(String identityId);
}