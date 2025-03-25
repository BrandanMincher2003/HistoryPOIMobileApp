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

//the placesdetailsfragment class for displaying details of places when clicked with upload fab
public class PlaceDetailsFragment extends Fragment {

    //decalring variables
    private TextView nameTextView, cityTextView, descriptionTextView;
    private ImageView placeImageView;
    private FusedLocationProviderClient fusedLocationClient;
    private double latitude, longitude;

    // this handles image selection from the phones gallery
    private final ActivityResultLauncher<Intent> galleryPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri selectedImageUri = result.getData().getData();
                    if (selectedImageUri != null) {
                        uploadImageToFirebase(selectedImageUri);
                    }
                } else {
                    Toast.makeText(getContext(), "No image selected", Toast.LENGTH_SHORT).show();
                }
            });

    //default contructtor
    public PlaceDetailsFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // inflates the fragments layout

        View view = inflater.inflate(R.layout.fragment_place_details, container, false);

        //initialises the ui components
        nameTextView = view.findViewById(R.id.textViewName);
        cityTextView = view.findViewById(R.id.textViewCity);
        descriptionTextView = view.findViewById(R.id.textViewDescription);
        placeImageView = view.findViewById(R.id.imageViewPlace);

        //finds the floating action buttons for upload and finding location of place
        View fabUpload = view.findViewById(R.id.fab_upload);
        View fabFindLocation = view.findViewById(R.id.fab_find_location);

        // inits the location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());

        // the listeners for the the fab buttons
        fabUpload.setOnClickListener(v -> openGalleryPicker());

        fabFindLocation.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            //data prep for navigation to map
            bundle.putDouble("latitude", latitude);
            bundle.putDouble("longitude", longitude);

            // navigates to the map fragment with the data for zoom
            NavController navController = NavHostFragment.findNavController(PlaceDetailsFragment.this);

            NavOptions navOptions = new NavOptions.Builder()
                    .setPopUpTo(R.id.nav_search, true)
                    .build();

            navController.navigate(R.id.nav_map, bundle, navOptions);
        });

        // gets the place details and sets them to the UI
        if (getArguments() != null) {
            nameTextView.setText(getArguments().getString("name"));
            cityTextView.setText(getArguments().getString("city"));
            descriptionTextView.setText(getArguments().getString("description"));
            String imageUrl = getArguments().getString("image");

            try {
                latitude = getArguments().containsKey("latitude") ? getArguments().getDouble("latitude") : Double.parseDouble(getArguments().getString("latitude"));
                longitude = getArguments().containsKey("longitude") ? getArguments().getDouble("longitude") : Double.parseDouble(getArguments().getString("longitude"));
            } catch (Exception e) {
                Toast.makeText(getContext(), "Invalid latitude or longitude", Toast.LENGTH_SHORT).show();
                latitude = 0.0;
                longitude = 0.0;
            }

            // uses glide to load the images from the urls in the firebase clodu storage
            Glide.with(this)
                    .load(imageUrl)
                    .into(placeImageView);
        }

        return view;
    }

    // function for opening the gallery
    private void openGalleryPicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        galleryPickerLauncher.launch(intent);
    }

    // uploading the selected photo to the firebase storage for in app gallery
    private void uploadImageToFirebase(Uri imageUri) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            Toast.makeText(getContext(), "Please log in to upload images", Toast.LENGTH_SHORT).show();
            return;
        }

        // this checks if the current location is within rules
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getCurrentLocation(
                    com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
                    null
            ).addOnSuccessListener(location -> {
                if (location != null) {
                    float distance = calculateDistance(location.getLatitude(), location.getLongitude(), latitude, longitude);
                    Log.d("DEBUG", "Current location: " + location.getLatitude() + ", " + location.getLongitude());
                    Log.d("DEBUG", "Target location: " + latitude + ", " + longitude);
                    Log.d("DEBUG", "Distance: " + distance + " metres");

                    // this is set as 1.7km but would usually be 100meters
                    if (distance <= 1700.0f) {
                        uploadImageWithLocation(imageUri, user.getUid(), location.getLatitude(), location.getLongitude());
                    } else {
                        Toast.makeText(getContext(), "You must be within 100 metres of this location to upload an image.", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Unable to get current location", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(getContext(), "Location error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        } else {
            Toast.makeText(getContext(), "Location permission not granted", Toast.LENGTH_SHORT).show();
        }
    }

    // uploads the image with metadata longitude and latitude
    private void uploadImageWithLocation(Uri imageUri, String uid, double lat, double lon) {
        String filename = "IMG_" + System.currentTimeMillis() + ".jpg";
        String path = "images/" + uid + "/" + filename;
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(path);

        StorageMetadata metadata = new StorageMetadata.Builder()
                .setCustomMetadata("latitude", String.valueOf(lat))
                .setCustomMetadata("longitude", String.valueOf(lon))
                .build();

        storageRef.putFile(imageUri, metadata)
                .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                    saveImageDataToFirestore(uid, downloadUri.toString(), String.valueOf(lat), String.valueOf(lon));
                    Toast.makeText(getContext(), "Image uploaded with current location!", Toast.LENGTH_SHORT).show();
                }))
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    // calculates the distance for doing it to location and user for rules for uploading
    private float calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        float[] results = new float[1];
        Location.distanceBetween(lat1, lon1, lat2, lon2, results);
        return results[0];
    }

    // saves the image  to the firestore with uid
    private void saveImageDataToFirestore(String uid, String imageUrl, String lat, String lon) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> galleryEntry = new HashMap<>();
        galleryEntry.put("Image", imageUrl);
        galleryEntry.put("Latitude", lat);
        galleryEntry.put("Longitude", lon);
        galleryEntry.put("Name", nameTextView.getText().toString());
        galleryEntry.put("date", String.valueOf(System.currentTimeMillis()));

        db.collection("users")
                .document(uid)
                .collection("gallery")
                .add(galleryEntry)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(getContext(), "Saved to gallery!", Toast.LENGTH_SHORT).show();

                    StatsManager statsManager = new StatsManager();
                    String cityName = cityTextView.getText().toString();
                    String placeName = nameTextView.getText().toString();

                    statsManager.incrementNottinghamCountIfNeeded(cityName, placeName);

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to save gallery entry: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
