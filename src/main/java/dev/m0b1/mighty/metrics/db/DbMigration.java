package dev.m0b1.mighty.metrics.db;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

/**
 * Handles database migrations by running the necessary SQL statements in a specific order.
 *
 * Why aren't you using Liquibase or Flyway?
 *  - This is not an enterprise with developers merging on top of each other. Also, this is faster, and more flexible.
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class DbMigration implements SmartInitializingSingleton {

  private final JdbcTemplate jdbcTemplate;

  /**
   * Enable Write-Ahead Logging before the transactional migrations run below.
   *
   * SQLite does not allow WAL to be enabled inside a transaction.
   */
  @PostConstruct
  public void postConstruct() {
    jdbcTemplate.execute("PRAGMA journal_mode = WAL;");
  }

  /**
   * Run any migrations from the resources directory that have not been applied yet.
   *
   * The use of SmartInitializingSingleton override ensures that WAL setup above has already finished.
   */
  @Override
  @Transactional
  public void afterSingletonsInstantiated() {

    var schemaVersion = fetchSchemaVersion();
    var migrations = buildMigrationsOrder();

    for (int i = 0; i < migrations.size(); i++) {

      var migrationCounter = i + 1;
      var migrationFilePath = migrations.get(i);

      if (schemaVersion < migrationCounter) {

        try {
          runMigration(migrationFilePath);
        } catch (Exception e) {
          log.warn("Could not run migration file: {}", migrationFilePath);
          log.error("Exception occurred while running migration", e);
          throw new RuntimeException(e);
        }

        setSchemaVersion(migrationCounter);

        schemaVersion = migrationCounter;
      }
    }
  }

  private Integer fetchSchemaVersion() {
    return jdbcTemplate.queryForObject("PRAGMA user_version;", Integer.class);
  }

  private void setSchemaVersion(Integer version) {
    var sql = String.format("PRAGMA user_version = %d;", version);
    jdbcTemplate.execute(sql);
  }

  private void runMigration(String migrationFilePath) throws Exception {
    log.info("Running migration: {}", migrationFilePath);
    var sql = readMigrationFile(migrationFilePath);
    jdbcTemplate.execute(sql);
  }

  private String readMigrationFile(String migrationFilePath) throws Exception {

      var classLoader = DbMigration.class.getClassLoader();
      var resource = classLoader.getResource("migrations/" + migrationFilePath + ".sql");

      assert resource != null;
      var path = Paths.get(resource.getPath());
      return Files.readString(path);
  }

  private List<String> buildMigrationsOrder() {
    var migrations = new LinkedList<String>();
    migrations.add("001-create-coach-table");
    migrations.add("002-populate-coach-table");
    migrations.add("003-create-score-table");
    migrations.add("004-populate-score-table");
    migrations.add("005-create-member-table");
    migrations.add("006-index-member-table");
    migrations.add("007-create-scorecard-table");
    migrations.add("008-index-scorecard-table");
    migrations.add("009-create-exercise-table");
    migrations.add("010-index-exercise-table");
    return migrations;
  }

}
