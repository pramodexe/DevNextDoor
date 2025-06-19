package com.s23010234.devnextdoor;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

// WelcomeActivity displays the welcome screen and navigates to GetStartedActivity.
public class WelcomeActivity extends AppCompatActivity {

    // Called when the activity is starting.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Enable edge-to-edge display
        EdgeToEdge.enable(this);

        // Set the layout for this activity
        setContentView(R.layout.activity_welcome);

        // Apply system window insets padding for edge-to-edge UI
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (view, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize the continue button and set click listener to navigate to GetStartedActivity
        Button continueButton = findViewById(R.id.continueButton);
        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to GetStartedActivity
                Intent intent = new Intent(WelcomeActivity.this, GetStartedActivity.class);
                startActivity(intent);
            }
        });
    }
}
