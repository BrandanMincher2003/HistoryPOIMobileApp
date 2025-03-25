package com.example.coursework.ui.achievements;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class StatsManager {

    private static final String TAG = "StatsManager";
    private static final String NOTTINGHAM_FIELD = "NottinghamCount";
    private static final String CASTLE_FIELD = "CastleCount";
    private static final String TROPHIES_COLLECTION = "trophies";

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
    private final DocumentReference statsRef = db.collection("users").document(uid).collection("stats").document(uid);


     //Increments visit counters based on location and manages trophies.

    public void incrementNottinghamCountIfNeeded(String cityName, String placeName) {
        boolean isNottingham = "Nottingham".equalsIgnoreCase(cityName);
        boolean isCastle = "Nottingham Castle".equalsIgnoreCase(placeName);

        statsRef.get().addOnSuccessListener(documentSnapshot -> {
            long currentNottinghamCount = documentSnapshot.getLong(NOTTINGHAM_FIELD) != null ?
                    documentSnapshot.getLong(NOTTINGHAM_FIELD) : 0;
            long currentCastleCount = documentSnapshot.getLong(CASTLE_FIELD) != null ?
                    documentSnapshot.getLong(CASTLE_FIELD) : 0;

            Map<String, Object> updates = new HashMap<>();

            // Update CastleCount if visiting Nottingham Castle for the first time
            if (isCastle && currentCastleCount < 1) {
                updates.put(CASTLE_FIELD, 1);
            }
            if (isCastle) {
                updates.put(NOTTINGHAM_FIELD, FieldValue.increment(1));
            }

            // Update NottinghamCount if visiting anywhere in Nottingham
            if (isNottingham) {
                updates.put(NOTTINGHAM_FIELD, FieldValue.increment(1));
            }

            if (!updates.isEmpty()) {
                statsRef.set(updates, SetOptions.merge())
                        .addOnSuccessListener(aVoid -> Log.d(TAG, "Stats updated: " + updates))
                        .addOnFailureListener(e -> Log.w(TAG, "Failed to update stats", e));
            }

            // Handle trophy logic
            long anticipatedNottinghamCount = currentNottinghamCount + (isNottingham ? 1 : 0);
            updateTrophiesIfNeeded(anticipatedNottinghamCount, isCastle, currentCastleCount);

        }).addOnFailureListener(e -> Log.w(TAG, "Failed to retrieve stats document", e));
    }


    //Updates trophies if the user meets new achievement conditions.

    private void updateTrophiesIfNeeded(long nottinghamCount, boolean isCastle, long currentCastleCount) {
        checkNottinghamTrophies(nottinghamCount);
        if (isCastle && currentCastleCount < 1) {
            checkRobinHoodTrophy();
        }
    }

    private void checkNottinghamTrophies(long count) {
        switch ((int) count) {
            case 3:
                addTrophyToUser("Nottingham Traveller", "Visit 3 locations in Nottingham");
                break;
            case 5:
                addTrophyToUser("Nottingham Explorer", "Visit 5 historic locations in Nottingham");
                break;
            case 10:
                addTrophyToUser("Nottingham Historian", "Visit 10 historic locations in Nottingham");
                break;
        }
    }

    private void checkRobinHoodTrophy() {
        db.collection("users").document(uid).collection(TROPHIES_COLLECTION)
                .whereEqualTo("Name", "Robin Hood")
                .get()
                .addOnSuccessListener((QuerySnapshot querySnapshot) -> {
                    boolean alreadyHasTrophy = false;
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        alreadyHasTrophy = true;
                        break;
                    }

                    if (!alreadyHasTrophy) {
                        addTrophyToUser("Robin Hood", "Visit Nottingham Castle");
                    } else {
                        Log.d(TAG, "Robin Hood trophy already awarded");
                    }
                })
                .addOnFailureListener(e -> Log.w(TAG, "Failed to check Robin Hood trophy", e));
    }

     //Adds a trophy document to the user's trophies subcollection.

    private void addTrophyToUser(String name, String description) {
        Map<String, Object> trophyData = new HashMap<>();
        trophyData.put("Name", name);
        trophyData.put("Description", description);
        trophyData.put("Image", ""); // Optional: Image URL

        db.collection("users").document(uid).collection(TROPHIES_COLLECTION)
                .add(trophyData)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Trophy awarded: " + name))
                .addOnFailureListener(e -> Log.w(TAG, "Failed to award trophy: " + name, e));
    }
}
