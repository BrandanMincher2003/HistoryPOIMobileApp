package com.example.coursework.ui.gallery;

import android.content.Context;
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

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.GalleryViewHolder> {

    private final Context context;
    private final List<GalleryItem> galleryItems;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(GalleryItem item);
    }

    public GalleryAdapter(Context context, List<GalleryItem> galleryItems, OnItemClickListener listener) {
        this.context = context;
        this.galleryItems = galleryItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public GalleryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_gallery, parent, false);
        return new GalleryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GalleryViewHolder holder, int position) {
        GalleryItem item = galleryItems.get(position);

        // Load image using Glide
        Glide.with(context).load(item.getImageUrl()).into(holder.imageView);

        // Set text values
        holder.locationName.setText(item.getLocationName());
        holder.date.setText(item.getDate());

        // Set click listener
        holder.itemView.setOnClickListener(v -> listener.onItemClick(item));
    }

    @Override
    public int getItemCount() {
        return galleryItems.size();
    }

    public static class GalleryViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView locationName, date;

        public GalleryViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.galleryImage);
            locationName = itemView.findViewById(R.id.locationName);
            date = itemView.findViewById(R.id.imageDate);
        }
    }
}
