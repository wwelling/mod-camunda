package org.folio.rest.service;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

@Service
public class DatabaseConnectionService {

  private Map<String, Connection> connections;

  public DatabaseConnectionService() {
    this.connections = new HashMap<>();
  }

  public void addConnection(String identifier, Connection connection) {
    this.connections.put(identifier, connection);
  }

  public Connection getConnection(String identifier) {
    return this.connections.get(identifier);
  }

}
