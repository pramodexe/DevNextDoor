package com.s23010234.devnextdoor;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class ProfileInstructionsActivity extends AppCompatActivity {

    private Button continueButton;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Apply the current theme before setting content view
        ThemeManager.applyTheme(this);
        
        setContentView(R.layout.activity_profile_instructions);

        // Get username from intent
        username = getIntent().getStringExtra("username");

        // Initialize views
        continueButton = findViewById(R.id.continueButton);

        // Set click listener for continue button
        continueButton.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileInstructionsActivity.this, EditProfileActivity.class);
            intent.putExtra("username", username);
            startActivity(intent);
            finish();
        });
    }
}
