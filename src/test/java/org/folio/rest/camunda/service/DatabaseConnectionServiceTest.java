package org.folio.rest.camunda.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.h2.tools.Server;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DatabaseConnectionServiceTest {

  private static final String KEY = "testPool";
  private static final String URL = "jdbc:h2:mem:testdb";

  private static final String USER = "sa";
  private static final String PASSWORD = "";

  private static final Properties INFO = new Properties();

  private static Server h2Server;

  static {
    INFO.setProperty("user", USER);
    INFO.setProperty("password", PASSWORD);
  }

  @InjectMocks
  private DatabaseConnectionService databaseConnectionService;

  @BeforeAll
  public static void startH2() throws SQLException {
    h2Server = Server.createTcpServer().start();
    try (Connection conn = DriverManager.getConnection(URL + ";INIT=CREATE SCHEMA IF NOT EXISTS testdb", USER, PASSWORD)) {
      try (Statement stmt = conn.createStatement()) {
        stmt.execute("CREATE TABLE IF NOT EXISTS testdb.test_table (id INT PRIMARY KEY, name VARCHAR(255))");
      }
    }
  }

  @AfterAll
  public static void stopH2() {
    h2Server.stop();
  }

  @Test
  void testCreatePool() throws SQLException {
    databaseConnectionService.createPool(KEY, URL, INFO);
    try (Connection conn = databaseConnectionService.getConnection(KEY)) {
      assertNotNull(conn);
    }
  }

  @Test
  void testCreatePoolAlreadyExists() throws SQLException {
    databaseConnectionService.createPool(KEY, URL, INFO);
    databaseConnectionService.createPool(KEY, URL, INFO);
    try (Connection conn = databaseConnectionService.getConnection(KEY)) {
      assertNotNull(conn);
    }
  }

  @Test
  void testGetConnection() throws SQLException {
    assertThrows(NullPointerException.class, () -> {
      databaseConnectionService.getConnection(KEY);
    });
    databaseConnectionService.createPool(KEY, URL, INFO);
    try (Connection conn = databaseConnectionService.getConnection(KEY)) {
      assertNotNull(conn);
    }
  }

  @Test
  void testDestroyConnection() throws SQLException {
    databaseConnectionService.destroyConnection(KEY);
    assertThrows(NullPointerException.class, () -> {
      databaseConnectionService.getConnection(KEY);
    });
  }
}
