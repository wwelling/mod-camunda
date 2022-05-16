package org.folio.rest.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import org.springframework.stereotype.Service;

@Service
public class DatabaseConnectionService {

  private final Map<String, HikariDataSource> pools;

  public DatabaseConnectionService() {
    this.pools = new HashMap<>();
  }

  public synchronized void createPool(String key, String url, Properties info) throws SQLException {
    if (!this.pools.containsKey(key)) {
      HikariConfig config = new HikariConfig();

      config.setLeakDetectionThreshold(0);
      config.setJdbcUrl(url);
      config.setUsername(info.getProperty("user"));
      config.setPassword(info.getProperty("password"));
      // config.setDriverClassName();
      config.setMaximumPoolSize(8);
      config.addDataSourceProperty("oracle.jdbc.implicitStatementCacheSize", 10);

      // would be nice to just use this but have to ensure properties match
      // config.setDataSourceProperties(info);

      HikariDataSource dataSource = new HikariDataSource(config);

      this.pools.put(key, dataSource);
    }
  }

  public synchronized Connection getConnection(String poolKey) throws SQLException {
    return this.pools.get(poolKey).getConnection();
  }

  public synchronized void destroyConnection(String poolKey) throws SQLException {
    // do nothing
  }

}
