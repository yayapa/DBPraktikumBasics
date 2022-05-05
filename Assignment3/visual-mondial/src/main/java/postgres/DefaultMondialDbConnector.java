package postgres;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DefaultMondialDbConnector implements DbConnector{

  @Override
  public Connection getConnection() throws SQLException {
    String url = "jdbc:postgresql://localhost:63333/mondial";
    String user = "dummy";
    String password = "dummy";
    return DriverManager.getConnection(url, user, password);

  }
}
