package com.turboboostteam.pos.dao.impl;

import com.turboboostteam.pos.dao.UserDao;
import com.turboboostteam.pos.jooq.tables.Users;
import com.turboboostteam.pos.jooq.tables.records.UsersRecord;
import com.turboboostteam.pos.model.user.Role;
import com.turboboostteam.pos.model.user.User;
import org.jooq.DSLContext;
import java.util.Optional;

public class UserDaoImpl implements UserDao {

    private final DSLContext dsl;

    public UserDaoImpl(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public Optional<User> findByUsername(String username) {
        UsersRecord record = dsl
                .selectFrom(Users.USERS)
                .where(Users.USERS.USERNAME.eq(username))
                .fetchOne();

        if (record == null) return Optional.empty();

        return Optional.of(new User(
                record.getId(),
                record.getUsername(),
                record.getPinHash(),
                Role.valueOf(record.getRole()),
                record.getActive()
        ));
    }

    @Override
    public void save(User user) {
        dsl.insertInto(Users.USERS)
                .set(Users.USERS.USERNAME, user.getUsername())
                .set(Users.USERS.PIN_HASH, user.getPinHash())
                .set(Users.USERS.ROLE, user.getRole().name())
                .set(Users.USERS.ACTIVE, user.isActive())
                .execute();
    }
}