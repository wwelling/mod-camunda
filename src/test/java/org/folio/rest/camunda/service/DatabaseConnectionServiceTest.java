package org.folio.rest.camunda.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import com.zaxxer.hikari.HikariDataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DatabaseConnectionServiceTest {

    @InjectMocks
    private DatabaseConnectionService databaseConnectionService;

    @Mock
    private HikariDataSource dataSource;

    @Mock
    private Connection connection;

    @BeforeEach
    void setUp() throws SQLException {
      when(dataSource.getConnection()).thenReturn(connection);
    }

    @Test
    void testCreatePool() throws SQLException {
      String key = "testPool";
      String url = "jdbc:testdb";
      Properties info = new Properties();
      info.setProperty("user", "testUser");
      info.setProperty("password", "testPass");

      databaseConnectionService.createPool(key, url, info);

      assertTrue(databaseConnectionService.getConnection(key) != null);
      verify(dataSource).getConnection();
    }

    @Test
    void testCreatePoolAlreadyExists() throws SQLException {
      String key = "testPool";
      String url = "jdbc:testdb";
      Properties info = new Properties();
      info.setProperty("user", "testUser");
      info.setProperty("password", "testPass");

      databaseConnectionService.createPool(key, url, info);
      databaseConnectionService.createPool(key, url, info);

      assertTrue(databaseConnectionService.getConnection(key) != null);
      verify(dataSource, times(1)).getConnection();
    }

    @Test
    void testGetConnection() throws SQLException {
      String key = "testPool";
      String url = "jdbc:testdb";
      Properties info = new Properties();
      info.setProperty("user", "testUser");
      info.setProperty("password", "testPass");

      databaseConnectionService.createPool(key, url, info);
      Connection conn = databaseConnectionService.getConnection(key);

      assertNotNull(conn);
      verify(dataSource).getConnection();
    }

    @Test
    void testDestroyConnection() throws SQLException {
      String key = "testPool";
      databaseConnectionService.destroyConnection(key);
    }
  }
