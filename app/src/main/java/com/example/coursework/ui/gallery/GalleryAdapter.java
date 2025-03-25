package com.example.coursework.ui.gallery;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.coursework.R;

import java.util.List;

// this is an adapter for displaying gallery items in the recyclervieew

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.GalleryViewHolder> {

    private final Context context;
    private final List<GalleryItem> galleryItems;

    // constructor for initialising context and gallery items
    public GalleryAdapter(Context context, List<GalleryItem> galleryItems) {
        this.context = context;
        this.galleryItems = galleryItems;
    }


    // the view holder for each item in the recycler
    @NonNull
    @Override
    public GalleryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_gallery, parent, false);
        return new GalleryViewHolder(view);
    }

    // binds the data to the views in the viewholder
    @Override
    public void onBindViewHolder(@NonNull GalleryViewHolder holder, int position) {
        GalleryItem item = galleryItems.get(position);

        Glide.with(context).load(item.getImageUrl()).into(holder.imageView);
        holder.locationName.setText(item.getLocationName());
        holder.date.setText(item.getDate());

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, FullScreenImageActivity.class);
            intent.putExtra("imageUrl", item.getImageUrl());
            context.startActivity(intent);
        });
    }

    // returns the total number of items in the view
    @Override
    public int getItemCount() {
        return galleryItems.size();
    }

    // view holdr vlass
    public static class GalleryViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView locationName, date;

        // view contructor
        public GalleryViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.galleryImage);
            locationName = itemView.findViewById(R.id.locationName);
            date = itemView.findViewById(R.id.imageDate);
        }
    }
}