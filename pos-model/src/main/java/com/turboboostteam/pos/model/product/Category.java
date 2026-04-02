package com.turboboostteam.pos.model.product;

public class Category {
    private Long id;
    private String name;
    private String description;

    public Category(Long id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public Long getId()          { return id; }
    public String getName()      { return name; }
    public String getDescription(){ return description; }

    @Override
    public String toString() { return name; }
}