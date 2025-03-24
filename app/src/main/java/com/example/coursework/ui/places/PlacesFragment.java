package com.example.coursework.ui.places;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.coursework.R;
import com.example.coursework.ui.carousel.ImageAdapter;
import com.example.coursework.ui.carousel.PlaceItem;
import com.google.android.gms.location.*;
import com.google.android.material.search.SearchBar;
import com.google.android.material.search.SearchView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.*;

public class PlacesFragment extends Fragment {

    private LocationCallback locationCallback;
    private RecyclerView recyclerViewLocal, recyclerViewFavourites;
    private ImageAdapter localAdapter, favouritesAdapter;
    private List<PlaceItem> localItems = new ArrayList<>();
    private List<PlaceItem> filteredItems = new ArrayList<>();
    private List<PlaceItem> favouritesItems = new ArrayList<>();
    private Set<String> favouritePlaceNames = new HashSet<>();

    private FirebaseFirestore db;
    private FusedLocationProviderClient fusedLocationClient;
    private double userLatitude = 0.0, userLongitude = 0.0;
    private String currentUserId;

    private SearchBar searchBar;
    private SearchView searchView;

    public PlacesFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_places, container, false);

        recyclerViewLocal = view.findViewById(R.id.recyclerViewLocal);
        recyclerViewFavourites = view.findViewById(R.id.recyclerViewFavourites);

        recyclerViewLocal.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerViewFavourites.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        localAdapter = new ImageAdapter(getContext(), filteredItems, favouritePlaceNames, this::openPlaceDetails);
        favouritesAdapter = new ImageAdapter(getContext(), favouritesItems, favouritePlaceNames, this::openPlaceDetails);

        recyclerViewLocal.setAdapter(localAdapter);
        recyclerViewFavourites.setAdapter(favouritesAdapter);

        searchBar = view.findViewById(R.id.search_bar);
        searchView = new SearchView(requireContext());
        searchView.setHint("Search Locations");
        searchBar.setOnClickListener(v -> searchView.show());

        searchView.addTransitionListener((sv, prevState, newState) -> {
            if (newState == SearchView.TransitionState.HIDDEN) {
                searchBar.setText("");
                filteredItems.clear();
                filteredItems.addAll(localItems);
                localAdapter.notifyDataSetChanged();
            }

        });

        searchView.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterLocalPlaces(s.toString());
            }
        });

        db = FirebaseFirestore.getInstance();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        requestUserLocation();

        return view;
    }

    private void filterLocalPlaces(String query) {
        filteredItems.clear();
        for (PlaceItem item : localItems) {
            if (item.getName().toLowerCase().contains(query.toLowerCase())) {
                filteredItems.add(item);
            }
        }
        localAdapter.notifyDataSetChanged();
    }

    private void openPlaceDetails(PlaceItem item) {
        db.collection("Locations")
                .whereEqualTo("Name", item.getName())
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        DocumentSnapshot document = querySnapshot.getDocuments().get(0);
                        String city = document.getString("City");
                        String description = document.getString("Description");
                        String image = document.getString("Image");

                        Bundle bundle = new Bundle();
                        bundle.putString("name", item.getName());
                        bundle.putString("city", city);
                        bundle.putString("description", description);
                        bundle.putString("image", image);
                        bundle.putDouble("latitude", item.getLatitude());
                        bundle.putDouble("longitude", item.getLongitude());
                        NavController navController = NavHostFragment.findNavController(this);
                        navController.navigate(R.id.action_placesFragment_to_placeDetailsFragment, bundle);
                    }
                });
    }

    @SuppressLint("MissingPermission")
    private void requestUserLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1001);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                userLatitude = location.getLatitude();
                userLongitude = location.getLongitude();
                fetchLocationsFromFirestore();
                listenToFavouritesUpdates();
            } else {
                requestNewLocationData();
            }
        }).addOnFailureListener(e -> requestNewLocationData());
    }

    @SuppressLint("MissingPermission")
    private void requestNewLocationData() {
        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .setInterval(5000)
                .setFastestInterval(2000);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult != null && !locationResult.getLocations().isEmpty()) {
                    Location location = locationResult.getLastLocation();
                    if (location != null) {
                        userLatitude = location.getLatitude();
                        userLongitude = location.getLongitude();
                        fetchLocationsFromFirestore();
                        listenToFavouritesUpdates();
                    }
                }
            }
        };

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    private void fetchLocationsFromFirestore() {
        db.collection("Locations").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                localItems.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String name = document.getString("Name");
                    String imageUrl = document.getString("Image");
                    double latitude = parseDouble(document.get("Latitude"));
                    double longitude = parseDouble(document.get("Longitude"));

                    if (name != null && imageUrl != null) {
                        double distance = calculateDistance(userLatitude, userLongitude, latitude, longitude);
                        String distanceText = String.format("%.1f miles", distance);
                        localItems.add(new PlaceItem(imageUrl, name, distanceText, latitude, longitude));
                    }
                }

                localItems.sort(Comparator.comparingDouble(item ->
                        calculateDistance(userLatitude, userLongitude, item.getLatitude(), item.getLongitude())));

                filteredItems.clear();
                filteredItems.addAll(localItems);
                localAdapter.notifyDataSetChanged();
            }
        });
    }

    private void listenToFavouritesUpdates() {
        db.collection("users")
                .document(currentUserId)
                .collection("favourites")
                .addSnapshotListener((querySnapshot, e) -> {
                    if (e != null || querySnapshot == null) return;

                    favouritesItems.clear();
                    favouritePlaceNames.clear();

                    for (QueryDocumentSnapshot document : querySnapshot) {
                        String name = document.getString("Name");
                        String imageUrl = document.getString("Image");
                        double latitude = parseDouble(document.get("Latitude"));
                        double longitude = parseDouble(document.get("Longitude"));

                        if (name != null && imageUrl != null) {
                            favouritePlaceNames.add(name);
                            double distance = calculateDistance(userLatitude, userLongitude, latitude, longitude);
                            String distanceText = String.format("%.1f miles", distance);
                            favouritesItems.add(new PlaceItem(imageUrl, name, distanceText, latitude, longitude));
                        }
                    }

                    favouritesAdapter.notifyDataSetChanged();
                    localAdapter.notifyDataSetChanged(); // so hearts sync
                });
    }

    private double parseDouble(Object obj) {
        if (obj instanceof Double) return (double) obj;
        if (obj instanceof String) return Double.parseDouble((String) obj);
        return 0.0;
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c * 0.621371;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1001 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            requestUserLocation();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }
}
