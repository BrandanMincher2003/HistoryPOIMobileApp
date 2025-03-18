package com.example.coursework.ui.carousel;

public class PlaceItem {
    private String imageUrl;
    private String name;
    private String distance;

    public PlaceItem(String imageUrl, String name, String distance) {
        this.imageUrl = imageUrl;
        this.name = name;
        this.distance = distance;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getName() {
        return name;
    }

    public String getDistance() {
        return distance;
    }
}
