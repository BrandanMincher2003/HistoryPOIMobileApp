package com.example.coursework.ui.places;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.coursework.R;
import com.example.coursework.ui.carousel.ImageAdapter;
import com.example.coursework.ui.carousel.PlaceItem;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class PlacesFragment extends Fragment {

    private RecyclerView recyclerViewLocal;
    private ImageAdapter localAdapter;
    private List<PlaceItem> localItems;
    private FirebaseFirestore db;
    private FusedLocationProviderClient fusedLocationClient;
    private double userLatitude = 0.0, userLongitude = 0.0; // Default in case location is unavailable

    public PlacesFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_places, container, false);

        recyclerViewLocal = view.findViewById(R.id.recyclerViewLocal);
        recyclerViewLocal.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        localItems = new ArrayList<>();
        localAdapter = new ImageAdapter(getContext(), localItems);
        recyclerViewLocal.setAdapter(localAdapter);

        // Initialize Firestore and Location Services
        db = FirebaseFirestore.getInstance();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());

        // Get real-time user location
        requestUserLocation();

        return view;
    }

    /**
     * Requests the user's real-time location and updates `userLatitude` and `userLongitude`
     */
    @SuppressLint("MissingPermission")
    private void requestUserLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001);
            return;
        }

        // Request a single last known location
        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                userLatitude = location.getLatitude();
                userLongitude = location.getLongitude();
                fetchLocationsFromFirestore(); // Fetch places only after getting user location
            } else {
                // Request live location updates if last location is unavailable
                requestNewLocationData();
            }
        }).addOnFailureListener(e -> requestNewLocationData());
    }

    /**
     * Requests a fresh location update if no last known location is available
     */
    @SuppressLint("MissingPermission")
    private void requestNewLocationData() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000); // Update every 5 seconds
        locationRequest.setFastestInterval(2000); // Minimum time interval for location updates


        fusedLocationClient.requestLocationUpdates(locationRequest, new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult != null && !locationResult.getLocations().isEmpty()) {
                    Location location = locationResult.getLastLocation();
                    if (location != null) {
                        userLatitude = location.getLatitude();
                        userLongitude = location.getLongitude();
                        fetchLocationsFromFirestore();
                        fusedLocationClient.removeLocationUpdates(this);
                    }
                }
            }
        }, Looper.getMainLooper());
    }

    /**
     * Fetches places from Firestore and calculates distance to the user
     */
    private void fetchLocationsFromFirestore() {
        db.collection("Locations").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                localItems.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String name = document.getString("Name");
                    String imageUrl = document.getString("Image");

                    // Ensure latitude and longitude are numbers, not strings
                    double latitude = document.contains("Latitude") && document.get("Latitude") instanceof Number
                            ? document.getDouble("Latitude") : 0.0;
                    double longitude = document.contains("Longitude") && document.get("Longitude") instanceof Number
                            ? document.getDouble("Longitude") : 0.0;

                    if (name != null && imageUrl != null) {
                        double distance = calculateDistance(userLatitude, userLongitude, latitude, longitude);
                        String distanceText = String.format("%.1f miles", distance);

                        localItems.add(new PlaceItem(imageUrl, name, distanceText));
                    }
                }
                localAdapter.notifyDataSetChanged();
            }
        });
    }

    /**
     * Uses the Haversine formula to calculate the distance between two locations
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radius of the Earth in km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c; // Convert to kilometers
        return distance * 0.621371; // Convert to miles
    }

    /**
     * Handles the user's permission request response
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1001 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            requestUserLocation(); // Retry fetching location after permission is granted
        }
    }
}
