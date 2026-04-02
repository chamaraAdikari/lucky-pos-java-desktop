package com.turboboostteam.pos.service;

import com.turboboostteam.pos.dao.UserDao;
import com.turboboostteam.pos.model.user.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.util.Optional;

public class UserService {

    private final UserDao userDao;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public UserService(UserDao userDao) {
        this.userDao = userDao;
    }

    public boolean login(String username, String pin) {
        Optional<User> userOpt = userDao.findByUsername(username);

        if (userOpt.isEmpty()) return false;

        User user = userOpt.get();

        if (!user.isActive()) return false;

        if (encoder.matches(pin, user.getPinHash())) {
            SessionManager.getInstance().login(user);
            return true;
        }
        return false;
    }

    public void logout() {
        SessionManager.getInstance().logout();
    }
}