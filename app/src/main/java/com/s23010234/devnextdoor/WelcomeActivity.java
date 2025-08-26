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

/**
 * This is the Welcome screen that users see when they first open the app.
 * It shows a welcome message and has a button to continue to the next screen.
 * This activity is like the first page of a book - it introduces the app to users.
 */
public class WelcomeActivity extends AppCompatActivity {

    // This button allows users to continue from the welcome screen to the next page
    private Button continueButton;

    /**
     * This method runs when the Welcome screen is created and shown to the user.
     * It sets up the screen layout and makes the continue button work.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Call the parent class method to properly create the activity
        super.onCreate(savedInstanceState);
        
        // Make the app use the full screen (edge to edge display)
        EdgeToEdge.enable(this);
        
        // Load and display the welcome screen layout from the XML file
        setContentView(R.layout.activity_welcome);
        
        // Handle system bars (like status bar and navigation bar) properly
        // This makes sure the app content doesn't get hidden behind system elements
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (view, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Find the continue button from the layout and set up what happens when clicked
        continueButton = findViewById(R.id.continueButton);
        continueButton.setOnClickListener(new View.OnClickListener() {
            /**
             * This method runs when the user taps the continue button.
             * It takes the user to the "Get Started" screen.
             */
            @Override
            public void onClick(View v) {
                // Create an instruction to go to the GetStarted screen
                Intent intent = new Intent(WelcomeActivity.this, GetStartedActivity.class);
                
                // Start the next screen (GetStarted activity)
                startActivity(intent);
            }
        });
    }
}
