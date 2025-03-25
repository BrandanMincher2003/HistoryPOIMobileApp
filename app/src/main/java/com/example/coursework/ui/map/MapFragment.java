package com.example.coursework.ui.map;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.appcompat.widget.SearchView;

import com.example.coursework.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


// Map fragment for displaying the google api map
public class MapFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private FirebaseFirestore db;
    private double passedLat = 0.0, passedLon = 0.0;
    private boolean hasZoomTarget = false;
    private SearchView searchView;

    public MapFragment() {}

    // inflates fragment layout and sets up the search view
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        db = FirebaseFirestore.getInstance();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());

        // intialise searchview
        searchView = view.findViewById(R.id.search_view);
        searchView.setQueryHint("Search Locations");

        //sets the listener to listen for query changes for filtering
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchLocation(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // filters as you type
                searchLocation(newText);
                return false;
            }
        });

        // get arguments passed from PlaceDetailsFragment for zooming in
        Bundle args = getArguments();
        if (args != null) {
            if (args.containsKey("latitude") && args.containsKey("longitude")) {
                try {
                    passedLat = args.getDouble("latitude");
                    passedLon = args.getDouble("longitude");
                    hasZoomTarget = true;
                    Log.d("MapFragment", "Received lat/lon from bundle: " + passedLat + ", " + passedLon);
                } catch (Exception e) {
                    Log.e("MapFragment", "Failed to parse lat/lon from bundle", e);
                }
            }
        }
    // initialises support fragment for map operations
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        return view;
    }

    // method for ready map
    @SuppressLint("MissingPermission")
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // checks permissions before using location
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }
        // zooms if has target if not use location is the zoom
        if (hasZoomTarget && passedLat != 0.0 && passedLon != 0.0) {
            Toast.makeText(requireContext(), "Zooming to selected location", Toast.LENGTH_SHORT).show();

            LatLng placeLatLng = new LatLng(passedLat, passedLon);
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(placeLatLng)
                    .zoom(16f)
                    .build();
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

            mMap.addMarker(new MarkerOptions()
                    .position(placeLatLng)
                    .title("Selected Location")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        } else {
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(userLatLng)
                            .zoom(13f)
                            .build();
                    mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                }
            });
        }

        loadLocationsFromFirestore();
    }

    // loads the location of locations from the firestore as well as name and city
    private void loadLocationsFromFirestore() {
        CollectionReference locationsRef = db.collection("Locations");

        locationsRef.get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                String name = document.getString("Name");
                String city = document.getString("City");
                String latStr = document.getString("Latitude");
                String lonStr = document.getString("Longitude");

                if (latStr != null && lonStr != null) {
                    try {
                        double latitude = Double.parseDouble(latStr);
                        double longitude = Double.parseDouble(lonStr);
                        LatLng location = new LatLng(latitude, longitude);
                        addDefaultMarker(name, city, location);
                    } catch (NumberFormatException e) {
                        Log.e("FirestoreError", "Invalid coordinates", e);
                    }
                }
            }
        }).addOnFailureListener(e ->
                Toast.makeText(requireContext(), "Failed to load locations", Toast.LENGTH_SHORT).show());
    }

    // adds a green marker to locations
    private void addDefaultMarker(String name, String city, LatLng location) {
        mMap.addMarker(new MarkerOptions()
                .position(location)
                .title(name)
                .snippet(city)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
    }

    private void searchLocation(String query) {
        CollectionReference locationsRef = db.collection("Locations");

        // performs a case-insensitive search for a location name that contains the query
        locationsRef.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        String name = document.getString("Name");
                        if (name != null && name.toLowerCase().contains(query.toLowerCase())) {
                            String city = document.getString("City");
                            double latitude = Double.parseDouble(document.getString("Latitude"));
                            double longitude = Double.parseDouble(document.getString("Longitude"));

                            LatLng location = new LatLng(latitude, longitude);
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 16));
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(requireContext(), "Location not found", Toast.LENGTH_SHORT).show());
    }
}
