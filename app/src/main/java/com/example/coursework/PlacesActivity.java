package com.example.coursework;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.example.coursework.databinding.ActivityPlacesBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class PlacesActivity extends AppCompatActivity {

    private ActivityPlacesBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityPlacesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // **Set up ActionBar (Fix)**
        setSupportActionBar(binding.toolbar); // Ensure you have a <androidx.appcompat.widget.Toolbar> in your layout

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        // Find NavController correctly
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment == null) {
            throw new IllegalStateException("NavHostFragment not found! Check activity_places.xml.");
        }

        NavController navController = navHostFragment.getNavController();

        // Define top-level destinations
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_gallery,
                R.id.nav_trophies,
                R.id.nav_profile,
                R.id.nav_map,
                R.id.nav_search
        ).build();

        // **Ensure Toolbar is set up correctly**
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(bottomNav, navController);
    }
}