package com.s23010234.devnextdoor;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

/**
 * This is the Sign Up screen where new users create their accounts.
 * It asks for a username, password, and password confirmation.
 * It checks if the username is available and creates the account in Firebase.
 * After successful signup, it takes users to set up their profile.
 */
public class SignupActivity extends AppCompatActivity {

    // Text input field where users type their desired username
    private TextInputEditText usernameInputText;
    
    // Text input field where users type their password
    private TextInputEditText passwordInputText;
    
    // Text input field where users confirm their password by typing it again
    private TextInputEditText confirmPasswordInputText;
    
    // Button users press to create their account
    private Button signupButton;
    
    // Button that takes users back to the login screen if they already have an account
    private Button loginButton;
    
    // Connection to Firebase database where user accounts are stored
    private DatabaseReference databaseReference;

    /**
     * This method runs when the Sign Up screen is created and shown to the user.
     * It sets up all the input fields, buttons, and database connections.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Call the parent class method to properly create the activity
        super.onCreate(savedInstanceState);
        
        // Make the app use the full screen (edge to edge display)
        EdgeToEdge.enable(this);
        
        // Load and display the signup screen layout from the XML file
        setContentView(R.layout.activity_signup);

        // Handle system bars (like status bar and navigation bar) properly
        // This makes sure the app content doesn't get hidden behind system elements
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Find and connect to all the input fields and buttons from the layout
        usernameInputText = findViewById(R.id.timesInputText);
        passwordInputText = findViewById(R.id.passwordInputText);
        confirmPasswordInputText = findViewById(R.id.confirmPasswordInputText);
        signupButton = findViewById(R.id.signupButton);
        loginButton = findViewById(R.id.loginButton);

        // Set up connection to Firebase database where user accounts are stored
        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        // Set up what happens when the signup button is pressed
        signupButton.setOnClickListener(v -> handleSignup());
        
        // Set up what happens when the login button is pressed
        loginButton.setOnClickListener(v -> {
            // Take user back to the login screen
            startActivity(new Intent(SignupActivity.this, LoginActivity.class));
            
            // Close this signup screen
            finish();
        });
    }

    /**
     * This method handles the signup process step by step.
     * It gets the user input, validates it, and creates the account if everything is correct.
     */
    private void handleSignup() {
        // Get the username text from the input field (remove extra spaces)
        String username = usernameInputText.getText() != null ?
                usernameInputText.getText().toString().trim() : "";
                
        // Get the password text from the input field
        String password = passwordInputText.getText() != null ?
                passwordInputText.getText().toString() : "";
                
        // Get the confirmation password text from the input field
        String confirmPassword = confirmPasswordInputText.getText() != null ?
                confirmPasswordInputText.getText().toString() : "";

        // Check if username field is empty
        if (TextUtils.isEmpty(username)) {
            Toast.makeText(SignupActivity.this, "Username required", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if username is the right length (between 4 and 12 characters)
        if (username.length() < 4 || username.length() > 12) {
            Toast.makeText(SignupActivity.this, "Username must be 4-12 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if username contains only letters, numbers, and underscores
        if (!username.matches("^[a-zA-Z0-9_]+$")) {
            Toast.makeText(SignupActivity.this, "Username can only contain letters, numbers, and underscores", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if password field is empty
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(SignupActivity.this, "Password required", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if password is the right length (between 4 and 12 characters)
        if (password.length() < 4 || password.length() > 12) {
            Toast.makeText(SignupActivity.this, "Password must be 4-12 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if both passwords match
        if (!password.equals(confirmPassword)) {
            Toast.makeText(SignupActivity.this, "Passwords must match", Toast.LENGTH_SHORT).show();
            return;
        }

        // If all validation passes, check if the username is available
        checkUsernameAvailability(username, password);
    }

    /**
     * This method checks if the chosen username is already taken.
     * It looks in the Firebase database to see if someone else is using this username.
     */
    private void checkUsernameAvailability(String username, String password) {
        // Look up this username in the database
        databaseReference.child(username).addListenerForSingleValueEvent(new ValueEventListener() {
            /**
             * This method runs when we get a response from the database.
             * It tells us if the username is available or already taken.
             */
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Check if someone is already using this username
                if (dataSnapshot.exists()) {
                    // Username is taken, show error message
                    Toast.makeText(SignupActivity.this, "Username taken", Toast.LENGTH_SHORT).show();
                } else {
                    // Username is available, create the account
                    createUserAccount(username, password);
                }
            }

            /**
             * This method runs if there's an error connecting to the database.
             */
            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Show error message about database connection problem
                Toast.makeText(SignupActivity.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * This method creates a new user account in the Firebase database.
     * It uses the FirebaseHelper to save the username and password.
     * If successful, it takes the user to set up their profile.
     */
    private void createUserAccount(String username, String password) {
        // Use FirebaseHelper to create the new user account with default settings
        FirebaseHelper firebaseHelper = new FirebaseHelper();
        firebaseHelper.addUser(username, password, new FirebaseHelper.DatabaseCallback() {
            /**
             * This method runs if the account was created successfully.
             */
            @Override
            public void onSuccess(boolean result) {
                if (result) {
                    // Show success message to user
                    Toast.makeText(SignupActivity.this, "Account created!", Toast.LENGTH_SHORT).show();
                    
                    // Set light mode as default theme for new user on this device
                    ThemeManager.saveDarkModePreference(SignupActivity.this, false);
                    
                    // Add a welcome notification for the new user
                    addWelcomeNotification(username);
                    
                    // Take user to profile setup instructions
                    Intent intent = new Intent(SignupActivity.this, ProfileInstructionsActivity.class);
                    intent.putExtra("username", username);
                    startActivity(intent);
                    
                    // Close this signup screen
                    finish();
                } else {
                    // Show error message if account creation failed
                    Toast.makeText(SignupActivity.this, "Failed to create account", Toast.LENGTH_SHORT).show();
                }
            }

            /**
             * This method runs if there was an error creating the account.
             */
            @Override
            public void onError(String error) {
                // Show error message with details about what went wrong
                Toast.makeText(SignupActivity.this, "Signup failed: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * This method adds a welcome notification for new users.
     * It congratulates them on creating their account and encourages them to set up their profile.
     */
    private void addWelcomeNotification(String username) {
        // Create a notification manager to handle notifications
        NotificationManager notificationManager = new NotificationManager();
        
        // Create a welcome notification
        Notification welcomeNotification = NotificationManager.createProfileCreatedNotification();
        
        // Add the notification to the user's notification list
        notificationManager.addNotification(username, welcomeNotification, new NotificationManager.NotificationCallback() {
            /**
             * This method runs if the notification was added successfully.
             * We don't need to show anything to the user - it happens in the background.
             */
            @Override
            public void onSuccess(boolean result) {
                // Notification added successfully (silent operation)
            }

            /**
             * This method runs if there was an error adding the notification.
             * We don't need to show an error - notifications are not critical.
             */
            @Override
            public void onError(String error) {
                // Failed to add notification (silent operation)
            }
        });
    }
}
