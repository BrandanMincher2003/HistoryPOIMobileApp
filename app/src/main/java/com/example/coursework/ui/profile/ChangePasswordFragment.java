package com.example.coursework.ui.profile;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.coursework.R;
import com.example.coursework.databinding.FragmentChangePasswordBinding;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;

public class ChangePasswordFragment extends Fragment {

    private FragmentChangePasswordBinding binding;
    private FirebaseAuth mAuth;

    public ChangePasswordFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentChangePasswordBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();

        binding.changePasswordButton.setOnClickListener(v -> {
            String oldPassword = binding.oldPassword.getText().toString().trim();
            String newPassword = binding.newPassword.getText().toString().trim();
            String confirmNewPassword = binding.confirmNewPassword.getText().toString().trim();

            if (TextUtils.isEmpty(oldPassword) || TextUtils.isEmpty(newPassword) || TextUtils.isEmpty(confirmNewPassword)) {
                Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPassword.equals(confirmNewPassword)) {
                Toast.makeText(getContext(), "New password and confirm password do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            changePassword(oldPassword, newPassword);
        });
    }

    private void changePassword(String oldPassword, String newPassword) {
        String email = mAuth.getCurrentUser().getEmail();

        if (email == null) {
            Toast.makeText(getContext(), "Error: Email is null", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseAuth.getInstance().getCurrentUser().reauthenticate(
                EmailAuthProvider.getCredential(email, oldPassword)
        ).addOnSuccessListener(aVoid -> {
            mAuth.getCurrentUser().updatePassword(newPassword)
                    .addOnSuccessListener(aVoid1 -> {
                        Toast.makeText(getContext(), "Password changed successfully", Toast.LENGTH_SHORT).show();
                        getActivity().onBackPressed();
                    })
                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Password change failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Old password is incorrect", Toast.LENGTH_SHORT).show();
        });
    }
}
