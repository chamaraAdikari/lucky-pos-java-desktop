package com.turboboostteam.pos.service;

import com.turboboostteam.pos.model.user.User;

public class SessionManager {

    public enum State { IDLE, ACTIVE }

    private static SessionManager instance;
    private State state = State.IDLE;
    private User currentUser;

    // Singleton
    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) instance = new SessionManager();
        return instance;
    }

    public void login(User user) {
        this.currentUser = user;
        this.state = State.ACTIVE;
        System.out.println("✅ Session ACTIVE — " + user.getUsername()
                + " [" + user.getRole() + "]");
    }

    public void logout() {
        this.currentUser = null;
        this.state = State.IDLE;
        System.out.println("🔒 Session IDLE");
    }

    public boolean isActive()        { return state == State.ACTIVE; }
    public User getCurrentUser()     { return currentUser; }
    public State getState()          { return state; }
}