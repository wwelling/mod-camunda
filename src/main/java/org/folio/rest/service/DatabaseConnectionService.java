package org.folio.rest.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

import org.springframework.stereotype.Service;

@Service
public class DatabaseConnectionService {

  private final Map<String, Connection> connections;

  public DatabaseConnectionService() {
    this.connections = new HashMap<>();
  }

  public synchronized Connection createConnection(String identifier, String url, Properties info) throws SQLException {
    Connection conn = DriverManager.getConnection(url, info);
    this.connections.put(identifier, conn);
    return conn;
  }

  public synchronized Connection getConnection(String identifier) {
    return this.connections.get(identifier);
  }

  public synchronized void destroyConnection(String identifier) throws SQLException {
    Connection conn = this.connections.remove(identifier);
    if (Objects.nonNull(conn)) {
      conn.close();
    }
  }

}
