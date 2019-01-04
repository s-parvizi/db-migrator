package com.soheil.db.migrator;

import com.google.common.collect.Lists;
import com.soheil.db.migrator.dbapi.Column;
import com.soheil.db.migrator.dbapi.Database;
import com.soheil.db.migrator.dbapi.Table;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

// Some of the tests are written in Java
public class MigratorTests {

    private Path dir;
    private Path ROOT = Paths.get("dbs");

    @BeforeEach
    void before() throws IOException {
        this.dir = Files.createTempDirectory(ROOT.toAbsolutePath(), "dbapi");
    }

    @AfterEach
    void after() throws IOException {
        //FileUtils.cleanDirectory(ROOT.toFile());
    }

    @Test
    void itMigratesDatabase() throws Exception {
        final Database db = Database.connect(dir);

        // First Migration is needed for creating schema_history table
        Main.migrate(db);

        final List<String> tables0 = db.getTables()
                .stream()
                .map(Table::getName)
                .collect(Collectors.toList());

        assertEquals(tables0, Lists.newArrayList("schema_history"));

        // all migration files that do not have any track on schema_history will be migrated
        Main.migrate(db);

        final List<String> tables1 = db.getTables()
                .stream()
                .map(Table::getName)
                .collect(Collectors.toList());

        assertEquals(tables1, Lists.newArrayList("schema_history", "users"));

        final List<String> columns = db.getTable("users")
                .getColumns()
                .stream()
                .map(Column::getName)
                .collect(Collectors.toList());

        assertEquals(Lists.newArrayList("id", "username", "password", "email"), columns);

        db.disconnect();
    }

    @Test
    void dynamicTables() throws Exception {

        final Database db = Database.connect(dir);

        // First Migration is needed for creating schema_history table
        Main.migrate(db);

        final String randomTable = "user_" + (int) (Math.random() * 1000);
        db.addTable(randomTable);
        final Table users = db.getTable(randomTable);
        users.addColumn("username", Column.DataType.STRING);

        final List<String> columns0 = db.getTable(randomTable)
                .getColumns()
                .stream()
                .map(Column::getName)
                .collect(Collectors.toList());

        assertEquals(Lists.newArrayList("id", "username"), columns0);

        // Execute a migration here, which adds a new password column to the existing user_XXX table
        Main.migrate(db);

        final List<String> columns1 = db.getTable(randomTable)
                .getColumns()
                .stream()
                .map(Column::getName)
                .collect(Collectors.toList());

        assertEquals(Lists.newArrayList("id", "username", "password"), columns1);

        db.disconnect();
    }
}
