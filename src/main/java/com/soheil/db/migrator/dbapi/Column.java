package com.soheil.db.migrator.dbapi;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Column {
    public enum DataType {
        STRING("varchar(100)"),
        INT("int"),
        FLOAT("float"),
        TIMESTAMP("timestamp");

        private final String asSql;

        DataType(String asSql) {
            this.asSql = asSql;
        }

        public String toSql() {
            return this.asSql;
        }
    }

    private final Connection conn;
    private final String columnName;
    private Table table;

    public Column(Connection conn, String columnName, Table table) {
        this.conn = conn;
        this.columnName = columnName;
        this.table = table;
    }

    public String getName() {
        return columnName;
    }

    public DataType getDataType() throws SQLException {
      ResultSet res = conn.prepareStatement("select "+columnName+" from " +table.getName()+" where 1<0").executeQuery();
      int type = res.getMetaData().getColumnType(1);
      switch(type) {
        case 12: return DataType.STRING;
        case 4: return DataType.INT;
        case 6: return DataType.FLOAT;
        case 93: return DataType.TIMESTAMP;
      }
      // Something went wrong
      return null;
    }
}
