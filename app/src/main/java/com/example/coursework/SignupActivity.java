package com.example.coursework;

import android.content.Intent;
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
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class SignupActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);  // Initialize Firebase
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // initialises the firebase auth
        mAuth = FirebaseAuth.getInstance();

        // esnures that the system bar insets are applied properly
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // finds  the signup button and sets an on click listener
        MaterialButton signupButton = findViewById(R.id.button);
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signupUser();
            }
        });

        // finds the login text button and set the on click listener
        MaterialButton loginTextButton = findViewById(R.id.loginText);
        loginTextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        // it will check if user is signed in (non-null) and update ui accordingly.
    }

    // method to sign up the user with the details they enter
    private void signupUser() {

        // ui elements find by id set to variables
        EditText emailField = findViewById(R.id.editTextTextEmailAddress);
        EditText passwordField = findViewById(R.id.editTextTextPassword);
        EditText confirmPasswordField = findViewById(R.id.editTextTextConfirmPassword);

        // trims them down
        String email = emailField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();
        String confirmPassword = confirmPasswordField.getText().toString().trim();

        // checks if email, password, and confirm password are empty
        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(SignupActivity.this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        // checks if passwords match
        if (!password.equals(confirmPassword)) {
            Toast.makeText(SignupActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        // checks password complexity (at least 8 characters, a capital letter, and a number)
        if (password.length() < 8 || !password.matches(".*[A-Z].*") || !password.matches(".*[0-9].*")) {
            Toast.makeText(SignupActivity.this, "Password must be at least 8 characters long, contain a capital letter, and a number.", Toast.LENGTH_LONG).show();
            return;
        }

        // goes ahead with creating the user
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // singup is success, navigate to login page without signing in
                            Log.d("SignupActivity", "createUserWithEmail:success");
                            Toast.makeText(SignupActivity.this, "Signup successful!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                            startActivity(intent);
                        } else {
                            // If signup fails, display a message to the user with the specific error
                            Log.w("SignupActivity", "createUserWithEmail:failure", task.getException());
                            String errorMessage = task.getException() != null ? task.getException().getMessage() : "Signup failed. Try again.";
                            Toast.makeText(SignupActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
