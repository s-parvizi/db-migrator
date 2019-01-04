package com.soheil.db.migrator.dbapi;

import java.nio.file.Path;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Database {

  private String location;
  private Connection conn;

  private Path dir;

  public Database(Path dir) throws SQLException {

    this.location = "jdbc:h2:" + dir.toAbsolutePath();
    this.conn = DriverManager.getConnection(location, "", "");

    this.dir = dir.toAbsolutePath();
/*
    this.location = "jdbc:mysql://localhost:3306/relex";
    this.conn = DriverManager.getConnection(location, "db", "123");
*/

  }

  public static Database connect(Path dir) throws SQLException {
    return new Database(dir);
  }

  public void disconnect() {
    dir.toFile().delete();
  }

  public void addTable(String name) throws SQLException {
    final PreparedStatement stmt = conn.prepareStatement("CREATE TABLE " + name + "(ID INT auto_increment PRIMARY KEY);");
    stmt.execute();
  }

  public Table getTable(String name) {
    return new Table(conn, name);
  }

  public List<Table> getTables() throws SQLException {
    final PreparedStatement stmt = conn.prepareStatement("SHOW TABLES");
    final ResultSet rs = stmt.executeQuery();
    List<Table> tables = new ArrayList<>();
    while (rs.next()) {
      tables.add(new Table(conn, rs.getString(1)));
    }
    return tables;
  }
}
