package com.shreeai.os.platform.runtime.persistence;

import javax.sql.DataSource;
import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * PostgreSQL implementation of MemoryVersionLedgerRepository.
 */
public final class PgMemoryVersionLedgerRepository implements MemoryVersionLedgerRepository {

    private final DataSource dataSource;

    public PgMemoryVersionLedgerRepository(DataSource dataSource) {
        this.dataSource = Objects.requireNonNull(dataSource, "dataSource must not be null");
    }

    @Override
    public boolean recordVersion(String tenantId, String memoryId, long version, String changeType, String snapshot) {
        String sql = """
            INSERT INTO memory_version_ledger (tenant_id, memory_id, version, change_type, snapshot, recorded_at)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tenantId);
            ps.setString(2, memoryId);
            ps.setLong(3, version);
            ps.setString(4, changeType);
            ps.setString(5, snapshot);
            ps.setTimestamp(6, Timestamp.from(Instant.now()));
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to record memory version", e);
        }
    }

    @Override
    public List<VersionInfo> findVersionHistory(String tenantId, String memoryId, int limit) {
        String sql = """
            SELECT memory_id, version, change_type, snapshot, recorded_at
            FROM memory_version_ledger
            WHERE tenant_id = ? AND memory_id = ?
            ORDER BY version DESC
            LIMIT ?
            """;
        List<VersionInfo> results = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tenantId);
            ps.setString(2, memoryId);
            ps.setInt(3, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                results.add(mapVersionInfo(rs));
            }
            return results;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find version history", e);
        }
    }

    @Override
    public VersionInfo findLatestVersion(String tenantId, String memoryId) {
        String sql = """
            SELECT memory_id, version, change_type, snapshot, recorded_at
            FROM memory_version_ledger
            WHERE tenant_id = ? AND memory_id = ?
            ORDER BY version DESC
            LIMIT 1
            """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tenantId);
            ps.setString(2, memoryId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapVersionInfo(rs);
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find latest version", e);
        }
    }

    private VersionInfo mapVersionInfo(ResultSet rs) throws SQLException {
        return new VersionInfo(
                rs.getString("memory_id"),
                rs.getLong("version"),
                rs.getString("change_type"),
                rs.getString("snapshot"),
                rs.getTimestamp("recorded_at").toInstant()
        );
    }
}