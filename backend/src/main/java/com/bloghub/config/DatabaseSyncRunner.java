package com.bloghub.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

//@Component
@Slf4j
public class DatabaseSyncRunner implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseSyncRunner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("Starting database schema synchronization...");

        try {
            // 1. Add missing columns to users table
            addColumnIfNotExists(jdbcTemplate, "users", "free_stories_remaining", "INTEGER DEFAULT 3");
            addColumnIfNotExists(jdbcTemplate, "users", "is_member", "BOOLEAN DEFAULT FALSE");
            addColumnIfNotExists(jdbcTemplate, "users", "subscription_type", "VARCHAR(255) DEFAULT 'FREE'");
            addColumnIfNotExists(jdbcTemplate, "users", "last_reset_date", "TIMESTAMP");
            addColumnIfNotExists(jdbcTemplate, "users", "subscription_end_date", "TIMESTAMP");

            // 2. Add missing columns to posts table
            addColumnIfNotExists(jdbcTemplate, "posts", "is_premium", "BOOLEAN DEFAULT FALSE");

            // 3. Ensure Activity and Achievement tables exist
            ensureTableExists(jdbcTemplate, "reading_activities",
                "CREATE TABLE reading_activities (" +
                "id BIGSERIAL PRIMARY KEY, " +
                "user_id BIGINT NOT NULL, " +
                "post_id BIGINT, " +
                "duration_minutes INTEGER, " +
                "read_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (user_id) REFERENCES users(id), " +
                "FOREIGN KEY (post_id) REFERENCES posts(id))");

            ensureTableExists(jdbcTemplate, "writing_activities",
                "CREATE TABLE writing_activities (" +
                "id BIGSERIAL PRIMARY KEY, " +
                "user_id BIGINT NOT NULL, " +
                "post_id BIGINT, " +
                "words_written INTEGER NOT NULL, " +
                "written_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (user_id) REFERENCES users(id), " +
                "FOREIGN KEY (post_id) REFERENCES posts(id))");

            ensureTableExists(jdbcTemplate, "user_achievements",
                "CREATE TABLE user_achievements (" +
                "id BIGSERIAL PRIMARY KEY, " +
                "user_id BIGINT NOT NULL, " +
                "achievement_type VARCHAR(255) NOT NULL, " +
                "earned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (user_id) REFERENCES users(id))");

            log.info("Database schema synchronization completed successfully.");
        } catch (Exception e) {
            log.error("Error during database schema synchronization: {}", e.getMessage());
        }
    }

    private void addColumnIfNotExists(JdbcTemplate jdbcTemplate, String tableName, String columnName, String columnDefinition) {
        String checkSql = "SELECT count(*) FROM information_schema.columns WHERE table_name = ? AND column_name = ?";
        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, tableName, columnName);
        if (count == null || count == 0) {
            log.info("Adding column {} to table {}", columnName, tableName);
            jdbcTemplate.execute("ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + columnDefinition);
        }
    }

    private void ensureTableExists(JdbcTemplate jdbcTemplate, String tableName, String createSql) {
        String checkSql = "SELECT count(*) FROM information_schema.tables WHERE table_name = ?";
        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, tableName);
        if (count == null || count == 0) {
            log.info("Creating table {}", tableName);
            jdbcTemplate.execute(createSql);
        }
    }
}