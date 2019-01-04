package com.soheil.db.migrator.dbapi;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Table {
    private final Connection conn;
    private final String tableName;

    public Table(Connection conn, String tableName) {
        this.conn = conn;
        this.tableName = tableName;
    }

    public void addColumn(String columnName, Column.DataType dataType) throws SQLException {
        String sql = "ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + dataType.toSql();
        final PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.execute();
    }

    public void changeColumnName(String oldColumnName, String newColumnName) throws SQLException {
        String sql = "ALTER TABLE " + tableName + " RENAME COLUMN " + oldColumnName + "to" + newColumnName;
        final PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.execute();
    }

    public void changeColumnType(String columnName, Column.DataType dataType) throws SQLException {
        String sql = "ALTER TABLE " + tableName + " MODIFY COLUMN " + columnName + " " + dataType.toSql();
        final PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.execute();
    }

    public String getName() {
        return tableName.toLowerCase();
    }

    public List<Column> getColumns() throws SQLException {
        final PreparedStatement stmt = conn.prepareStatement("SHOW COLUMNS FROM " + tableName);
        final ResultSet rs = stmt.executeQuery();
        List<Column> columns = new ArrayList<>();
        while (rs.next()) {
            final String colName = rs.getString(1).toLowerCase();
            columns.add(new Column(conn, colName, this));
        }
        return columns;
    }

    public Column getColumn(String columnName) {
        return new Column(conn, columnName, this);
    }

    public void insert(Map<String, Object> entries) throws SQLException {
        final List<String> columnNames = new ArrayList<>(entries.keySet());
        final List columnValues = new ArrayList(entries.values());

        final String columns = columnNames.stream().collect(Collectors.joining(", "));
        final String placeholders = Collections.nCopies(columnNames.size(), "?").stream().collect(Collectors.joining(", "));
        final PreparedStatement stmt = conn.prepareStatement(
            "INSERT INTO " + tableName +
                " (" + columns + ") VALUES " +
                " (" + placeholders + ")"
        );

        for (int i = 0; i < columnNames.size(); i++) {
            String colName = columnNames.get(i);
            Column c = getColumn(colName);
            switch (c.getDataType()) {
                case STRING:
                    stmt.setString(i+1, (String) columnValues.get(i));
                    break;
                case FLOAT:
                    stmt.setFloat(i+1, (Float) columnValues.get(i));
                    break;
                case TIMESTAMP:
                    stmt.setTimestamp(i+1, (Timestamp) columnValues.get(i));
                    break;
                case INT:
                    stmt.setInt(i+1, (Integer) columnValues.get(i));
                    break;
            }
        }

        stmt.executeUpdate();
    }

    public Row find(int id) throws SQLException {
        final String sql = "SELECT * FROM " + tableName + " WHERE id = ?";
        final PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, id);
        return convertResultSetToRow(stmt.executeQuery());
    }

    public List<Row> findAllBy(String column, String value) throws SQLException {
        final String sql = "SELECT * FROM " + tableName + " WHERE " + column + " = ?";
        final PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, value);
        return convertResultSetToList(stmt.executeQuery());
    }

    private List<Row> convertResultSetToList(ResultSet rs) throws SQLException {
        final ResultSetMetaData md = rs.getMetaData();
        final int columns = md.getColumnCount();
        final List<Row> list = new ArrayList<>();

        while (rs.next()) {
            final Row row = new Row(this, conn);
            for (int i=1; i<=columns; ++i) {
                row.setAttribute(md.getColumnName(i), rs.getObject(i));
            }
            list.add(row);
        }

        return list;
    }

    private Row convertResultSetToRow(ResultSet rs) throws SQLException {
        final ResultSetMetaData md = rs.getMetaData();
        final int columns = md.getColumnCount();
        final Row row = new Row(this, conn);

        rs.next();
        for (int i=1; i<=columns; ++i) {
            row.setAttribute(md.getColumnName(i), rs.getObject(i));
        }

        return row;
    }
}
