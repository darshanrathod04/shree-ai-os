package com.shreeai.os.platform.services;

import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <b>PostgreSqlStore</b>
 *
 * <p>A {@link PersistenceStore} backed by PostgreSQL. Creates a table on first use.
 * Requires the PostgreSQL JDBC driver on the classpath.</p>
 *
 * <p><b>Ownership:</b> Platform Services (v1.0)</p>
 *
 * @since v1.0
 */
public class PostgreSqlStore implements PersistenceStore {

    private final String url;
    private final Properties props;
    private volatile boolean healthy = true;

    public PostgreSqlStore(String host, int port, String database, String user, String password) {
        Objects.requireNonNull(host, "host");
        Objects.requireNonNull(database, "database");
        this.url = String.format("jdbc:postgresql://%s:%d/%s", host, port, database);
        this.props = new Properties();
        if (user != null) props.setProperty("user", user);
        if (password != null) props.setProperty("password", password);
        props.setProperty("connectTimeout", "5");
        props.setProperty("socketTimeout", "5");
        // Attempt init, but don't fail constructor — just mark unhealthy
        try {
            try (Connection c = DriverManager.getConnection(url, props)) {
                initSchema(c);
            }
        } catch (Exception e) {
            this.healthy = false;
        }
    }

    private void initSchema(Connection c) throws SQLException {
        try (Statement stmt = c.createStatement()) {
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS shree_kv_store (
                        key TEXT PRIMARY KEY,
                        value TEXT,
                        prefix TEXT
                    )
                    """);
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_shree_prefix ON shree_kv_store(prefix)");
        }
    }

    private Connection getConnection() {
        if (!healthy) return null;
        try {
            return DriverManager.getConnection(url, props);
        } catch (SQLException e) {
            healthy = false;
            return null;
        }
    }

    @Override
    public void put(String key, String value) {
        try (Connection c = getConnection()) {
            if (c == null) return;
            try (PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO shree_kv_store(key, value, prefix) VALUES (?, ?, ?)" +
                            " ON CONFLICT(key) DO UPDATE SET value = EXCLUDED.value")) {
                ps.setString(1, key);
                ps.setString(2, value);
                ps.setString(3, extractPrefix(key));
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            healthy = false;
        }
    }

    @Override
    public Optional<String> get(String key) {
        try (Connection c = getConnection()) {
            if (c == null) return Optional.empty();
            try (PreparedStatement ps = c.prepareStatement(
                    "SELECT value FROM shree_kv_store WHERE key = ?")) {
                ps.setString(1, key);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return Optional.ofNullable(rs.getString(1));
                }
            }
        } catch (SQLException e) {
            healthy = false;
        }
        return Optional.empty();
    }

    @Override
    public void delete(String key) {
        try (Connection c = getConnection()) {
            if (c == null) return;
            try (PreparedStatement ps = c.prepareStatement(
                    "DELETE FROM shree_kv_store WHERE key = ?")) {
                ps.setString(1, key);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            healthy = false;
        }
    }

    @Override
    public boolean exists(String key) {
        return get(key).isPresent();
    }

    @Override
    public List<String> keys(String prefix) {
        List<String> result = new ArrayList<>();
        try (Connection c = getConnection()) {
            if (c == null) return result;
            String sql = prefix == null || prefix.isEmpty()
                    ? "SELECT key FROM shree_kv_store ORDER BY key"
                    : "SELECT key FROM shree_kv_store WHERE prefix = ? ORDER BY key";
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                if (prefix != null && !prefix.isEmpty()) ps.setString(1, prefix);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) result.add(rs.getString(1));
                }
            }
        } catch (SQLException e) {
            healthy = false;
        }
        return result;
    }

    @Override
    public Map<String, String> entries(String prefix) {
        Map<String, String> out = new LinkedHashMap<>();
        try (Connection c = getConnection()) {
            if (c == null) return out;
            String sql = prefix == null || prefix.isEmpty()
                    ? "SELECT key, value FROM shree_kv_store"
                    : "SELECT key, value FROM shree_kv_store WHERE prefix = ?";
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                if (prefix != null && !prefix.isEmpty()) ps.setString(1, prefix);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) out.put(rs.getString(1), rs.getString(2));
                }
            }
        } catch (SQLException e) {
            healthy = false;
        }
        return out;
    }

    @Override
    public long size(String prefix) {
        try (Connection c = getConnection()) {
            if (c == null) return 0;
            String sql = prefix == null || prefix.isEmpty()
                    ? "SELECT COUNT(*) FROM shree_kv_store"
                    : "SELECT COUNT(*) FROM shree_kv_store WHERE prefix = ?";
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                if (prefix != null && !prefix.isEmpty()) ps.setString(1, prefix);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return rs.getLong(1);
                }
            }
        } catch (SQLException e) {
            healthy = false;
        }
        return 0;
    }

    @Override
    public void clear(String prefix) {
        try (Connection c = getConnection()) {
            if (c == null) return;
            String sql = prefix == null || prefix.isEmpty()
                    ? "TRUNCATE shree_kv_store"
                    : "DELETE FROM shree_kv_store WHERE prefix = ?";
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                if (prefix != null && !prefix.isEmpty()) ps.setString(1, prefix);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            healthy = false;
        }
    }

    @Override
    public String name() { return "postgresql"; }

    @Override
    public boolean isHealthy() { return healthy; }

    private static String extractPrefix(String key) {
        if (key == null || !key.contains(":")) return "";
        return key.substring(0, key.indexOf(':'));
    }
}
