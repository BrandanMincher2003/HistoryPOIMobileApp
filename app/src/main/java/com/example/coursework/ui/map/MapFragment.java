package com.example.coursework.ui.map;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.coursework.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.CameraPosition;


public class MapFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private FirebaseFirestore db;

    public MapFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize Map
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Initialize FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());

        return view;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Enable location if permissions are granted
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }
        // Zoom to user's current location
        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(userLatLng)
                        .zoom(13f)
                        .build();
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }
        });

        // Load locations from Firestore and add markers
        loadLocationsFromFirestore();
    }

    private void loadLocationsFromFirestore() {
        CollectionReference locationsRef = db.collection("Locations");

        locationsRef.get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                String name = document.getString("Name");
                String city = document.getString("City");

                // Fetch latitude & longitude as strings and convert safely
                String latStr = document.getString("Latitude");
                String lonStr = document.getString("Longitude");

                if (latStr != null && lonStr != null) {
                    try {
                        double latitude = Double.parseDouble(latStr);
                        double longitude = Double.parseDouble(lonStr);
                        LatLng location = new LatLng(latitude, longitude);

                        // Add green default marker
                        addDefaultMarker(name, city, location);

                    } catch (NumberFormatException e) {
                        Log.e("FirestoreError", "Invalid latitude/longitude format: " + latStr + ", " + lonStr, e);
                    }
                } else {
                    Log.e("FirestoreError", "Missing latitude/longitude fields in Firestore document: " + document.getId());
                }
            }
        }).addOnFailureListener(e ->
                Toast.makeText(requireContext(), "Failed to load locations", Toast.LENGTH_SHORT).show());
    }

    private void addDefaultMarker(String name, String city, LatLng location) {
        mMap.addMarker(new MarkerOptions()
                .position(location)
                .title(name)
                .snippet(city)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
    }
}
