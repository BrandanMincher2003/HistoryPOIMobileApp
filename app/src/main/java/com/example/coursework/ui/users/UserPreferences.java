package com.example.coursework.ui.users;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;
import com.google.firebase.firestore.FirebaseFirestore;

public class UserPreferences {
    private static final String PREF_NAME = "user_prefs";
    private static final String KEY_DARK_MODE = "darkMode";
    private SharedPreferences sharedPreferences;
    private FirebaseFirestore db;
    private String userId;

    public UserPreferences(Context context, String userId) {
        this.sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.db = FirebaseFirestore.getInstance();
        this.userId = userId;
    }

    public void setDarkMode(boolean isDarkMode) {
        sharedPreferences.edit().putBoolean(KEY_DARK_MODE, isDarkMode).apply();
        AppCompatDelegate.setDefaultNightMode(isDarkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
        updateFirestore(isDarkMode);
    }

    public boolean isDarkModeEnabled() {
        return sharedPreferences.getBoolean(KEY_DARK_MODE, false); // Default to Light Mode
    }

    private void updateFirestore(boolean isDarkMode) {
        db.collection("users").document(userId)
                .update(KEY_DARK_MODE, isDarkMode)
                .addOnFailureListener(e -> e.printStackTrace());
    }

    public void loadFromFirestore() {
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Boolean isDarkMode = documentSnapshot.getBoolean(KEY_DARK_MODE);
                        if (isDarkMode != null) {
                            setDarkMode(isDarkMode); // Sync with local storage
                        } else {
                            setDarkMode(false); // Default to Light Mode if preference is missing
                        }
                    } else {
                        setDarkMode(false); // Default to Light Mode if no document exists
                    }
                })
                .addOnFailureListener(e -> e.printStackTrace());
    }
}
