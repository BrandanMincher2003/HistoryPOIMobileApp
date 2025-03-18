package com.example.coursework.ui.gallery;

public class GalleryItem {
    private final String imageUrl;
    private final String locationName;
    private final String date;

    public GalleryItem(String imageUrl, String locationName, String date) {
        this.imageUrl = imageUrl;
        this.locationName = locationName;
        this.date = date;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getLocationName() {
        return locationName;
    }

    public String getDate() {
        return date;
    }
}
