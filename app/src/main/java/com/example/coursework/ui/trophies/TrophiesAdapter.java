package com.example.coursework.ui.trophies;

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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

//adapter to bind data to the UI elements mainly the recyclerview
public class TrophiesAdapter extends RecyclerView.Adapter<TrophiesAdapter.ViewHolder> {

    //context for data access and glide image using
    private final Context context;
    private final List<TrophyItem> trophyList;

    // cosntructor
    public TrophiesAdapter(Context context, List<TrophyItem> trophyList) {
        this.context = context;
        this.trophyList = trophyList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // inflate layout
        View view = LayoutInflater.from(context).inflate(R.layout.trophy_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        //gets trophy data for position in list
        TrophyItem trophy = trophyList.get(position);

        // sets data from cloud db
        holder.nameTextView.setText(trophy.getName());
        holder.descriptionTextView.setText(trophy.getDescription());

        // makes it full opacity when trohpy is achieved
        holder.itemView.setAlpha(trophy.isAchieved() ? 1.0f : 0.5f);

        //tries to loads image from path
        String imagePath = trophy.getImagePath();
        if (imagePath != null && !imagePath.isEmpty()) {
            StorageReference imageRef = FirebaseStorage.getInstance().getReference().child(imagePath);

            Glide.with(context)
                    .load(imageRef)
                    .placeholder(R.drawable.ic_trophy)
                    .error(R.drawable.ic_trophy)
                    .into(holder.iconImageView);
        } else {
            //uses default trophy icon if cant find
            holder.iconImageView.setImageResource(R.drawable.ic_trophy);
        }
    }

    @Override
    public int getItemCount() {
        return trophyList.size();
    }

    // view holder class for holding views for each of the trohpy items
    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView iconImageView;
        TextView nameTextView, descriptionTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            iconImageView = itemView.findViewById(R.id.trophy_icon);
            nameTextView = itemView.findViewById(R.id.trophy_name);
            descriptionTextView = itemView.findViewById(R.id.trophy_description);
        }
    }
}
