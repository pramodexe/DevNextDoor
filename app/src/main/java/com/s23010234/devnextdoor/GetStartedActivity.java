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

// GetStartedActivity allows users to navigate to Login or Signup screens.
public class GetStartedActivity extends AppCompatActivity {

    // Called when the activity is starting.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Enable edge-to-edge display
        EdgeToEdge.enable(this);

        // Set the layout for this activity
        setContentView(R.layout.activity_get_started);

        // Apply system window insets padding for edge-to-edge UI
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (view, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize the login button and set click listener to navigate to LoginActivity
        Button loginButton = findViewById(R.id.loginButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to LoginActivity
                Intent intent = new Intent(GetStartedActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        // Initialize the signup button and set click listener to navigate to SignupActivity
        Button signupButton = findViewById(R.id.signupButton);
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to SignupActivity
                Intent intent = new Intent(GetStartedActivity.this, SignupActivity.class);
                startActivity(intent);
            }
        });
    }
}
