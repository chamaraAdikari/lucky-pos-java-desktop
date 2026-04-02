package com.turboboostteam.pos.dao;

import com.turboboostteam.pos.model.user.User;
import java.util.Optional;

public interface UserDao {
    Optional<User> findByUsername(String username);
    void save(User user);
}