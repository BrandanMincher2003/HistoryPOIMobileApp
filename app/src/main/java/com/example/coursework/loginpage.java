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
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class loginpage extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "loginPrefs";
    private static final String KEY_REMEMBER_ME = "rememberMe";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);  // Initialize Firebase
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_loginpage);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        // Ensure system bar insets are applied properly
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Auto-login if Remember Me is enabled
        if (sharedPreferences.getBoolean(KEY_REMEMBER_ME, false)) {
            FirebaseUser user = mAuth.getCurrentUser();
            if (user != null) {
                Intent intent = new Intent(loginpage.this, placespage.class);
                startActivity(intent);
                finish(); // Close login page
            }
        }

        // Find the sign-up link button
        MaterialButton signUpButton = findViewById(R.id.signUpLink);
        signUpButton.setOnClickListener(v -> {
            Intent intent = new Intent(loginpage.this, MainActivity.class);
            startActivity(intent);
        });

        // Find the login button
        MaterialButton loginButton = findViewById(R.id.loginButton);
        loginButton.setOnClickListener(v -> loginUser());
    }

    private void loginUser() {
        EditText emailField = findViewById(R.id.editTextTextEmailAddress);
        EditText passwordField = findViewById(R.id.editTextTextPassword);
        MaterialSwitch rememberMeSwitch = findViewById(R.id.switch1); // Corrected casting

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
                            editor.putBoolean(KEY_REMEMBER_ME, rememberMeSwitch.isChecked()); // Fixed
                            editor.apply();

                            // Navigate to PlacesPage after successful login
                            Intent intent = new Intent(loginpage.this, placespage.class);
                            startActivity(intent);
                            finish(); // Close login page
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("LoginPage", "signInWithEmail:failure", task.getException());
                            Toast.makeText(loginpage.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
