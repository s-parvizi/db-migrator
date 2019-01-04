package com.soheil.db.migrator.migrator;

import com.soheil.db.migrator.dbapi.Column;
import com.soheil.db.migrator.dbapi.Database;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Executes the callbacks for a specific event.
 */

public class Migrator {

    private static final Logger LOGGER = LoggerFactory.getLogger(Migrator.class);

    public static void migrate(File file, Database database, boolean schemaHistoryExist) throws SQLException, IOException {

        LOGGER.info("Start Migration of File {}", file.getName());

        if (schemaHistoryExist) {

            if (database.getTable("schema_history").findAllBy("version", file.getName()).isEmpty()) {

                long startTime = System.currentTimeMillis();

                // Parse file and Change Database
                List<String> lines = Files.readAllLines(file.toPath());

                for (String line : lines) {
                    parseAndExecute(line, database);
                }

                long stopTime = System.currentTimeMillis();
                long elapsedTime = stopTime - startTime;

                parseAndExecute("ADD ROW schema_history version installed_on execution_time#"
                        + file.getName() + " "
                        + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + " "
                        + elapsedTime, database);

            }

        } else {

            // Create schema_history Table

            List<String> lines = Files.readAllLines(file.toPath());

            for (String line : lines) {
                parseAndExecute(line, database);
            }

        }

    }

    private static void parseAndExecute(String request, Database database) throws SQLException {

        LOGGER.info("parseAndExecute for request {}", request);

        String[] parts = request.split(" ");

        if (parts[1].equalsIgnoreCase("TABLE")) {

            database.addTable(parts[2]);

        } else if (parts[1].equalsIgnoreCase("COLUMN")) {

            //database.getTable(parts[2]).addColumn(parts[3], Column.DataType.valueOf(parts[4]));
            database.getTables().stream().filter(t -> t.getName().matches(parts[2])).forEach(t -> {
                try {
                    t.addColumn(parts[3], Column.DataType.valueOf(parts[4]));
                } catch (SQLException e) {
                    throw new MigrationException("ParseAndExecute Exception:", e);
                }
            });

        } else if (parts[1].equalsIgnoreCase("ROW")) {

            String[] mainParts = request.split("#");
            String[] keys = mainParts[0].split(" ");
            String[] values = mainParts[1].split(" ");

            Map<String, Object> rows = new TreeMap<>();

            for (int i = 3; i < values.length + 3; i++) {
                rows.put(keys[i], values[i - 3]);
            }

            database.getTable(parts[2]).insert(rows);

        }

        // other data parsing can be added simply

    }

}
