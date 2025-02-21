package com.example.coursework;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;

import android.view.MenuItem;

public class profilepage extends AppCompatActivity {

    private static final String PREFS_NAME = "ThemePrefs";
    private static final String KEY_IS_DARK_MODE = "isDarkMode";
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Apply the saved theme
        applySavedTheme();

        setContentView(R.layout.activity_profilepage);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Find Dark Mode Switch
        SwitchMaterial darkModeSwitch = findViewById(R.id.dark_mode_switch);

        // Initialise switch based on saved preference
        darkModeSwitch.setChecked(isDarkModeEnabled());

        // Set listener for the dark mode switch
        darkModeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // Enable dark mode
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                } else {
                    // Enable light mode
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                }
                saveDarkModePreference(isChecked);
            }
        });

        // Find Logout Button
        MaterialButton logoutButton = findViewById(R.id.logout_button);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logoutUser();
            }
        });

        // Find Bottom Navigation View
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);

        // Ensure "Profile" is selected by default when on this page
        bottomNav.setSelectedItemId(R.id.nav_profile);

        // Set listener for Bottom Navigation
        bottomNav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                // Prevent reopening the same page
                if (itemId == R.id.nav_profile) {
                    return true; // Already on Profile Page
                }

                if (itemId == R.id.nav_gallery) {
                    Log.d("NAVIGATION", "Navigating to Gallery Page");
                    startActivity(new Intent(profilepage.this, gallerypage.class));
                    finish(); // Close current activity
                    return true;
                } else if (itemId == R.id.nav_trophies) {
                    Log.d("NAVIGATION", "Navigating to Trophies Page");
                    startActivity(new Intent(profilepage.this, trophiespage.class));
                    finish(); // Close current activity
                    return true;
                } else if (itemId == R.id.nav_search) {
                    Log.d("NAVIGATION", "Navigating to Places Page");
                    startActivity(new Intent(profilepage.this, placespage.class));
                    finish(); // Close current activity
                    return true;
                }
                return false;
            }
        });
    }

    private void logoutUser() {
        mAuth.signOut();
        Toast.makeText(profilepage.this, "Logged out successfully!", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(profilepage.this, loginpage.class);
        startActivity(intent);
        finish(); // Close current page
    }

    /**
     * Saves the dark mode preference.
     */
    private void saveDarkModePreference(boolean isDarkMode) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY_IS_DARK_MODE, isDarkMode);
        editor.apply();
    }

    /**
     * Checks if dark mode is enabled based on saved preferences.
     */
    private boolean isDarkModeEnabled() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getBoolean(KEY_IS_DARK_MODE, false); // Default to false (light mode)
    }

    /**
     * Applies the saved theme.
     */
    private void applySavedTheme() {
        if (isDarkModeEnabled()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }
}
