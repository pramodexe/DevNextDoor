package com.s23010234.devnextdoor;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

/**
 * This is the Login screen where existing users enter their username and password.
 * It checks if the login information is correct by looking it up in the Firebase database.
 * If the login is successful, it takes the user to the main homepage of the app.
 */
public class LoginActivity extends AppCompatActivity {

    // Text input field where users type their username
    private TextInputEditText usernameInputText;
    
    // Text input field where users type their password
    private TextInputEditText passwordInputText;
    
    // Button users press to attempt logging in
    private Button loginButton;
    
    // Button that takes users to the signup screen if they don't have an account yet
    private Button signupButton;
    
    // Connection to Firebase database where user information is stored
    private DatabaseReference databaseReference;

    /**
     * This method runs when the Login screen is created and shown to the user.
     * It sets up all the input fields and buttons on the screen.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Call the parent class method to properly create the activity
        super.onCreate(savedInstanceState);
        
        // Load and display the login screen layout from the XML file
        setContentView(R.layout.activity_login);

        // Find and connect to all the input fields and buttons from the layout
        usernameInputText = findViewById(R.id.timesInputText);
        passwordInputText = findViewById(R.id.passwordInputText);
        loginButton = findViewById(R.id.loginButton);
        signupButton = findViewById(R.id.signupButton);

        // Set up connection to Firebase database where user accounts are stored
        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        // Set up what happens when the login button is pressed
        loginButton.setOnClickListener(new View.OnClickListener() {
            /**
             * This method runs when the user taps the login button.
             * It starts the process of checking their username and password.
             */
            @Override
            public void onClick(View view) {
                handleLogin();
            }
        });

        // Set up what happens when the signup button is pressed
        signupButton.setOnClickListener(new View.OnClickListener() {
            /**
             * This method runs when the user taps the signup button.
             * It takes them to the signup screen to create a new account.
             */
            @Override
            public void onClick(View view) {
                // Create an instruction to go to the Signup screen
                Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                
                // Start the signup screen
                startActivity(intent);
                
                // Close this login screen since user is going to signup instead
                finish();
            }
        });
    }

    /**
     * This method checks if the username and password are correct.
     * It looks up the user information in the Firebase database.
     * If the credentials match, it logs the user in and takes them to the homepage.
     */
    private void validateUserCredentials(String username, String password) {
        // Look up the user's information in the database using their username
        databaseReference.child(username).addListenerForSingleValueEvent(new ValueEventListener() {
            /**
             * This method runs when we get a response from the database.
             * It checks if the user exists and if their password is correct.
             */
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Check if a user with this username exists in the database
                if (dataSnapshot.exists()) {
                    // Get the stored password for this username
                    String storedPassword = dataSnapshot.child("password").getValue(String.class);

                    // Check if the entered password matches the stored password
                    if (storedPassword != null && storedPassword.equals(password)) {
                        // Show success message to user
                        Toast.makeText(LoginActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();

                        // Save the username in device storage so app remembers the user is logged in
                        SharedPreferences sharedPreferences = getSharedPreferences("DevNextDoorPrefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("username", username);
                        editor.apply();

                        // Get the user's theme preference (dark or light mode) from Firebase
                        FirebaseHelper firebaseHelper = new FirebaseHelper();
                        firebaseHelper.getDarkModePreference(username, new FirebaseHelper.DarkModeCallback() {
                            /**
                             * This method runs when we successfully get the user's theme preference.
                             * It saves the preference and takes the user to the homepage.
                             */
                            @Override
                            public void onResult(boolean isDarkMode) {
                                // Save the user's theme preference on the device
                                ThemeManager.saveDarkModePreference(LoginActivity.this, isDarkMode);

                                // Create instruction to go to the main homepage
                                Intent intent = new Intent(LoginActivity.this, HomepageActivity.class);
                                intent.putExtra("isNewUser", false);

                                // Clear all previous screens so user can't go back to login
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                                // Start the homepage
                                startActivity(intent);

                                // Close this login screen
                                finish();
                            }

                            /**
                             * This method runs if there's an error getting the theme preference.
                             * It uses a default theme and still takes the user to the homepage.
                             */
                            @Override
                            public void onError(String error) {
                                // Use light mode as default if we can't get user's preference
                                ThemeManager.saveDarkModePreference(LoginActivity.this, false);

                                // Create instruction to go to the main homepage
                                Intent intent = new Intent(LoginActivity.this, HomepageActivity.class);
                                intent.putExtra("isNewUser", false);

                                // Clear all previous screens so user can't go back to login
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                                // Start the homepage
                                startActivity(intent);

                                // Close this login screen
                                finish();
                            }
                        });
                    } else {
                        // Show error message if password is wrong
                        Toast.makeText(LoginActivity.this, "Invalid username or password", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Show error message if username doesn't exist
                    Toast.makeText(LoginActivity.this, "Invalid username or password", Toast.LENGTH_SHORT).show();
                }
            }

            /**
             * This method runs if there's an error connecting to the database.
             * It shows an error message to the user.
             */
            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Show error message about database connection problem
                Toast.makeText(LoginActivity.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * This method handles the login process step by step.
     * It gets the username and password from the input fields,
     * checks if they are valid, and then verifies them with the database.
     */
    private void handleLogin() {
        // Get the username text from the input field (remove extra spaces)
        String username = usernameInputText.getText() != null ?
                usernameInputText.getText().toString().trim() : "";

        // Get the password text from the input field
        String password = passwordInputText.getText() != null ?
                passwordInputText.getText().toString() : "";

        // Check if username field is empty
        if (TextUtils.isEmpty(username)) {
            // Show error message to user if username is missing
            Toast.makeText(LoginActivity.this, "Username is required", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if password field is empty
        if (TextUtils.isEmpty(password)) {
            // Show error message to user if password is missing
            Toast.makeText(LoginActivity.this, "Password is required", Toast.LENGTH_SHORT).show();
            return;
        }

        // If both username and password are provided, check if they are correct
        validateUserCredentials(username, password);
    }
}
