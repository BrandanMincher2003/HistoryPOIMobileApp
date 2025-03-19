package com.example.coursework.ui.trophies;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.example.coursework.R;
import com.example.coursework.ui.trophies.TrophiesAdapter;
import com.example.coursework.model.TrophyItem;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class TrophiesFragment extends Fragment {

    private RecyclerView achievedRecyclerView, notAchievedRecyclerView;
    private TrophiesAdapter achievedAdapter, notAchievedAdapter;
    private List<TrophyItem> achievedList, notAchievedList;
    private FirebaseFirestore db;

    public TrophiesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_trophies, container, false);

        achievedRecyclerView = view.findViewById(R.id.achievedRecyclerView);
        notAchievedRecyclerView = view.findViewById(R.id.notAchievedRecyclerView);

        achievedRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        notAchievedRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        achievedList = new ArrayList<>();
        notAchievedList = new ArrayList<>();

        db = FirebaseFirestore.getInstance();

        // Fetch achievements from Firestore
        loadTrophyData();

        achievedAdapter = new TrophiesAdapter(getContext(), achievedList);
        notAchievedAdapter = new TrophiesAdapter(getContext(), notAchievedList);

        achievedRecyclerView.setAdapter(achievedAdapter);
        notAchievedRecyclerView.setAdapter(notAchievedAdapter);

        return view;
    }

    private void loadTrophyData() {
        db.collection("achievements").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                achievedList.clear();
                notAchievedList.clear();

                for (QueryDocumentSnapshot document : task.getResult()) {
                    String name = document.getString("Name");
                    String description = document.getString("Description");

                    // Assume achievements are stored based on completion status
                    boolean isAchieved = document.contains("Achieved") && document.getBoolean("Achieved") != null && document.getBoolean("Achieved");

                    if (name != null && description != null) {
                        TrophyItem trophy = new TrophyItem(name, description, isAchieved);
                        if (isAchieved) {
                            achievedList.add(trophy);
                        } else {
                            notAchievedList.add(trophy);
                        }
                    }
                }

                achievedAdapter.notifyDataSetChanged();
                notAchievedAdapter.notifyDataSetChanged();
            } else {
                Toast.makeText(getContext(), "Failed to load achievements", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
