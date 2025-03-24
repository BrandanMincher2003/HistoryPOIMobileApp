package com.example.coursework.ui.trophies;

public class TrophyItem {
    private String name;
    private String description;
    private boolean achieved;
    private String imagePath;

    public TrophyItem(String name, String description, boolean achieved, String imagePath) {
        this.name = name;
        this.description = description;
        this.achieved = achieved;
        this.imagePath = imagePath;
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

    public String getImagePath() {
        return imagePath;
    }
}
