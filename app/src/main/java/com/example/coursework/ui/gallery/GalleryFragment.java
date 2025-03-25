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

// fragment for gallery with image uploading fab
public class GalleryFragment extends Fragment {

    private RecyclerView recyclerView;
    private GalleryAdapter adapter;
    private Uri photoUri;
    private FusedLocationProviderClient fusedLocationClient;

    public GalleryFragment() {}


    // registers camera actiivtyresultlauncher to handle capturing image
    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Toast.makeText(getContext(), "Photo saved!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Cancelled", Toast.LENGTH_SHORT).show();
                }
            });

    // initialising fragment view with recyclerview and fab click listener
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gallery, container, false);
        recyclerView = view.findViewById(R.id.galleryRecyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());

        // sets the onclick listener for capturing photos
        view.findViewById(R.id.fab_camera).setOnClickListener(v -> openSystemCamera());

        // loads all the gallery items
        loadGalleryItems();

        return view;
    }

    // opens the system camera and saves it to phone storage
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


    // function for loading gallery itenms from the firebase user gallery subcollection
    private void loadGalleryItems() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }
        // sets instance and location for gallery
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
                // formatting the data from gallery
                String formattedDate;
                try {
                    long millis = Long.parseLong(dateRaw);
                    SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
                    formattedDate = sdf.format(new Date(millis));
                } catch (Exception e) {
                    formattedDate = dateRaw; // fallback
                }
                // adds gallery item
                galleryItems.add(new GalleryItem(imageUrl, locationName, formattedDate));
            }

            // sets the adapter after adding the gallery items
            adapter = new GalleryAdapter(getContext(), galleryItems);
            recyclerView.setAdapter(adapter);
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Failed to load gallery: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
}
