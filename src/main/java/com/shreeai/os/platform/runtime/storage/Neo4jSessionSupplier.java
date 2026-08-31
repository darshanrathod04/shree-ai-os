package com.shreeai.os.platform.runtime.storage;

/**
 * <b>Neo4jSessionSupplier</b>
 *
 * <p>Functional boundary isolating the Neo4j adapter from driver/session
 * acquisition. Keeps the adapter testable (fake suppliers) and free of
 * driver-lifecycle logic.</p>
 *
 * <p><b>Ownership:</b> Runtime — Storage</p>
 * <p><b>Version:</b> 1.0</p>
 * <p><b>Constitutional Authority:</b> PHASE-1-ARCH-001</p>
 */
@FunctionalInterface
public interface Neo4jSessionSupplier {

    /**
     * Opens a new Neo4j session. The caller MUST close it.
     *
     * @return an open session (never null)
     * @throws StorageRuntimeException if the session cannot be established
     */
    org.neo4j.driver.Session get();
}
