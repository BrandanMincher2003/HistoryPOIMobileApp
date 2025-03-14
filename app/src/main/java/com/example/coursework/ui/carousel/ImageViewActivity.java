package com.example.coursework.ui.carousel;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.coursework.R;

public class ImageViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);

        ImageView imageView = findViewById(R.id.imageView);

        // Load the image from the intent
        String imageUrl = getIntent().getStringExtra("image");
        Glide.with(this).load(imageUrl).into(imageView);
    }
}
