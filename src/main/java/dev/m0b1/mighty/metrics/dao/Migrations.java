package dev.m0b1.mighty.metrics.dao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

/**
 * The Migrations class handles database migrations by running the necessary SQL statements in a specific order.
 *
 * Why aren't you using Liquibase or Flyway?
 *  - This is not an enterprise with developers merging on top of each other. Also, this is faster, and more flexible.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class Migrations implements SmartInitializingSingleton {

  private final JdbcTemplate jdbcTemplate;

  /**
   * Enable Write-Ahead Logging outside the scope of the transactional migrations below.
   *
   * SQLite does not allow WAL to be enabled inside a transaction.
   */
  @PostConstruct
  public void postConstruct() {
    ensureJournalModeWAL();
  }

  @Override
  @Transactional
  public void afterSingletonsInstantiated() {

    var schemaVersion = fetchSchemaVersion();
    var migrations = buildMigrationsOrder();

    for (int i = 0; i < migrations.size(); i++) {

      var migrationCounter = i + 1;
      var migrationFilePath = migrations.get(i);

      if (schemaVersion < migrationCounter) {

        runMigration(migrationFilePath);
        setSchemaVersion(migrationCounter);

        schemaVersion = migrationCounter;
      }
    }
  }

  private void ensureJournalModeWAL() {
    jdbcTemplate.execute("PRAGMA journal_mode = WAL;");
  }

  private Integer fetchSchemaVersion() {
    return jdbcTemplate.queryForObject("PRAGMA user_version;", Integer.class);
  }

  private void setSchemaVersion(Integer version) {
    var sql = String.format("PRAGMA user_version = %d;", version);
    jdbcTemplate.execute(sql);
  }

  private void runMigration(String migrationFilePath) {
    log.info("Running migration: {}", migrationFilePath);
    var sql = readMigrationFile(migrationFilePath);
    jdbcTemplate.execute(sql);
  }

  private String readMigrationFile(String migrationFilePath) {

    try {

      var classLoader = Migrations.class.getClassLoader();
      var resource = classLoader.getResource("migrations/" + migrationFilePath + ".sql");
      if (resource == null) {
        throw new RuntimeException("Resource file not found");
      }

      var path = Paths.get(resource.getPath());
      return Files.readString(path);

    } catch (Exception e) {
      log.error("Could not retrieve SQL migration file", e);
      throw new RuntimeException(e);
    }
  }

  private List<String> buildMigrationsOrder() {
    var migrations = new LinkedList<String>();
    migrations.add("001-create-coach-table");
    migrations.add("002-populate-coach-table");
    migrations.add("003-create-score-table");
    migrations.add("004-populate-score-table");
    migrations.add("005-create-scorecard-table");
    migrations.add("006-create-exercise-table");
    return migrations;
  }

}
