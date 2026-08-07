package com.shreeai.os.platform.kernels.context.api;

import com.shreeai.os.platform.kernels.context.error.ContextNotFoundException;
import com.shreeai.os.platform.kernels.context.model.ContextId;
import com.shreeai.os.platform.kernels.context.model.ContextSnapshot;

import java.util.List;

/**
 * <b>ContextSnapshotService</b>
 *
 * <p>Defines the contract for managing Context snapshots within the platform.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Defines the contract for Context snapshot operations.</li>
 *   <li>Provides methods to create and retrieve Context snapshots.</li>
 *   <li>Snapshots represent runtime state only, not Memory.</li>
 * </ul>
 *
 * <p><b>Snapshot Principles:</b></p>
 * <ul>
 *   <li>Snapshots represent runtime state only.</li>
 *   <li>Snapshots are not Memory.</li>
 *   <li>Snapshots are temporary and lightweight.</li>
 *   <li>Snapshots capture execution context at a point in time.</li>
 * </ul>
 *
 * <p><b>Thread Safety:</b> Implementations MUST be thread-safe. Multiple kernels
 * may concurrently access Context snapshots.</p>
 *
 * <p><b>Immutability:</b> All returned ContextSnapshot objects MUST be immutable.
 * Consumers MUST NOT modify returned objects.</p>
 *
 * <p><b>Ownership:</b> Context Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-CTX-101, EIO-ARCH-001</p>
 *
 * @see ContextService
 * @see ContextQueryService
 * @see ContextLifecycleService
 */
public interface ContextSnapshotService {

    /**
     * Creates a snapshot of a Context.
     *
     * <p>Creates a snapshot of the specified Context at the current point in time.
     * The snapshot captures the runtime state of the Context.</p>
     *
     * <p><b>Thread Safety:</b> This operation is thread-safe.</p>
     *
     * @param id the Context identifier (must not be null)
     * @return the created ContextSnapshot (never null)
     * @throws ContextNotFoundException if the Context does not exist
     */
    ContextSnapshot createSnapshot(ContextId id);

    /**
     * Retrieves the latest snapshot of a Context.
     *
     * <p>Retrieves the most recent snapshot of the specified Context. Returns
     * an empty Optional if no snapshots exist.</p>
     *
     * <p><b>Thread Safety:</b> This operation is thread-safe.</p>
     *
     * <p><b>Side Effects:</b> None. This is a read-only operation.</p>
     *
     * @param id the Context identifier (must not be null)
     * @return an Optional containing the latest ContextSnapshot, or empty if none exist
     */
    java.util.Optional<ContextSnapshot> latestSnapshot(ContextId id);

    /**
     * Retrieves the snapshot history of a Context.
     *
     * <p>Retrieves all snapshots of the specified Context, ordered by creation
     * time (most recent first). Returns an immutable list that may be empty
     * if no snapshots exist.</p>
     *
     * <p><b>Thread Safety:</b> This operation is thread-safe.</p>
     *
     * <p><b>Side Effects:</b> None. This is a read-only operation.</p>
     *
     * @param id the Context identifier (must not be null)
     * @return an immutable list of ContextSnapshots (never null, may be empty)
     */
    List<ContextSnapshot> history(ContextId id);
}