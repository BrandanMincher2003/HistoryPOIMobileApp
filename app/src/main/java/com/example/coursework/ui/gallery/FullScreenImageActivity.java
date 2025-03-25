package com.example.coursework.ui.gallery;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.coursework.R;


// this is for full screening the image when you click on the image in gallery
public class FullScreenImageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_image);

        ImageView fullImageView = findViewById(R.id.fullscreenImageView);

        String imageUrl = getIntent().getStringExtra("imageUrl");
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this)
                    .load(imageUrl)
                    .into(fullImageView);
        }

        fullImageView.setOnClickListener(v -> finish());
    }
}
