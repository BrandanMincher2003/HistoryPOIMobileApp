package com.example.coursework.ui.carousel;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.coursework.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {

    // Declaring Varibles
    private Context context;
    private List<PlaceItem> items;
    private OnPlaceClickListener onPlaceClickListener;
    private Set<String> favouritePlaceNames;
    private String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    // handles place click
    public interface OnPlaceClickListener {
        void onPlaceClick(PlaceItem placeItem);
    }

    // ImageAdapter contructor to initialise variables
    public ImageAdapter(Context context, List<PlaceItem> items, Set<String> favouritePlaceNames, OnPlaceClickListener listener) {
        this.context = context;
        this.items = items;
        this.favouritePlaceNames = favouritePlaceNames;
        this.onPlaceClickListener = listener;
    }

    // Inflates the view for each item in the RecyclerView

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_place_carousel, parent, false);
        return new ViewHolder(view);
    }

    // Binds data to the views in each item of the RecyclerView

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PlaceItem item = items.get(position);

        // uses glide to load the images
        Glide.with(context).load(item.getImageUrl()).into(holder.imageView);

        //sets the text for name and distance
        holder.name.setText(item.getName());
        holder.distance.setText(item.getDistance());

        // Handle heart icon click (add or remove from favourites)

        boolean isFavourited = favouritePlaceNames.contains(item.getName());
        holder.heartIcon.setColorFilter(ContextCompat.getColor(context,
                isFavourited ? R.color.red : R.color.grey));

        holder.heartIcon.setOnClickListener(v -> {
            if (isFavourited) {
                removeFromFavourites(item);
                favouritePlaceNames.remove(item.getName());
                Toast.makeText(context, "Removed from favourites", Toast.LENGTH_SHORT).show();
            } else {
                addToFavourites(item);
                favouritePlaceNames.add(item.getName());
                Toast.makeText(context, "Added to favourites", Toast.LENGTH_SHORT).show();
            }
            notifyDataSetChanged();
        });

        holder.imageView.setOnClickListener(v -> {
            if (onPlaceClickListener != null) {
                onPlaceClickListener.onPlaceClick(item);
            }
        });
    }

    // function to add place to users favourites subcollection
    private void addToFavourites(PlaceItem item) {
        Map<String, Object> data = new HashMap<>();
        data.put("Name", item.getName());
        data.put("Image", item.getImageUrl());
        data.put("Latitude", item.getLatitude());
        data.put("Longitude", item.getLongitude());

        db.collection("users")
                .document(currentUserId)
                .collection("favourites")
                .add(data);
    }
    // function to remove a place to users facourites in subcollection
    private void removeFromFavourites(PlaceItem item) {
        db.collection("users")
                .document(currentUserId)
                .collection("favourites")
                .whereEqualTo("Name", item.getName())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        db.collection("users")
                                .document(currentUserId)
                                .collection("favourites")
                                .document(doc.getId())
                                .delete();
                    }
                });
    }
    // returns total number of items in the list
    @Override
    public int getItemCount() {
        return items.size();
    }

    // the view holder class which holds the view for the items in the recyclerview
    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView, heartIcon;
        TextView name, distance;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.place_image);
            heartIcon = itemView.findViewById(R.id.heart_icon);
            name = itemView.findViewById(R.id.place_name);
            distance = itemView.findViewById(R.id.place_distance);
        }
    }
}

