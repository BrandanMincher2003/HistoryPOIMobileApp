package com.example.coursework.ui.trophies;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.coursework.R;
import com.example.coursework.ui.achievements.StatsManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


// fragmetn for displaying all the trohpies from the user sub collection and the main achievement collection
public class TrophiesFragment extends Fragment {

    private RecyclerView achievedRecyclerView, notAchievedRecyclerView;
    private TrophiesAdapter achievedAdapter, notAchievedAdapter;
    private List<TrophyItem> achievedList, notAchievedList;
    private FirebaseFirestore db;

    public TrophiesFragment() {}

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

        achievedAdapter = new TrophiesAdapter(getContext(), achievedList);
        notAchievedAdapter = new TrophiesAdapter(getContext(), notAchievedList);

        achievedRecyclerView.setAdapter(achievedAdapter);
        notAchievedRecyclerView.setAdapter(notAchievedAdapter);

        loadTrophyData();

        return view;
    }

    // method for loading the data from trophies user subcollection
    private void loadTrophyData() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("users").document(uid).collection("trophies").get()
                .addOnSuccessListener(userTrophiesSnapshot -> {
                    Set<String> achievedTrophyNames = new HashSet<>();
                    for (DocumentSnapshot doc : userTrophiesSnapshot.getDocuments()) {
                        String name = doc.getString("Name");
                        if (name != null) {
                            achievedTrophyNames.add(name);
                        }
                    }

                    // fetches the rest of the achievements from achievement collection
                    db.collection("achievements").get().addOnSuccessListener(achievementSnapshot -> {
                        achievedList.clear();
                        notAchievedList.clear();

                        // loops through the acheivements to add them to correct section acheived or not acheived
                        for (QueryDocumentSnapshot doc : achievementSnapshot) {
                            String name = doc.getString("Name");
                            String description = doc.getString("Description");
                            String image = doc.getString("Image");

                            // if user has it in sub collection its achieved
                            if (name != null && description != null) {
                                boolean isAchieved = achievedTrophyNames.contains(name);
                                TrophyItem trophy = new TrophyItem(name, description, isAchieved, image);

                                if (isAchieved) {
                                    achievedList.add(trophy);
                                } else {
                                    notAchievedList.add(trophy);
                                }
                            }
                        }
                        // makes adapter change due to new information
                        achievedAdapter.notifyDataSetChanged();
                        notAchievedAdapter.notifyDataSetChanged();
                    }).addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Failed to load achievements", Toast.LENGTH_SHORT).show();
                    });

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to load your trophies", Toast.LENGTH_SHORT).show();
                });
    }
}
