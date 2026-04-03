package com.turboboostteam.pos.dao;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.flywaydb.core.Flyway;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.BeforeAll;

import javax.sql.DataSource;

// All DAO tests extend this — avoids repeating setup code
public abstract class BaseDbTest {

    protected static DSLContext dsl;
    protected static DataSource dataSource;

    @BeforeAll
    static void initDb() {
        HikariConfig config = new HikariConfig();
        // Unique DB name per test class prevents conflicts
        config.setJdbcUrl("jdbc:h2:mem:testdb_"
                + System.currentTimeMillis()
                + ";DB_CLOSE_DELAY=-1");
        config.setUsername("sa");
        config.setPassword("");
        dataSource = new HikariDataSource(config);

        // Run all migrations
        Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .load()
                .migrate();

        dsl = DSL.using(dataSource, SQLDialect.H2);
    }
}