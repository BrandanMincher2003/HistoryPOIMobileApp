package com.example.coursework.ui.places;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.coursework.R;
import com.example.coursework.ui.carousel.ImageAdapter;
import com.example.coursework.ui.carousel.ImageViewActivity;

import java.util.ArrayList;

public class PlacesFragment extends Fragment {

    private RecyclerView recyclerView;
    private ImageAdapter adapter;
    private ArrayList<String> imageUrls;

    public PlacesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the correct layout file for the fragment
        View view = inflater.inflate(R.layout.fragment_places, container, false);

        // Find RecyclerView inside the fragment layout
        recyclerView = view.findViewById(R.id.recyclerView);

        // Check if RecyclerView exists
        if (recyclerView == null) {
            throw new RuntimeException("RecyclerView not found! Check fragment_places.xml");
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        imageUrls = new ArrayList<>();
        imageUrls.add("https://images.unsplash.com/photo-1616856497165-a217b221c8b6?q=80&w=1974&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D");
        imageUrls.add("https://images.unsplash.com/photo-1595685833450-b63451efcf01?q=80&w=2129&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D");
        imageUrls.add("https://images.unsplash.com/photo-1630438325568-69fc918b27cc?q=80&w=1931&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D");


        adapter = new ImageAdapter(getContext(), imageUrls);
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(new ImageAdapter.OnItemClickListener() {
            @Override
            public void onClick(ImageView imageView, String path) {
                Intent intent = new Intent(getContext(), ImageViewActivity.class);
                intent.putExtra("image", path);
                startActivity(intent);
            }
        });

        return view;
    }
}
