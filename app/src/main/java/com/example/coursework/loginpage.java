package com.example.coursework;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.concurrent.Executor;

public class loginpage extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "loginPrefs";
    private static final String KEY_REMEMBER_ME = "rememberMe";

    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_loginpage);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        // Find UI elements
        MaterialButton signUpButton = findViewById(R.id.signUpLink);
        MaterialButton loginButton = findViewById(R.id.loginButton);
        MaterialSwitch rememberMeSwitch = findViewById(R.id.switch1);

        // Handle sign-up button click
        signUpButton.setOnClickListener(v -> {
            Intent intent = new Intent(loginpage.this, MainActivity.class);
            startActivity(intent);
        });

        // Handle login button click
        loginButton.setOnClickListener(v -> loginUser());

        // If "Remember Me" is enabled, trigger biometric authentication
        if (sharedPreferences.getBoolean(KEY_REMEMBER_ME, false)) {
            FirebaseUser user = mAuth.getCurrentUser();
            if (user != null) {
                setupBiometricAuthentication();
            }
        }
    }

    private void setupBiometricAuthentication() {
        BiometricManager biometricManager = BiometricManager.from(this);

        if (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                == BiometricManager.BIOMETRIC_SUCCESS) {

            Executor executor = ContextCompat.getMainExecutor(this);
            biometricPrompt = new BiometricPrompt(this, executor,
                    new BiometricPrompt.AuthenticationCallback() {
                        @Override
                        public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                            super.onAuthenticationSucceeded(result);
                            Toast.makeText(loginpage.this, "Authentication successful!", Toast.LENGTH_SHORT).show();

                            // Navigate to PlacesPage after successful fingerprint authentication
                            Intent intent = new Intent(loginpage.this, placespage.class);
                            startActivity(intent);
                            finish();
                        }

                        @Override
                        public void onAuthenticationFailed() {
                            super.onAuthenticationFailed();
                            Toast.makeText(loginpage.this, "Fingerprint not recognized. Try again.", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                            super.onAuthenticationError(errorCode, errString);
                            Toast.makeText(loginpage.this, "Authentication error: " + errString, Toast.LENGTH_SHORT).show();
                        }
                    });

            promptInfo = new BiometricPrompt.PromptInfo.Builder()
                    .setTitle("Fingerprint Authentication")
                    .setSubtitle("Use your fingerprint to log in")
                    .setNegativeButtonText("Use password instead")
                    .build();

            biometricPrompt.authenticate(promptInfo);
        } else {
            Toast.makeText(this, "Biometric authentication not available", Toast.LENGTH_SHORT).show();
        }
    }

    private void loginUser() {
        EditText emailField = findViewById(R.id.editTextTextEmailAddress);
        EditText passwordField = findViewById(R.id.editTextTextPassword);
        MaterialSwitch rememberMeSwitch = findViewById(R.id.switch1);

        String email = emailField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(loginpage.this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success
                            Log.d("LoginPage", "signInWithEmail:success");
                            Toast.makeText(loginpage.this, "Login successful!", Toast.LENGTH_SHORT).show();

                            // Save login state if Remember Me is enabled
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putBoolean(KEY_REMEMBER_ME, rememberMeSwitch.isChecked());
                            editor.apply();

                            // Navigate to PlacesPage after successful login
                            Intent intent = new Intent(loginpage.this, placespage.class);
                            startActivity(intent);
                            finish();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("LoginPage", "signInWithEmail:failure", task.getException());
                            Toast.makeText(loginpage.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
