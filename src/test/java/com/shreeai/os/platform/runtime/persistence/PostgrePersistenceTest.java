package com.shreeai.os.platform.runtime.persistence;

import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for the PostgreSQL L2 persistence adapters using a mocked JDBC layer.
 * Verifies tenant-scoped SQL flows and mapping behaviour without a live DB.
 */
class PostgrePersistenceTest {

    private final DataSource dataSource = mock(DataSource.class);
    private final Connection connection = mock(Connection.class);
    private final PreparedStatement statement = mock(PreparedStatement.class);
    private final ResultSet resultSet = mock(ResultSet.class);

    private void stubConnection() throws Exception {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(statement);
    }

    // ---------------------------------------------------------------
    // EpisodicMemory
    // ---------------------------------------------------------------

    @Test
    void episodicMemorySaveWritesAndReturnsTrue() throws Exception {
        stubConnection();
        when(statement.executeUpdate()).thenReturn(1);

        PgEpisodicMemoryRepository repo = new PgEpisodicMemoryRepository(dataSource);
        boolean saved = repo.save("t1", "mem-1", "content", Map.of("k", "v"));

        assertTrue(saved);
        verify(statement).setString(1, "t1");
        verify(statement).setString(2, "mem-1");
        verify(statement).setString(3, "content");
    }

    @Test
    void episodicMemoryFindByIdReturnsContent() throws Exception {
        stubConnection();
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("content")).thenReturn("hello");

        PgEpisodicMemoryRepository repo = new PgEpisodicMemoryRepository(dataSource);
        Optional<String> found = repo.findById("t1", "mem-1");

        assertTrue(found.isPresent());
        assertEquals("hello", found.get());
    }

    @Test
    void episodicMemoryFindByIdReturnsEmptyWhenMissing() throws Exception {
        stubConnection();
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        PgEpisodicMemoryRepository repo = new PgEpisodicMemoryRepository(dataSource);
        assertTrue(repo.findById("t1", "nope").isEmpty());
    }

    @Test
    void episodicMemoryFindByTenantIdReturnsAll() throws Exception {
        stubConnection();
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString("content")).thenReturn("c1", "c2");

        PgEpisodicMemoryRepository repo = new PgEpisodicMemoryRepository(dataSource);
        List<String> results = repo.findByTenantId("t1", 10);

        assertEquals(List.of("c1", "c2"), results);
        verify(statement).setString(1, "t1");
        verify(statement).setInt(2, 10);
    }

    @Test
    void episodicMemoryFindAllTenantIds() throws Exception {
        stubConnection();
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString("tenant_id")).thenReturn("t1", "t2");

        PgEpisodicMemoryRepository repo = new PgEpisodicMemoryRepository(dataSource);
        Set<String> tenants = repo.findAllTenantIds();

        assertEquals(Set.of("t1", "t2"), tenants);
    }

    @Test
    void episodicMemoryDeleteReturnsTrueWhenDeleted() throws Exception {
        stubConnection();
        when(statement.executeUpdate()).thenReturn(1);

        PgEpisodicMemoryRepository repo = new PgEpisodicMemoryRepository(dataSource);
        assertTrue(repo.delete("t1", "mem-1"));
    }

    // ---------------------------------------------------------------
    // MemoryVersionLedger
    // ---------------------------------------------------------------

    @Test
    void versionLedgerRecordVersionReturnsTrue() throws Exception {
        stubConnection();
        when(statement.executeUpdate()).thenReturn(1);

        PgMemoryVersionLedgerRepository repo = new PgMemoryVersionLedgerRepository(dataSource);
        boolean recorded = repo.recordVersion("t1", "mem-1", 3L, "UPDATE", "snapshot");

        assertTrue(recorded);
        verify(statement).setString(1, "t1");
        verify(statement).setLong(3, 3L);
        verify(statement).setString(4, "UPDATE");
    }

    @Test
    void versionLedgerFindVersionHistoryMapsRows() throws Exception {
        stubConnection();
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString("memory_id")).thenReturn("mem-1", "mem-1");
        when(resultSet.getLong("version")).thenReturn(2L, 1L);
        when(resultSet.getString("change_type")).thenReturn("UPDATE", "CREATE");
        when(resultSet.getString("snapshot")).thenReturn("a", "b");
        when(resultSet.getTimestamp("recorded_at"))
                .thenReturn(java.sql.Timestamp.from(java.time.Instant.now()));

        PgMemoryVersionLedgerRepository repo = new PgMemoryVersionLedgerRepository(dataSource);
        List<MemoryVersionLedgerRepository.VersionInfo> history =
                repo.findVersionHistory("t1", "mem-1", 10);

        assertEquals(2, history.size());
        assertEquals(2L, history.get(0).version());
        assertEquals("CREATE", history.get(1).changeType());
    }

    @Test
    void versionLedgerFindLatestVersionReturnsNullWhenNone() throws Exception {
        stubConnection();
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        PgMemoryVersionLedgerRepository repo = new PgMemoryVersionLedgerRepository(dataSource);
        assertNull(repo.findLatestVersion("t1", "mem-1"));
    }
}