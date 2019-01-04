package com.soheil.db.migrator.dbapi;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;

public class Row {
    private final Table table;
    private final Connection conn;
    private HashMap<String, Object> attributes;

    public Row(Table table, Connection conn) {
        this.table = table;
        this.conn = conn;
        this.attributes = new HashMap<>();
    }

    public void setAttribute(String key, Object value) {
        attributes.put(key, value);
    }

    public Object getAttribute(String key) {
        return attributes.get(key.toUpperCase());
    }

    public int getId() {
        return (int) attributes.get("ID");
    }

    public int update(String column, Object value) throws SQLException {
        final String sql = "UPDATE " + table.getName() + " SET " + column + " = ? WHERE id = ?";
        final PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setObject(1, value);
        stmt.setInt(2, getId());
        return stmt.executeUpdate();
    }

    public HashMap<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(HashMap<String, Object> attributes) {
        this.attributes = attributes;
    }
}
