package com.soheil.db.migrator;

import com.soheil.db.migrator.dbapi.Database;
import com.soheil.db.migrator.dbapi.Table;
import com.soheil.db.migrator.migrator.MigrationException;
import com.soheil.db.migrator.migrator.Migrator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

public class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    private static final Path DB_PATH = Paths.get("dbs", "main");

    public static void main(String[] args) throws IOException, SQLException {
        Database db = Database.connect(DB_PATH);

        if (args[0].equals("migrate")) migrate(db);
        else if (args[0].equals("show_tables")) showTables();
        else if (args[0].equals("generate")) generate(args[1]);
        else {
            System.out.println("Unknown command " + args[0]);
            System.exit(1);
        }
        System.exit(0);
    }

    private static void showTables() throws SQLException {

        Database db = Database.connect(DB_PATH);
        System.out.println("Tables: ");
        db.getTables().forEach(System.out::println);

    }

    public static void migrate(Database db) throws SQLException {

        File dir = new File("src/main/resources/migrations/");
        File[] files = dir.listFiles((d, name) -> name.endsWith(".dbmgr"));

        // Check that schema_history Table is Exist
        if (db.getTables().stream().map(Table::getName).anyMatch(s -> s.equalsIgnoreCase("schema_history"))) {

            Arrays.stream(files).filter(f -> !f.getName().contains("schema_history")).forEach(f -> {
                try {
                    Migrator.migrate(f, db, true);
                } catch (SQLException | IOException e) {
                    throw new MigrationException("Migration Exception:", e);
                }
            });

        } else {

            // schema_history Table does not exist and should be created
            Arrays.stream(files)
                    .filter(f -> f.getName().contains("schema_history"))
                    .forEach(f -> {
                        try {
                            Migrator.migrate(f, db, false);
                        } catch (SQLException | IOException e) {
                            throw new MigrationException("Migration Exception:", e);
                        }
                    });

        }

    }

    public static void generate(String name) throws IOException {

        LOGGER.info("Generating new migration with name {}" , name);

        Path newFilePath = Paths.get("src/main/resources/migrations/" +
                new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + "-" +
                name + ".dbmgr");

        Files.createFile(newFilePath);

    }

}
