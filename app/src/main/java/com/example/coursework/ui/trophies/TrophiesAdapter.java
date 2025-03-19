package com.example.coursework.ui.trophies;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.coursework.R;
import com.example.coursework.model.TrophyItem;
import java.util.List;

public class TrophiesAdapter extends RecyclerView.Adapter<TrophiesAdapter.ViewHolder> {
    private Context context;
    private List<TrophyItem> trophyList;

    public TrophiesAdapter(Context context, List<TrophyItem> trophyList) {
        this.context = context;
        this.trophyList = trophyList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.trophy_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TrophyItem trophy = trophyList.get(position);
        holder.nameTextView.setText(trophy.getName());
        holder.descriptionTextView.setText(trophy.getDescription());

        // Change background color based on achievement status
        if (trophy.isAchieved()) {
            holder.itemView.setBackgroundColor(ContextCompat.getColor(context, android.R.color.white));
        } else {
            holder.itemView.setBackgroundColor(ContextCompat.getColor(context, android.R.color.white));
        }
    }

    @Override
    public int getItemCount() {
        return trophyList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, descriptionTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.trophy_name);
            descriptionTextView = itemView.findViewById(R.id.trophy_description);
        }
    }
}
