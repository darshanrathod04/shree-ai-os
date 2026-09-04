package com.shreeai.os.platform.runtime.persistence;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

/**
 * PostgreSQL implementation of EpisodicMemoryRepository.
 */
public final class PgEpisodicMemoryRepository implements EpisodicMemoryRepository {

    private final DataSource dataSource;

    public PgEpisodicMemoryRepository(DataSource dataSource) {
        this.dataSource = Objects.requireNonNull(dataSource, "dataSource must not be null");
    }

    @Override
    public boolean save(String tenantId, String memoryId, String content, Map<String, String> metadata) {
        String sql = """
            INSERT INTO episodic_memory (tenant_id, memory_id, content, metadata, created_at, updated_at)
            VALUES (?, ?, ?, ?::jsonb, NOW(), NOW())
            ON CONFLICT (tenant_id, memory_id)
            DO UPDATE SET content = EXCLUDED.content, metadata = EXCLUDED.metadata::jsonb, updated_at = NOW()
            """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tenantId);
            ps.setString(2, memoryId);
            ps.setString(3, content);
            ps.setString(4, toJson(metadata));
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save episodic memory", e);
        }
    }

    @Override
    public Optional<String> findById(String tenantId, String memoryId) {
        String sql = "SELECT content FROM episodic_memory WHERE tenant_id = ? AND memory_id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tenantId);
            ps.setString(2, memoryId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(rs.getString("content"));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find episodic memory", e);
        }
    }

    @Override
    public List<String> findByTenantId(String tenantId, int limit) {
        String sql = "SELECT content FROM episodic_memory WHERE tenant_id = ? ORDER BY updated_at DESC LIMIT ?";
        List<String> results = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tenantId);
            ps.setInt(2, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                results.add(rs.getString("content"));
            }
            return results;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find episodic memories by tenant", e);
        }
    }

    @Override
    public Set<String> findAllTenantIds() {
        String sql = "SELECT DISTINCT tenant_id FROM episodic_memory";
        Set<String> tenantIds = new HashSet<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                tenantIds.add(rs.getString("tenant_id"));
            }
            return tenantIds;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find tenant IDs", e);
        }
    }

    @Override
    public boolean delete(String tenantId, String memoryId) {
        String sql = "DELETE FROM episodic_memory WHERE tenant_id = ? AND memory_id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tenantId);
            ps.setString(2, memoryId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete episodic memory", e);
        }
    }

    private String toJson(Map<String, String> metadata) {
        if (metadata == null || metadata.isEmpty()) {
            return "{}";
        }
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, String> entry : metadata.entrySet()) {
            if (!first) sb.append(",");
            sb.append("\"").append(escapeJson(entry.getKey())).append("\":\"")
              .append(escapeJson(entry.getValue())).append("\"");
            first = false;
        }
        sb.append("}");
        return sb.toString();
    }

    private String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}