package com.shreeai.os.platform.runtime.vector;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * <b>SqlConnectionSupplier</b>
 *
 * <p>Functional boundary isolating JDBC adapters from connection acquisition.
 * Adapters receive connections through this supplier, keeping them testable
 * (fake suppliers in unit tests) and free of driver-specific lookup logic.</p>
 *
 * <p><b>Ownership:</b> Runtime — Vector</p>
 * <p><b>Version:</b> 1.0</p>
 * <p><b>Constitutional Authority:</b> PHASE-1-ARCH-001</p>
 */
@FunctionalInterface
public interface SqlConnectionSupplier {

    /**
     * Opens a new database connection. The caller MUST close it.
     *
     * @return an open JDBC connection (never null)
     * @throws SQLException if the connection cannot be established
     */
    Connection get() throws SQLException;
}
