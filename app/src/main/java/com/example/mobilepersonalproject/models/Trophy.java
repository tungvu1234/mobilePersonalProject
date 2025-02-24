package com.example.mobilepersonalproject.models;

public class Trophy {
    private String name;
    private String description;
    private String imageUrl;

    public Trophy() { }

    public Trophy(String name, String description, String imageUrl) {
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getImageUrl() { return imageUrl; }
}
