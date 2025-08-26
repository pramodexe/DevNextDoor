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
 * This is the "Get Started" screen where users choose what they want to do.
 * It shows two options: Login (for existing users) or Sign Up (for new users).
 * Think of this as a fork in the road - users pick their path from here.
 */
public class GetStartedActivity extends AppCompatActivity {

    // Button that takes users to the login screen if they already have an account
    private Button loginButton;
    
    // Button that takes users to the signup screen if they need to create a new account
    private Button signupButton;

    /**
     * This method runs when the Get Started screen is created and shown to the user.
     * It sets up the screen layout and makes both buttons work properly.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Call the parent class method to properly create the activity
        super.onCreate(savedInstanceState);
        
        // Make the app use the full screen (edge to edge display)
        EdgeToEdge.enable(this);
        
        // Load and display the get started screen layout from the XML file
        setContentView(R.layout.activity_get_started);
        
        // Handle system bars (like status bar and navigation bar) properly
        // This makes sure the app content doesn't get hidden behind system elements
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (view, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Find the login button from the layout and set up what happens when clicked
        loginButton = findViewById(R.id.loginButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            /**
             * This method runs when the user taps the login button.
             * It takes users who already have an account to the login screen.
             */
            @Override
            public void onClick(View v) {
                // Create an instruction to go to the Login screen
                Intent intent = new Intent(GetStartedActivity.this, LoginActivity.class);
                
                // Start the login screen
                startActivity(intent);
            }
        });

        // Find the signup button from the layout and set up what happens when clicked
        signupButton = findViewById(R.id.signupButton);
        signupButton.setOnClickListener(new View.OnClickListener() {
            /**
             * This method runs when the user taps the signup button.
             * It takes new users to the signup screen to create an account.
             */
            @Override
            public void onClick(View v) {
                // Create an instruction to go to the Signup screen
                Intent intent = new Intent(GetStartedActivity.this, SignupActivity.class);
                
                // Start the signup screen
                startActivity(intent);
            }
        });
    }
}
