package com.turboboostteam.pos.dao;

import com.turboboostteam.pos.dao.impl.UserDaoImpl;
import com.turboboostteam.pos.model.user.Role;
import com.turboboostteam.pos.model.user.User;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.flywaydb.core.Flyway;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

class UserDaoTest {

    static UserDao userDao;

    @BeforeAll
    static void setup() {
        // In-memory H2 for tests
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
        config.setUsername("sa");
        config.setPassword("");
        DataSource ds = new HikariDataSource(config);

        // Run migrations
        Flyway.configure()
                .dataSource(ds)
                .locations("classpath:db/migration")
                .load()
                .migrate();

        DSLContext dsl = DSL.using(ds, SQLDialect.H2);
        userDao = new UserDaoImpl(dsl);
    }

    @Test
    void adminUserExists() {
        Optional<User> user = userDao.findByUsername("admin");
        assertThat(user).isPresent();
        assertThat(user.get().getRole()).isEqualTo(Role.ADMIN);
        assertThat(user.get().isActive()).isTrue();
    }

    @Test
    void unknownUserReturnsEmpty() {
        Optional<User> user = userDao.findByUsername("nobody");
        assertThat(user).isEmpty();
    }
}