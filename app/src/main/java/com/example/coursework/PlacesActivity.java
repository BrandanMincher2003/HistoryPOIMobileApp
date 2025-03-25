package com.example.coursework;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.example.coursework.databinding.ActivityPlacesBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;


// the main acitivty where all the fragments are binded onto it holds the bottom navbar and toolbar
public class PlacesActivity extends AppCompatActivity {

    private ActivityPlacesBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityPlacesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        // finds the navController correctly
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment == null) {
            throw new IllegalStateException("NavHostFragment not found! Check activity_places.xml.");
        }

        NavController navController = navHostFragment.getNavController();

        // defines the  top-level navigation fragments max 5 for m3 guidelines
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_gallery,
                R.id.nav_trophies,
                R.id.nav_profile,
                R.id.nav_map,
                R.id.nav_search
        ).build();

        // ensures that the toolbar is set up
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(bottomNav, navController);
    }
    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return navController.navigateUp() || super.onSupportNavigateUp();
    }
}