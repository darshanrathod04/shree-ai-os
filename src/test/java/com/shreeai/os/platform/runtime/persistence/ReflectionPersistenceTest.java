package com.shreeai.os.platform.runtime.persistence;

import com.shreeai.os.platform.runtime.reflection.ReflectionHistory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for PostgreSQL persistence of reflection history (L2 ledger).
 */
class ReflectionPersistenceTest {

    private final DataSource dataSource = mock(DataSource.class);
    private final Connection connection = mock(Connection.class);
    private final PreparedStatement statement = mock(PreparedStatement.class);
    private final ResultSet resultSet = mock(ResultSet.class);

    private PgReflectionHistoryRepository repository;

    private final Instant evaluatedAt = Instant.parse("2026-08-31T10:00:00Z");
    private final Timestamp evaluatedTimestamp = Timestamp.from(evaluatedAt);

    @BeforeEach
    void setUp() throws Exception {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(statement);
        repository = new PgReflectionHistoryRepository(dataSource);
    }

    @Test
    void savePersistsAndReturnsSameRecord() throws Exception {
        when(statement.executeUpdate()).thenReturn(1);
        ReflectionHistory history = historyOf("t1", "exec-1", "SUCCESS", 0.9, 40);

        ReflectionHistory saved = repository.save(history);

        assertSame(history, saved);
        verify(statement).setString(1, "t1");
        verify(statement).setString(2, "org-1");
        verify(statement).setString(3, "exec-1");
        verify(statement).setString(5, "SUCCESS");
        verify(statement).setDouble(6, 0.9);
        verify(statement).setInt(7, 40);
        verify(statement).setString(8, "[\"Lesson 1\"]");
    }

    @Test
    void findByExecutionIdReturnsMappedHistory() throws Exception {
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        stubRow("t1", "exec-1", "FAILURE", 0.2, 95);

        Optional<ReflectionHistory> found = repository.findByExecutionId("t1", "exec-1");

        assertTrue(found.isPresent());
        assertEquals("t1", found.get().tenantId());
        assertEquals("org-1", found.get().organizationId());
        assertEquals("exec-1", found.get().executionId());
        assertEquals("FAILURE", found.get().verdict());
        assertEquals(0.2, found.get().score());
        assertEquals(95, found.get().importanceScore());
        assertTrue(found.get().retryAdvised());
    }

    @Test
    void findByExecutionIdReturnsEmptyWhenNotPresent() throws Exception {
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        assertTrue(repository.findByExecutionId("t1", "missing").isEmpty());
    }

    @Test
    void findByTenantIdReturnsTenantScopedHistory() throws Exception {
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false);
        // Consecutive values for the two rows
        when(resultSet.getString("tenant_id")).thenReturn("t1");
        when(resultSet.getString("organization_id")).thenReturn("org-1");
        when(resultSet.getString("execution_id")).thenReturn("exec-1", "exec-2");
        when(resultSet.getString("request_id")).thenReturn("req-exec-1", "req-exec-2");
        when(resultSet.getString("verdict")).thenReturn("SUCCESS", "PARTIAL");
        when(resultSet.getDouble("score")).thenReturn(0.9, 0.5);
        when(resultSet.getInt("importance_score")).thenReturn(30, 60);
        when(resultSet.getString("lessons")).thenReturn("[\"Lesson 1\"]");
        when(resultSet.getString("root_cause")).thenReturn(null);
        when(resultSet.getBoolean("retry_advised")).thenReturn(false);
        when(resultSet.getTimestamp("evaluated_at")).thenReturn(evaluatedTimestamp);

        List<ReflectionHistory> records = repository.findByTenantId("t1", 10);

        assertEquals(2, records.size());
        assertEquals("exec-1", records.get(0).executionId());
        assertEquals("exec-2", records.get(1).executionId());
        assertEquals("PARTIAL", records.get(1).verdict());
    }

    @Test
    void findFailuresByTenantIdFiltersFailures() throws Exception {
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        stubRow("t1", "exec-f", "FAILURE", 0.1, 99);

        List<ReflectionHistory> failures = repository.findFailuresByTenantId("t1", 10);

        assertEquals(1, failures.size());
        assertEquals("exec-f", failures.get(0).executionId());
    }

    @Test
    void countByTenantIdReturnsCount() throws Exception {
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getLong(1)).thenReturn(5L);

        assertEquals(5, repository.countByTenantId("t1"));
    }

    @Test
    void tenantIsolationIsScopedInSql() throws Exception {
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        repository.findByExecutionId("tenant-a", "exec-1");

        verify(statement).setString(1, "tenant-a");
        verify(statement, never()).setString(1, "tenant-b");
    }

    private void stubRow(String tenant, String executionId, String verdict,
                         double score, int importance) throws Exception {
        when(resultSet.getString("tenant_id")).thenReturn(tenant);
        when(resultSet.getString("organization_id")).thenReturn("org-1");
        when(resultSet.getString("execution_id")).thenReturn(executionId);
        when(resultSet.getString("request_id")).thenReturn("req-" + executionId);
        when(resultSet.getString("verdict")).thenReturn(verdict);
        when(resultSet.getDouble("score")).thenReturn(score);
        when(resultSet.getInt("importance_score")).thenReturn(importance);
        when(resultSet.getString("lessons")).thenReturn("[\"Lesson 1\"]");
        when(resultSet.getString("root_cause")).thenReturn("FAILURE".equals(verdict) ? "timeout" : null);
        when(resultSet.getBoolean("retry_advised")).thenReturn("FAILURE".equals(verdict));
        when(resultSet.getTimestamp("evaluated_at")).thenReturn(evaluatedTimestamp);
    }

    private ReflectionHistory historyOf(String tenant, String executionId,
                                        String verdict, double score, int importance) {
        return new ReflectionHistory(
                tenant, "org-1", executionId, "req-" + executionId,
                verdict, score, importance, List.of("Lesson 1"),
                "FAILURE".equals(verdict) ? "timeout" : null,
                "FAILURE".equals(verdict), evaluatedAt
        );
    }
}