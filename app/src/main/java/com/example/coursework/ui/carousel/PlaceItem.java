package com.example.coursework.ui.carousel;

public class PlaceItem {
    private String imageUrl;
    private String name;
    private String distance;
    private double latitude;
    private double longitude;

    public PlaceItem(String imageUrl, String name, String distance) {
        this.imageUrl = imageUrl;
        this.name = name;
        this.distance = distance;
    }

    public PlaceItem(String imageUrl, String name, String distance, double latitude, double longitude) {
        this.imageUrl = imageUrl;
        this.name = name;
        this.distance = distance;
        this.latitude = latitude;
        this.longitude = longitude;
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

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
