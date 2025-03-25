package com.example.coursework.ui.users;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;
import com.google.firebase.firestore.FirebaseFirestore;


// public class for user preferences such as dark mode, connects with the db to update it to selected options
public class UserPreferences {
    private static final String PREF_NAME = "user_prefs";
    private static final String KEY_DARK_MODE = "darkMode";
    private SharedPreferences sharedPreferences;
    private FirebaseFirestore db;
    private String userId;

    // it shares prefs with firebase to sqnc the prefs
    public UserPreferences(Context context, String userId) {
        this.sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.db = FirebaseFirestore.getInstance();
        this.userId = userId;
    }

    // sets darkmode to shared prefs and firebase
    public void setDarkMode(boolean isDarkMode) {
        sharedPreferences.edit().putBoolean(KEY_DARK_MODE, isDarkMode).apply();
        AppCompatDelegate.setDefaultNightMode(isDarkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
        updateFirestore(isDarkMode);
    }

    // checks if darkmode is on (this isnt used anymore)
    public boolean isDarkModeEnabled() {
        return sharedPreferences.getBoolean(KEY_DARK_MODE, false); // Default to Light Mode
    }

    // updates firebase witbh darkmode setting
    private void updateFirestore(boolean isDarkMode) {
        db.collection("users").document(userId)
                .update(KEY_DARK_MODE, isDarkMode)
                .addOnFailureListener(e -> e.printStackTrace());
    }

    // loads darkmode pref from firebase
    public void loadFromFirestore() {
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Boolean isDarkMode = documentSnapshot.getBoolean(KEY_DARK_MODE);
                        if (isDarkMode != null) {
                            setDarkMode(isDarkMode);
                        } else {
                            setDarkMode(false);
                        }
                    } else {
                        setDarkMode(false);
                    }
                })
                .addOnFailureListener(e -> e.printStackTrace());
    }
}
