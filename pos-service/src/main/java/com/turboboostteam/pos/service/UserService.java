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

        if (userOpt.isEmpty()) {
            AuditLogger.loginFailed(username);  // ← ADD
            return false;
        }

        User user = userOpt.get();

        if (!user.isActive()) {
            AuditLogger.loginFailed(username);  // ← ADD
            return false;
        }

        if (encoder.matches(pin, user.getPinHash())) {
            SessionManager.getInstance().login(user);
            AuditLogger.loginSuccess(username); // ← ADD
            return true;
        }

        AuditLogger.loginFailed(username);      // ← ADD
        return false;
    }

    public void logout() {
        String username = SessionManager.getInstance()
                .getCurrentUser()
                .getUsername();
        AuditLogger.logout(username);           // ← ADD
        SessionManager.getInstance().logout();
    }
}