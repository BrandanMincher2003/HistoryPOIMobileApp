package com.example.coursework.ui.places;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import com.bumptech.glide.Glide;
import com.example.coursework.R;
import com.example.coursework.ui.achievements.StatsManager;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

// places details fragment displays the details of a specific place when clicked, allowing users to upload images and find the place on a map.
public class PlaceDetailsFragment extends Fragment {

    // declares variables for ui components and location handling
    private TextView nameTextView, cityTextView, descriptionTextView;
    private ImageView placeImageView;
    private FusedLocationProviderClient fusedLocationClient;
    private double latitude, longitude;

    // ActivityResultLauncher handles image selection from the gallery
    private final ActivityResultLauncher<Intent> galleryPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                // If an image is selected, upload it to Firebase
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri selectedImageUri = result.getData().getData();
                    if (selectedImageUri != null) {
                        uploadImageToFirebase(selectedImageUri);
                    }
                } else {
                    // Show a message if no image is selected
                    Toast.makeText(getContext(), "No image selected", Toast.LENGTH_SHORT).show();
                }
            });

    // basic constructor
    public PlaceDetailsFragment() {}

    // thiss method inflates the fragments layout and initialises the ui components
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // inflates the fragments layout
        View view = inflater.inflate(R.layout.fragment_place_details, container, false);

        // inits the ui components
        nameTextView = view.findViewById(R.id.textViewName);
        cityTextView = view.findViewById(R.id.textViewCity);
        descriptionTextView = view.findViewById(R.id.textViewDescription);
        placeImageView = view.findViewById(R.id.imageViewPlace);

        // finds the fabs for uploading images and finding the location
        View fabUpload = view.findViewById(R.id.fab_upload);
        View fabFindLocation = view.findViewById(R.id.fab_find_location);

        // inits the location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());

        // ets the click listeners for the fabs
        fabUpload.setOnClickListener(v -> openGalleryPicker());
        fabFindLocation.setOnClickListener(v -> {
            // preps the data for navigating to the map
            Bundle bundle = new Bundle();
            bundle.putDouble("latitude", latitude);
            bundle.putDouble("longitude", longitude);

            // navigates to the magfragmetn with the place's location data
            NavController navController = NavHostFragment.findNavController(PlaceDetailsFragment.this);
            NavOptions navOptions = new NavOptions.Builder().setPopUpTo(R.id.nav_search, true).build();
            navController.navigate(R.id.nav_map, bundle, navOptions);
        });

        // gets and display the place details from the arguments passed to the fragment
        if (getArguments() != null) {
            nameTextView.setText(getArguments().getString("name"));
            cityTextView.setText(getArguments().getString("city"));
            descriptionTextView.setText(getArguments().getString("description"));
            String imageUrl = getArguments().getString("image");

            // gets the latitude and longitude values from the arguments
            try {
                latitude = getArguments().containsKey("latitude") ? getArguments().getDouble("latitude") : Double.parseDouble(getArguments().getString("latitude"));
                longitude = getArguments().containsKey("longitude") ? getArguments().getDouble("longitude") : Double.parseDouble(getArguments().getString("longitude"));
            } catch (Exception e) {
                // shows an error message if the latitude or longitude is invalid
                Toast.makeText(getContext(), "Invalid latitude or longitude", Toast.LENGTH_SHORT).show();
                latitude = 0.0;
                longitude = 0.0;
            }

            // uses glide to load the image from firebase stroage into the imageview
            Glide.with(this)
                    .load(imageUrl)
                    .into(placeImageView);
        }

        return view;
    }

    // opens the device gallery for selecting an image
    private void openGalleryPicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        galleryPickerLauncher.launch(intent);
    }

    // uploads the selected image to Firebase Storage
    private void uploadImageToFirebase(Uri imageUri) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        // checks if the user is logged in
        if (user == null) {
            Toast.makeText(getContext(), "Please log in to upload images", Toast.LENGTH_SHORT).show();
            return;
        }

        // checks if location permissions are granted
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getCurrentLocation(
                    com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
                    null
            ).addOnSuccessListener(location -> {
                if (location != null) {
                    // caluclates the distance between the current location and the place's location
                    float distance = calculateDistance(location.getLatitude(), location.getLongitude(), latitude, longitude);

                    // if the user is within a valid distance, it will upload the image
                    if (distance <= 1700.0f) {  // distance set to 1.7km for testing (will be adjusted to 100m for deployment)
                        uploadImageWithLocation(imageUri, user.getUid(), location.getLatitude(), location.getLongitude());
                    } else {
                        // shows a message if the user is not within the allowed range
                        Toast.makeText(getContext(), "You must be within 100 metres of this location to upload an image.", Toast.LENGTH_LONG).show();
                    }
                } else {
                    // shows a message if the location could not be retrieved
                    Toast.makeText(getContext(), "Unable to get current location", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(e -> {
                // shows a message if there was an error retrieving the location
                Toast.makeText(getContext(), "Location error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        } else {
            // shows a message if location permission is not granted
            Toast.makeText(getContext(), "Location permission not granted", Toast.LENGTH_SHORT).show();
        }
    }

    // uploads the image along with its metadata (latitude and longitude)
    private void uploadImageWithLocation(Uri imageUri, String uid, double lat, double lon) {
        String filename = "IMG_" + System.currentTimeMillis() + ".jpg";
        String path = "images/" + uid + "/" + filename;
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(path);

        // creayes metadata with latitude and longitude
        StorageMetadata metadata = new StorageMetadata.Builder()
                .setCustomMetadata("latitude", String.valueOf(lat))
                .setCustomMetadata("longitude", String.valueOf(lon))
                .build();

        // uploads the image to Firebase Storage
        storageRef.putFile(imageUri, metadata)
                .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                    // saves the image data to firestore after a successful upload
                    saveImageDataToFirestore(uid, downloadUri.toString(), String.valueOf(lat), String.valueOf(lon));
                    Toast.makeText(getContext(), "Image uploaded with current location!", Toast.LENGTH_SHORT).show();
                }))
                .addOnFailureListener(e -> {
                    // shows an error message if the upload fails
                    Toast.makeText(getContext(), "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // calculates the distance between two  coordinates (in meters)
    private float calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        float[] results = new float[1];
        Location.distanceBetween(lat1, lon1, lat2, lon2, results);
        return results[0];
    }

    // saves the image data to firestore
    private void saveImageDataToFirestore(String uid, String imageUrl, String lat, String lon) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // preps the data to be saved in firestore
        Map<String, Object> galleryEntry = new HashMap<>();
        galleryEntry.put("Image", imageUrl);
        galleryEntry.put("Latitude", lat);
        galleryEntry.put("Longitude", lon);
        galleryEntry.put("Name", nameTextView.getText().toString());
        galleryEntry.put("date", String.valueOf(System.currentTimeMillis()));

        // saves the data to firestore under the users gallery sub collection
        db.collection("users")
                .document(uid)
                .collection("gallery")
                .add(galleryEntry)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(getContext(), "Saved to gallery!", Toast.LENGTH_SHORT).show();

                    // updates stats for the place if needed
                    StatsManager statsManager = new StatsManager();
                    String cityName = cityTextView.getText().toString();
                    String placeName = nameTextView.getText().toString();

                    statsManager.incrementNottinghamCountIfNeeded(cityName, placeName);

                })
                .addOnFailureListener(e -> {
                    // shows an error message if saving the data fails
                    Toast.makeText(getContext(), "Failed to save gallery entry: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
