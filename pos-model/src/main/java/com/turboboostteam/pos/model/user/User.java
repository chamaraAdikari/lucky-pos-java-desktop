package com.turboboostteam.pos.model.user;

public class User {
    private Long id;
    private String username;
    private String pinHash;
    private Role role;
    private boolean active;

    // Constructor
    public User(Long id, String username, String pinHash, Role role, boolean active) {
        this.id = id;
        this.username = username;
        this.pinHash = pinHash;
        this.role = role;
        this.active = active;
    }

    // Getters only — no setters (encapsulation)
    public Long getId()         { return id; }
    public String getUsername() { return username; }
    public String getPinHash()  { return pinHash; }
    public Role getRole()       { return role; }
    public boolean isActive()   { return active; }
}