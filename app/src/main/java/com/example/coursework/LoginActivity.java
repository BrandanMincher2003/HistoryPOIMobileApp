package com.example.coursework;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private MaterialSwitch rememberMeSwitch;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_loginpage);

        // Initialize Firebase Auth & Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Find UI elements
        MaterialButton signUpButton = findViewById(R.id.signUpLink);
        MaterialButton loginButton = findViewById(R.id.loginButton);
        rememberMeSwitch = findViewById(R.id.switch1);

        // Handle sign-up button click
        signUpButton.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
            startActivity(intent);
        });

        // Handle login button click
        loginButton.setOnClickListener(v -> loginUser());

        // Biometric authentication check
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            setupBiometricAuthentication(user.getUid());
        }
    }

    /**
     * Sets up biometric authentication
     */
    private void setupBiometricAuthentication(String userId) {
        BiometricManager biometricManager = BiometricManager.from(this);

        if (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                == BiometricManager.BIOMETRIC_SUCCESS) {

            Executor executor = ContextCompat.getMainExecutor(this);
            biometricPrompt = new BiometricPrompt(this, executor,
                    new BiometricPrompt.AuthenticationCallback() {
                        @Override
                        public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                            super.onAuthenticationSucceeded(result);
                            Toast.makeText(LoginActivity.this, "Authentication successful!", Toast.LENGTH_SHORT).show();
                            loadUserPreferences(userId); // Apply dark mode preference
                            navigateToMainActivity();
                        }

                        @Override
                        public void onAuthenticationFailed() {
                            super.onAuthenticationFailed();
                            Toast.makeText(LoginActivity.this, "Fingerprint not recognized. Try again.", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                            super.onAuthenticationError(errorCode, errString);
                            Toast.makeText(LoginActivity.this, "Authentication error: " + errString, Toast.LENGTH_SHORT).show();
                        }
                    });

            promptInfo = new BiometricPrompt.PromptInfo.Builder()
                    .setTitle("Fingerprint Authentication")
                    .setSubtitle("Use your fingerprint to log in")
                    .setNegativeButtonText("Use password instead")
                    .build();

            biometricPrompt.authenticate(promptInfo);
        } else {
            Log.w("Biometrics", "Biometric authentication not available");
        }
    }

    /**
     * Logs in the user using email and password
     */
    private void loginUser() {
        EditText emailField = findViewById(R.id.editTextTextEmailAddress);
        EditText passwordField = findViewById(R.id.editTextTextPassword);

        String email = emailField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(LoginActivity.this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            createUserDocument(user.getUid(), user.getEmail());
                            loadUserPreferences(user.getUid()); // Apply user preferences
                        }
                        navigateToMainActivity();
                    } else {
                        Log.w("LoginPage", "signInWithEmail:failure", task.getException());
                        Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Creates or updates Firestore document for user
     */
    private void createUserDocument(String userId, String email) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("email", email);
        userData.put("darkMode", false); // Default preference

        db.collection("users").document(userId)
                .set(userData, SetOptions.merge()) // Merge avoids overwriting
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "User document created/updated"))
                .addOnFailureListener(e -> Log.e("Firestore", "Error creating user document", e));
    }

    /**
     * Loads dark mode preference from Firestore and applies it immediately
     */
    private void loadUserPreferences(String userId) {
        DocumentReference userRef = db.collection("users").document(userId);

        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Boolean isDarkMode = documentSnapshot.getBoolean("darkMode");
                if (isDarkMode != null) {
                    AppCompatDelegate.setDefaultNightMode(isDarkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
                }
            }
        }).addOnFailureListener(e -> Log.e("Firestore", "Failed to load user preferences", e));
    }

    /**
     * Navigates to MainActivity after login
     */
    private void navigateToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, PlacesActivity.class);
        startActivity(intent);
        finish();
    }
}
