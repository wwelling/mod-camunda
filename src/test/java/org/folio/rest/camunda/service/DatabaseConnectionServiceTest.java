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

  @InjectMocks
  private DatabaseConnectionService databaseConnectionService;

  private static Server h2Server;

  private static final String key = "testPool";
  private static final String url = "jdbc:h2:mem:testdb";

  private static final String user = "sa";
  private static final String password = "";

  private static final Properties info = new Properties();

  static {
    info.setProperty("user", user);
    info.setProperty("password", password);
  }

  @BeforeAll
  public static void startH2() throws SQLException {
    h2Server = Server.createTcpServer().start();
    try (Connection conn = DriverManager.getConnection(url + ";INIT=CREATE SCHEMA IF NOT EXISTS testdb", user, password)) {
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
    databaseConnectionService.createPool(key, url, info);
    try (Connection conn = databaseConnectionService.getConnection(key)) {
      assertNotNull(conn);
    }
  }

  @Test
  void testCreatePoolAlreadyExists() throws SQLException {
    databaseConnectionService.createPool(key, url, info);
    databaseConnectionService.createPool(key, url, info);
    try (Connection conn = databaseConnectionService.getConnection(key)) {
      assertNotNull(conn);
    }
  }

  @Test
  void testGetConnection() throws SQLException {
    assertThrows(NullPointerException.class, () -> {
      databaseConnectionService.getConnection(key);
    });
    databaseConnectionService.createPool(key, url, info);
    try (Connection conn = databaseConnectionService.getConnection(key)) {
      assertNotNull(conn);
    }
  }

  @Test
  void testDestroyConnection() throws SQLException {
    databaseConnectionService.destroyConnection(key);
    assertThrows(NullPointerException.class, () -> {
      databaseConnectionService.getConnection(key);
    });
  }
}
