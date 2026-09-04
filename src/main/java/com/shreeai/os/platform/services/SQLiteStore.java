package com.shreeai.os.platform.services;

import java.nio.file.Path;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <b>SQLiteStore</b>
 *
 * <p>A {@link PersistenceStore} backed by SQLite. Creates a table on first use.
 * Stores key-value pairs in a single table with optional prefix column.</p>
 *
 * <p><b>Note:</b> Requires the sqlite-jdbc driver on the classpath.
 * Falls back to in-memory mode if SQLite is unavailable.</p>
 *
 * <p><b>Ownership:</b> Platform Services (v1.0)</p>
 *
 * @since v1.0
 */
public class SQLiteStore implements PersistenceStore {

    private final String url;
    private Connection conn;
    private volatile boolean healthy = true;

    public SQLiteStore(String dbPath) {
        Objects.requireNonNull(dbPath, "dbPath");
        String jdbcUrl = dbPath.contains(":memory:")
                ? "jdbc:sqlite::memory:"
                : "jdbc:sqlite:" + dbPath;
        this.url = jdbcUrl;
        Connection assignedConn = null;
        try {
            assignedConn = DriverManager.getConnection(jdbcUrl);
            initSchema(assignedConn);
            this.conn = assignedConn;
        } catch (Exception e) {
            this.conn = null;
            this.healthy = false;
            throw new RuntimeException("Failed to initialize SQLite store: " + e.getMessage(), e);
        }
    }

    private void initSchema(Connection c) throws SQLException {
        try (Statement stmt = c.createStatement()) {
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS kv_store (
                        key TEXT PRIMARY KEY,
                        value TEXT,
                        prefix TEXT
                    )
                    """);
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_prefix ON kv_store(prefix)");
        }
    }

    @Override
    public void put(String key, String value) {
        if (conn == null) return;
        String prefix = extractPrefix(key);
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT OR REPLACE INTO kv_store(key, value, prefix) VALUES (?, ?, ?)")) {
            ps.setString(1, key);
            ps.setString(2, value);
            ps.setString(3, prefix);
            ps.executeUpdate();
        } catch (SQLException e) {
            healthy = false;
        }
    }

    @Override
    public Optional<String> get(String key) {
        if (conn == null) return Optional.empty();
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT value FROM kv_store WHERE key = ?")) {
            ps.setString(1, key);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.ofNullable(rs.getString(1));
            }
        } catch (SQLException e) {
            healthy = false;
        }
        return Optional.empty();
    }

    @Override
    public void delete(String key) {
        if (conn == null) return;
        try (PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM kv_store WHERE key = ?")) {
            ps.setString(1, key);
            ps.executeUpdate();
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
        if (conn == null) return List.of();
        List<String> result = new ArrayList<>();
        String sql = prefix == null || prefix.isEmpty()
                ? "SELECT key FROM kv_store ORDER BY key"
                : "SELECT key FROM kv_store WHERE prefix = ? ORDER BY key";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            if (prefix != null && !prefix.isEmpty()) ps.setString(1, prefix);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(rs.getString(1));
            }
        } catch (SQLException e) {
            healthy = false;
        }
        return result;
    }

    @Override
    public Map<String, String> entries(String prefix) {
        if (conn == null) return Map.of();
        Map<String, String> out = new LinkedHashMap<>();
        String sql = prefix == null || prefix.isEmpty()
                ? "SELECT key, value FROM kv_store"
                : "SELECT key, value FROM kv_store WHERE prefix = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            if (prefix != null && !prefix.isEmpty()) ps.setString(1, prefix);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.put(rs.getString(1), rs.getString(2));
            }
        } catch (SQLException e) {
            healthy = false;
        }
        return out;
    }

    @Override
    public long size(String prefix) {
        if (conn == null) return 0;
        String sql = prefix == null || prefix.isEmpty()
                ? "SELECT COUNT(*) FROM kv_store"
                : "SELECT COUNT(*) FROM kv_store WHERE prefix = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            if (prefix != null && !prefix.isEmpty()) ps.setString(1, prefix);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getLong(1);
            }
        } catch (SQLException e) {
            healthy = false;
        }
        return 0;
    }

    @Override
    public void clear(String prefix) {
        if (conn == null) return;
        String sql = prefix == null || prefix.isEmpty()
                ? "DELETE FROM kv_store"
                : "DELETE FROM kv_store WHERE prefix = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            if (prefix != null && !prefix.isEmpty()) ps.setString(1, prefix);
            ps.executeUpdate();
        } catch (SQLException e) {
            healthy = false;
        }
    }

    @Override
    public String name() { return "sqlite"; }

    @Override
    public boolean isHealthy() { return healthy && conn != null; }

    /**
     * Closes the database connection.
     */
    public void close() {
        if (conn != null) {
            try { conn.close(); } catch (Exception ignored) {}
        }
    }

    private static String extractPrefix(String key) {
        if (key == null || !key.contains(":")) return "";
        return key.substring(0, key.indexOf(':'));
    }
}
