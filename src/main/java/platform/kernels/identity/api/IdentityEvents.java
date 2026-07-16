package platform.kernels.identity.api;

/**
 * <b>IdentityEvents</b>
 *
 * <p>Defines events exposed by the Identity Kernel within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Defines the contract for all Identity event types.</li>
 *   <li>Events represent significant occurrences within the Identity Kernel.</li>
 *   <li>Enables other kernels to react to Identity state changes.</li>
 *   <li>Provides stable event contracts for platform-wide communication.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Identity Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Invariant:</b> Events are immutable representations of occurrences.
 * They do not contain business logic or handling mechanisms.</p>
 *
 * <p><b>Constitutional Authority:</b> ADD-104, ADD-105, ADD-106</p>
 *
 * @see platform.kernels.identity.api.IdentityKernel
 * @see platform.kernels.identity.api.IdentityCommands
 */
public interface IdentityEvents {

    /**
     * Event published when a new Identity is created.
     *
     * <p>This event is published after an Identity has been successfully
     * created within the platform. It contains the unique identifier of
     * the newly created Identity.</p>
     *
     * <p>Other kernels can subscribe to this event to react to new
     * Identity creation, such as initializing related resources or
     * establishing default configurations.</p>
     */
    String IDENTITY_CREATED = "identity.created";

    /**
     * Event published when an Identity's profile is updated.
     *
     * <p>This event is published after an Identity's profile has been
     * successfully updated. It contains the Identity identifier and
     * information about what was changed.</p>
     *
     * <p>Other kernels can subscribe to this event to maintain consistency
     * with Identity profile changes, such as updating cached data or
     * propagating changes to dependent systems.</p>
     */
    String IDENTITY_UPDATED = "identity.updated";

    /**
     * Event published when a relationship is created between two Identities.
     *
     * <p>This event is published after a relationship has been successfully
     * registered between two Identities. It contains information about
     * the source Identity, target Identity, and relationship type.</p>
     *
     * <p>Other kernels can subscribe to this event to maintain relationship
     * consistency, enforce relationship-based access control, or update
     * social graphs.</p>
     */
    String RELATIONSHIP_CREATED = "identity.relationship.created";

    /**
     * Event published when ownership is registered for an asset.
     *
     * <p>This event is published after ownership has been successfully
     * registered, establishing that an Identity owns a specific asset.
     * It contains information about the owning Identity and the owned asset.</p>
     *
     * <p>Other kernels can subscribe to this event to enforce access control,
     * track asset ownership, or maintain ownership records.</p>
     */
    String OWNERSHIP_REGISTERED = "identity.ownership.registered";
}