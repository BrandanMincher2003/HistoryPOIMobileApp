package com.example.coursework.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.example.coursework.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.coursework.LoginActivity;
import com.example.coursework.databinding.FragmentProfileBinding;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileFragment extends Fragment {
    private FragmentProfileBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    public ProfileFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();


    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // instances db auth and gets current user from auth
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            loadUserPreferences();
        }

        // binding for nightmode ui
        binding.darkModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            AppCompatDelegate.setDefaultNightMode(isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
            saveDarkModePreference(isChecked);
        });

        // finds logout button for onclick listenerr
        MaterialButton logoutButton = view.findViewById(R.id.logout_button); // Find the logout button
        logoutButton.setOnClickListener(v -> logoutUser()); // Set the click listener for the logout button

        // onclick listenrn for support fragment
        binding.support.setOnClickListener(v -> {
            NavController navController = NavHostFragment.findNavController(this);
            navController.navigate(R.id.action_profileFragment_to_supportFragment);
        });
        // binding for navigation to datapriv fragmengt
        binding.dataPrivacy.setOnClickListener(v -> {
            NavController navController = NavHostFragment.findNavController(this);
            navController.navigate(R.id.action_profileFragment_to_dataPrivacyFragment);
        });
        // binding and onclick listening for change password frag navigation
        binding.changePassword.setOnClickListener(v -> {
            NavController navController = NavHostFragment.findNavController(this);
            navController.navigate(R.id.action_profileFragment_to_changePasswordFragment);  // Navigate to ChangePasswordFragment
        });




    }


     //loads user preferences from firestore

    private void loadUserPreferences() {
        DocumentReference userRef = db.collection("users").document(currentUser.getUid());

        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Boolean isDarkMode = documentSnapshot.getBoolean("darkMode");
                if (isDarkMode != null) {
                    binding.darkModeSwitch.setChecked(isDarkMode);
                    AppCompatDelegate.setDefaultNightMode(isDarkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
                }
            }
        }).addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to load preferences", Toast.LENGTH_SHORT).show());
    }


     //saves dark mode preference to firestore

    private void saveDarkModePreference(boolean isDarkMode) {
        db.collection("users").document(currentUser.getUid())
                .update("darkMode", isDarkMode)
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to update preference", Toast.LENGTH_SHORT).show());
    }


    //Logs out the current user from Firebase Authentication this forgets remember me too

    private void logoutUser() {
        mAuth.signOut();
        Toast.makeText(requireContext(), "Logged out successfully!", Toast.LENGTH_SHORT).show();
        navigateToLogin();
    }


     //navigates to loginactiivty after logout

    private void navigateToLogin() {
        Intent intent = new Intent(requireContext(), LoginActivity.class);
        startActivity(intent);
        requireActivity().finish(); // Finish current activity if needed
    }
}
