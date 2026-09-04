package com.shreeai.os.platform.runtime.persistence;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shreeai.os.platform.runtime.reflection.ReflectionHistory;
import com.shreeai.os.platform.runtime.reflection.ReflectionRepository;

import javax.sql.DataSource;
import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * PostgreSQL implementation of {@link ReflectionRepository}.
 *
 * <p>Persists reflection history to the {@code reflection_history} L2 table.
 * Tenant and organization identifiers are always stored and every query is
 * scoped by tenant to enforce isolation at the data layer.</p>
 *
 * <p><b>Ownership:</b> Runtime — Persistence (L2 Ledger)</p>
 * <p><b>Version:</b> 1.0</p>
 */
public final class PgReflectionHistoryRepository implements ReflectionRepository {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final DataSource dataSource;

    public PgReflectionHistoryRepository(DataSource dataSource) {
        this.dataSource = Objects.requireNonNull(dataSource, "dataSource must not be null");
    }

    @Override
    public ReflectionHistory save(ReflectionHistory history) {
        Objects.requireNonNull(history, "history must not be null");

        String sql = """
            INSERT INTO reflection_history (
                tenant_id, organization_id, execution_id, request_id,
                verdict, score, importance_score, lessons, root_cause,
                retry_advised, evaluated_at
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?::jsonb, ?, ?, ?)
            ON CONFLICT (tenant_id, execution_id)
            DO UPDATE SET
                organization_id = EXCLUDED.organization_id,
                request_id = EXCLUDED.request_id,
                verdict = EXCLUDED.verdict,
                score = EXCLUDED.score,
                importance_score = EXCLUDED.importance_score,
                lessons = EXCLUDED.lessons::jsonb,
                root_cause = EXCLUDED.root_cause,
                retry_advised = EXCLUDED.retry_advised,
                evaluated_at = EXCLUDED.evaluated_at
            """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, history.tenantId());
            ps.setString(2, history.organizationId());
            ps.setString(3, history.executionId());
            ps.setString(4, history.requestId());
            ps.setString(5, history.verdict());
            ps.setDouble(6, history.score());
            ps.setInt(7, history.importanceScore());
            ps.setString(8, toJson(history.lessons()));
            ps.setString(9, history.rootCause());
            ps.setBoolean(10, history.retryAdvised());
            ps.setTimestamp(11, Timestamp.from(history.evaluatedAt()));
            ps.executeUpdate();
            return history;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save reflection history", e);
        }
    }

    @Override
    public Optional<ReflectionHistory> findByExecutionId(String tenantId, String executionId) {
        String sql = """
            SELECT tenant_id, organization_id, execution_id, request_id, verdict,
                   score, importance_score, lessons, root_cause, retry_advised, evaluated_at
            FROM reflection_history
            WHERE tenant_id = ? AND execution_id = ?
            """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tenantId);
            ps.setString(2, executionId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(mapHistory(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find reflection history", e);
        }
    }

    @Override
    public List<ReflectionHistory> findByTenantId(String tenantId, int limit) {
        String sql = """
            SELECT tenant_id, organization_id, execution_id, request_id, verdict,
                   score, importance_score, lessons, root_cause, retry_advised, evaluated_at
            FROM reflection_history
            WHERE tenant_id = ?
            ORDER BY evaluated_at DESC
            LIMIT ?
            """;
        return queryList(sql, tenantId, limit);
    }

    @Override
    public List<ReflectionHistory> findRecent(int limit) {
        String sql = """
            SELECT tenant_id, organization_id, execution_id, request_id, verdict,
                   score, importance_score, lessons, root_cause, retry_advised, evaluated_at
            FROM reflection_history
            ORDER BY evaluated_at DESC
            LIMIT ?
            """;
        List<ReflectionHistory> results = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                results.add(mapHistory(rs));
            }
            return results;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find recent reflections", e);
        }
    }

    @Override
    public long countByTenantId(String tenantId) {
        String sql = "SELECT COUNT(*) FROM reflection_history WHERE tenant_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tenantId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getLong(1);
            }
            return 0L;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count reflection history", e);
        }
    }

    @Override
    public List<ReflectionHistory> findFailuresByTenantId(String tenantId, int limit) {
        String sql = """
            SELECT tenant_id, organization_id, execution_id, request_id, verdict,
                   score, importance_score, lessons, root_cause, retry_advised, evaluated_at
            FROM reflection_history
            WHERE tenant_id = ? AND verdict = 'FAILURE'
            ORDER BY evaluated_at DESC
            LIMIT ?
            """;
        return queryList(sql, tenantId, limit);
    }

    private List<ReflectionHistory> queryList(String sql, String tenantId, int limit) {
        List<ReflectionHistory> results = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tenantId);
            ps.setInt(2, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                results.add(mapHistory(rs));
            }
            return results;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to query reflection history", e);
        }
    }

    private ReflectionHistory mapHistory(ResultSet rs) throws SQLException {
        return new ReflectionHistory(
                rs.getString("tenant_id"),
                rs.getString("organization_id"),
                rs.getString("execution_id"),
                rs.getString("request_id"),
                rs.getString("verdict"),
                rs.getDouble("score"),
                rs.getInt("importance_score"),
                fromJson(rs.getString("lessons")),
                rs.getString("root_cause"),
                rs.getBoolean("retry_advised"),
                toInstant(rs.getTimestamp("evaluated_at"))
        );
    }

    private String toJson(List<String> lessons) {
        try {
            return MAPPER.writeValueAsString(lessons != null ? lessons : List.of());
        } catch (Exception e) {
            return "[]";
        }
    }

    private List<String> fromJson(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return MAPPER.readValue(json, new TypeReference<List<String>>() { });
        } catch (Exception e) {
            return List.of();
        }
    }

    private Instant toInstant(Timestamp timestamp) {
        return timestamp != null ? timestamp.toInstant() : Instant.now();
    }
}