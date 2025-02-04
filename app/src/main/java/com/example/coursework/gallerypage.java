package com.example.coursework;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import android.view.MenuItem;

public class gallerypage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallerypage);

        // Find Bottom Navigation View
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);

        // Ensure "Gallery" is selected by default when on this page
        bottomNav.setSelectedItemId(R.id.nav_gallery);

        // Set listener for Bottom Navigation
        bottomNav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                // Prevent reopening the same page
                if (itemId == R.id.nav_gallery) {
                    return true; // Already on Gallery Page
                }

                if (itemId == R.id.nav_trophies) {
                    Log.d("NAVIGATION", "Navigating to Trophies Page");
                    startActivity(new Intent(gallerypage.this, trophiespage.class));
                    finish(); // Close current activity
                    return true;
                } else if (itemId == R.id.nav_profile) {
                    Log.d("NAVIGATION", "Navigating to Profile Page");
                    startActivity(new Intent(gallerypage.this, profilepage.class));
                    finish(); // Close current activity
                    return true;
                } else if (itemId == R.id.nav_search) {
                    Log.d("NAVIGATION", "Navigating to Places Page");
                    startActivity(new Intent(gallerypage.this, placespage.class));
                    finish(); // Close current activity
                    return true;
                }
                return false;
            }
        });
    }
}
