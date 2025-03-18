package com.example.coursework.ui.gallery;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class FirebaseGalleryHelper {

    private final FirebaseFirestore db;
    private final CollectionReference galleryCollection;

    public FirebaseGalleryHelper() {
        db = FirebaseFirestore.getInstance();
        galleryCollection = db.collection("gallery");
    }

    // Fetch all locations from gallery/{documentId}/Locations
    public void fetchGalleryItems(Consumer<List<GalleryItem>> callback) {
        galleryCollection.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                List<GalleryItem> galleryItems = new ArrayList<>();
                AtomicInteger pendingQueries = new AtomicInteger(task.getResult().size()); // Track queries

                if (pendingQueries.get() == 0) {
                    callback.accept(galleryItems); // No documents found
                    return;
                }

                for (QueryDocumentSnapshot galleryDoc : task.getResult()) {
                    CollectionReference locationsCollection = galleryDoc.getReference().collection("Locations");
                    fetchLocations(locationsCollection, galleryItems, pendingQueries, callback);
                }
            } else {
                callback.accept(new ArrayList<>()); // Return empty if failed
            }
        });
    }

    // Helper function to fetch images from the Locations subcollection
    private void fetchLocations(CollectionReference locationCollection, List<GalleryItem> galleryItems, AtomicInteger pendingQueries, Consumer<List<GalleryItem>> callback) {
        locationCollection.get().addOnCompleteListener(locationTask -> {
            if (locationTask.isSuccessful() && locationTask.getResult() != null) {
                for (QueryDocumentSnapshot locationDoc : locationTask.getResult()) {
                    String imageUrl = locationDoc.getString("Image");
                    String title = locationDoc.getString("Name");
                    String date = locationDoc.getString("Date");

                    if (imageUrl != null && title != null && date != null) {
                        galleryItems.add(new GalleryItem(imageUrl, title, date));
                    }
                }
            }

            // Reduce pending queries, update UI only when all subcollections finish
            if (pendingQueries.decrementAndGet() == 0) {
                callback.accept(galleryItems);
            }
        });
    }
}
