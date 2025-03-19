package com.example.coursework.model;

public class TrophyItem {
    private String name;
    private String description;
    private boolean achieved;

    public TrophyItem(String name, String description, boolean achieved) {
        this.name = name;
        this.description = description;
        this.achieved = achieved;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public boolean isAchieved() {
        return achieved;
    }
}
