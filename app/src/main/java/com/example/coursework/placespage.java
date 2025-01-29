package com.example.coursework;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.view.MenuItem;
import android.widget.Toast;

public class placespage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_placespage);

        // Find Bottom Navigation View
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);

        // Set Listener for Bottom Navigation Clicks (Material 3 Fix)
        bottomNav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.nav_home) {
                    Toast.makeText(placespage.this, "Home Clicked", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (itemId == R.id.nav_search) {
                    Toast.makeText(placespage.this, "Search Clicked", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (itemId == R.id.nav_favorites) {
                    Toast.makeText(placespage.this, "Favorites Clicked", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (itemId == R.id.nav_map) {
                    Toast.makeText(placespage.this, "Map Clicked", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (itemId == R.id.nav_profile) {
                    Toast.makeText(placespage.this, "Profile Clicked", Toast.LENGTH_SHORT).show();
                    return true;
                }
                return false;
            }
        });
    }
}
