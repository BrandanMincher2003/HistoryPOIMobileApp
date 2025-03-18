package com.example.coursework.ui.carousel;

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

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {

    private Context context;
    private List<PlaceItem> items;
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onClick(ImageView imageView, String imageUrl);
    }

    public ImageAdapter(Context context, List<PlaceItem> items) {
        this.context = context;
        this.items = items;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_place_carousel, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PlaceItem item = items.get(position);

        // Load image using Glide
        Glide.with(context).load(item.getImageUrl()).into(holder.imageView);

        // Set name and distance
        holder.name.setText(item.getName());
        holder.distance.setText(item.getDistance());

        holder.imageView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onClick(holder.imageView, item.getImageUrl());
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView name, distance;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.place_image);
            name = itemView.findViewById(R.id.place_name);
            distance = itemView.findViewById(R.id.place_distance);
        }
    }
}
