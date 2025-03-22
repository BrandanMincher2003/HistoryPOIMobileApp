package com.example.coursework.ui.gallery;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
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
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.exifinterface.media.ExifInterface;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.coursework.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.io.InputStream;

public class GalleryFragment extends Fragment {

    private RecyclerView recyclerView;
    private GalleryAdapter adapter;
    private FirebaseGalleryHelper firebaseGalleryHelper;

    private Uri photoUri; // Where the photo will be saved
    private FusedLocationProviderClient fusedLocationClient;

    public GalleryFragment() {
        // Required empty public constructor
    }

    // Handle camera result with ActivityResultLauncher
    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Toast.makeText(getContext(), "Photo saved!", Toast.LENGTH_SHORT).show();
                    // You can refresh the gallery here if needed
                } else {
                    Toast.makeText(getContext(), "Cancelled", Toast.LENGTH_SHORT).show();
                }
            });

    // Handle gallery picker result
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

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gallery, container, false);
        recyclerView = view.findViewById(R.id.galleryRecyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));

        firebaseGalleryHelper = new FirebaseGalleryHelper();
        loadGalleryItems();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());

        View fabCamera = view.findViewById(R.id.fab_camera);
        fabCamera.setOnClickListener(v -> openSystemCamera());

        View fabUpload = view.findViewById(R.id.fab_upload);
        fabUpload.setOnClickListener(v -> openGalleryPicker());

        return view;
    }

    private void openSystemCamera() {
        String filename = "IMG_" + System.currentTimeMillis() + ".jpg";
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, filename);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");

        photoUri = requireActivity().getContentResolver().insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values
        );

        if (photoUri != null) {
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            cameraLauncher.launch(cameraIntent);
        } else {
            Toast.makeText(getContext(), "Error creating image file", Toast.LENGTH_SHORT).show();
        }
    }

    private void openGalleryPicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        galleryPickerLauncher.launch(intent);
    }

    private void uploadImageToFirebase(Uri imageUri) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            Toast.makeText(getContext(), "Please log in to upload images", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = user.getUid();
        String filename = "IMG_" + System.currentTimeMillis() + ".jpg";
        String path = "images/" + uid + "/" + filename;

        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(path);

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                StorageMetadata metadata;
                if (location != null) {
                    metadata = new StorageMetadata.Builder()
                            .setCustomMetadata("latitude", String.valueOf(location.getLatitude()))
                            .setCustomMetadata("longitude", String.valueOf(location.getLongitude()))
                            .build();
                } else {
                    metadata = new StorageMetadata.Builder()
                            .setCustomMetadata("latitude", "unknown")
                            .setCustomMetadata("longitude", "unknown")
                            .build();
                }

                storageRef.putFile(imageUri, metadata)
                        .addOnSuccessListener(taskSnapshot -> {
                            Toast.makeText(getContext(), "Image uploaded with current location!", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(getContext(), "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            });
        } else {
            Toast.makeText(getContext(), "Location permission not granted", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadGalleryItems() {
        firebaseGalleryHelper.fetchGalleryItems(galleryItems -> {
            if (galleryItems.isEmpty()) {
                Toast.makeText(getContext(), "No data found in Firestore", Toast.LENGTH_SHORT).show();
            }
            adapter = new GalleryAdapter(getContext(), galleryItems, item -> {
                // Handle item click (e.g., open details page)
            });
            recyclerView.setAdapter(adapter);
        });
    }
}