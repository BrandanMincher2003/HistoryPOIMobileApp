package com.example.coursework.ui.gallery;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.example.coursework.R;
import java.util.List;

public class GalleryFragment extends Fragment {

    private RecyclerView recyclerView;
    private GalleryAdapter adapter;
    private FirebaseGalleryHelper firebaseGalleryHelper;

    public GalleryFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gallery, container, false);
        recyclerView = view.findViewById(R.id.galleryRecyclerView);

        // Use GridLayout with 3 columns
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));

        // Initialize Firestore Helper
        firebaseGalleryHelper = new FirebaseGalleryHelper();

        // Fetch and display gallery items
        loadGalleryItems();

        return view;
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
