package com.example.coursework;
//
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import com.example.coursework.ui.users.UserPreferences;
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
    private UserPreferences userPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_loginpage);

        // initialises firebase auth and firestore cloud
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // finds ui elements
        MaterialButton signUpButton = findViewById(R.id.signUpLink);
        MaterialButton loginButton = findViewById(R.id.loginButton);
        rememberMeSwitch = findViewById(R.id.switch1);

        // hdanles sign up button click
        signUpButton.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
            startActivity(intent);
        });

        // handles login button click
        loginButton.setOnClickListener(v -> loginUser());

        // does the biometric authentication check
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            userPreferences = new UserPreferences(this, user.getUid());
            setupBiometricAuthentication(user.getUid());
        }
    }


     //sets  up the biometric authentication

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
                            userPreferences.loadFromFirestore(); // Load user preferences
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
                            //Toast.makeText(LoginActivity.this, "Authentication error: " + errString, Toast.LENGTH_SHORT).show();
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


     //logs the user in using email and password

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
                            userPreferences = new UserPreferences(this, user.getUid());
                            userPreferences.loadFromFirestore(); // Load preferences

                            // Ensure user collections are created if they don't exist
                            createUserCollectionsIfNeeded(user.getUid(), email);

                            navigateToMainActivity();
                        }
                    } else {
                        Log.w("LoginPage", "signInWithEmail:failure", task.getException());
                        Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void createUserCollectionsIfNeeded(String uid, String email) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // creation of stats collection if it doesn't exist and set NottinghamCount to 1
        DocumentReference statsRef = db.collection("users").document(uid).collection("stats").document(uid);
        statsRef.get().addOnSuccessListener(documentSnapshot -> {
            if (!documentSnapshot.exists()) {
                Map<String, Object> statsData = new HashMap<>();
                statsData.put("NottinghamCount", 0);
                statsData.put("CastleCount", 0);
                statsRef.set(statsData);
            }
        });


        // creation of trophies collection if it doesn't exist
        DocumentReference trophiesRef = db.collection("users").document(uid).collection("trophies").document("dummy");
        trophiesRef.get().addOnSuccessListener(documentSnapshot -> {

        });

        // adds email and darkMode to the user document
        DocumentReference userRef = db.collection("users").document(uid);
        Map<String, Object> userData = new HashMap<>();
        userData.put("email", email);
        userRef.set(userData, SetOptions.merge());  // Use merge to ensure existing fields are not overwritten
    }


     //this navigated to MainActivity(which is PlacesActivity) after login

    private void navigateToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, PlacesActivity.class);
        startActivity(intent);
        finish();
    }
}
