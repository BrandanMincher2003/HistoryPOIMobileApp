package com.example.coursework;

import android.content.Intent;
import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.view.View;
import com.google.android.material.button.MaterialButton;

public class loginpage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_loginpage);

        // Ensure system bar insets are applied properly
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Find the sign-up link button
        MaterialButton signUpButton = findViewById(R.id.signUpLink);

        // Set OnClickListener to navigate to MainActivity
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(loginpage.this, MainActivity.class);
                startActivity(intent);
            }
        });

        // Find the login button
        MaterialButton loginButton = findViewById(R.id.loginButton);

        // Set OnClickListener to navigate to PlacesPage
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(loginpage.this, placespage.class);
                startActivity(intent);
            }
        });
    }
}
