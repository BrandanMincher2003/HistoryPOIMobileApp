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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.coursework.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class GalleryFragment extends Fragment {

    private RecyclerView recyclerView;
    private GalleryAdapter adapter;
    private Uri photoUri;
    private FusedLocationProviderClient fusedLocationClient;

    public GalleryFragment() {}

    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Toast.makeText(getContext(), "Photo saved!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Cancelled", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gallery, container, false);
        recyclerView = view.findViewById(R.id.galleryRecyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());

        view.findViewById(R.id.fab_camera).setOnClickListener(v -> openSystemCamera());

        loadGalleryItems();

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
                final String[] lat = {"unknown"};
                final String[] lon = {"unknown"};

                if (location != null) {
                    lat[0] = String.valueOf(location.getLatitude());
                    lon[0] = String.valueOf(location.getLongitude());
                }

                StorageMetadata metadata = new StorageMetadata.Builder()
                        .setCustomMetadata("latitude", lat[0])
                        .setCustomMetadata("longitude", lon[0])
                        .build();

                storageRef.putFile(imageUri, metadata)
                        .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                            saveImageDataToFirestore(uid, downloadUri.toString(), lat[0], lon[0], filename);
                            Toast.makeText(getContext(), "Image uploaded with current location!", Toast.LENGTH_SHORT).show();
                        }))
                        .addOnFailureListener(e -> Toast.makeText(getContext(), "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            });
        } else {
            Toast.makeText(getContext(), "Location permission not granted", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveImageDataToFirestore(String uid, String imageUrl, String lat, String lon, String name) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> galleryEntry = new HashMap<>();
        galleryEntry.put("Image", imageUrl);
        galleryEntry.put("Latitude", lat);
        galleryEntry.put("Longitude", lon);
        galleryEntry.put("Name", name);
        galleryEntry.put("date", String.valueOf(System.currentTimeMillis()));

        db.collection("users")
                .document(uid)
                .collection("gallery")
                .add(galleryEntry)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(getContext(), "Saved to gallery!", Toast.LENGTH_SHORT).show();
                    loadGalleryItems();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to save gallery entry: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void loadGalleryItems() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference galleryRef = db.collection("users")
                .document(user.getUid())
                .collection("gallery");

        galleryRef.get().addOnSuccessListener(querySnapshot -> {
            List<GalleryItem> galleryItems = new ArrayList<>();
            for (QueryDocumentSnapshot doc : querySnapshot) {
                String imageUrl = doc.getString("Image");
                String locationName = doc.getString("Name");
                String dateRaw = doc.getString("date");

                String formattedDate;
                try {
                    long millis = Long.parseLong(dateRaw);
                    SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
                    formattedDate = sdf.format(new Date(millis));
                } catch (Exception e) {
                    formattedDate = dateRaw; // fallback
                }

                galleryItems.add(new GalleryItem(imageUrl, locationName, formattedDate));
            }

            adapter = new GalleryAdapter(getContext(), galleryItems);
            recyclerView.setAdapter(adapter);
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Failed to load gallery: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
}
