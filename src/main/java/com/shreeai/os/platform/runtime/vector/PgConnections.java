package com.shreeai.os.platform.runtime.vector;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;

/**
 * <b>PgConnections</b>
 *
 * <p>Factory for PostgreSQL {@link SqlConnectionSupplier}s used by the PgVector
 * adapters. Plain JDBC — no ORM, no pool — so the platform has zero mandatory
 * infrastructure dependency; production deployments may substitute a pooled
 * supplier without touching adapter code.</p>
 *
 * <p><b>Ownership:</b> Runtime — Vector</p>
 * <p><b>Version:</b> 1.0</p>
 * <p><b>Constitutional Authority:</b> PHASE-1-ARCH-001</p>
 */
public final class PgConnections {

    private PgConnections() {
        // static factory
    }

    /**
     * Creates a supplier that opens a fresh connection per call via
     * {@link DriverManager}.
     *
     * @param jdbcUrl  PostgreSQL JDBC URL (must not be null or blank)
     * @param user     database user (must not be null)
     * @param password database password (must not be null)
     * @return a connection supplier (never null)
     */
    public static SqlConnectionSupplier from(String jdbcUrl, String user, String password) {
        Objects.requireNonNull(jdbcUrl, "jdbcUrl must not be null");
        if (jdbcUrl.isBlank()) {
            throw new IllegalArgumentException("jdbcUrl must not be blank");
        }
        Objects.requireNonNull(user, "user must not be null");
        Objects.requireNonNull(password, "password must not be null");

        return () -> {
            try {
                return DriverManager.getConnection(jdbcUrl, user, password);
            } catch (SQLException e) {
                throw new VectorRuntimeException(
                        "Failed to open PostgreSQL connection: " + e.getMessage(), e);
            }
        };
    }
}
