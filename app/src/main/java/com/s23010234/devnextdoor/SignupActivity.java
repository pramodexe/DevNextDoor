package com.s23010234.devnextdoor;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.activity.EdgeToEdge;

// SignupActivity handles user registration by collecting user details
// and storing them in the SQLite database.
public class SignupActivity extends AppCompatActivity {

    private TextInputEditText nameInputText;
    private TextInputEditText usernameInputText;
    private TextInputEditText passwordInputText;
    private Button signupButton;
    private Button loginButton;

    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup);

        // Apply system window insets padding for edge-to-edge UI
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize views
        nameInputText = findViewById(R.id.nameInputText);
        usernameInputText = findViewById(R.id.usernameInputText);
        passwordInputText = findViewById(R.id.passwordInputText);
        signupButton = findViewById(R.id.signupButton);
        loginButton = findViewById(R.id.loginButton);

        // Initialize database helper
        databaseHelper = new DatabaseHelper(this);

        // Set click listener for signup button
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleSignup();
            }
        });

        // Navigate to LoginActivity on login button click
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    // Handles the signup process by validating input and inserting user into database.
    private void handleSignup() {
        String name = nameInputText.getText() != null ? nameInputText.getText().toString().trim() : "";
        String username = usernameInputText.getText() != null ? usernameInputText.getText().toString().trim() : "";
        String password = passwordInputText.getText() != null ? passwordInputText.getText().toString() : "";

        // Validate inputs
        if (TextUtils.isEmpty(name)) {
            nameInputText.setError("Name is required");
            return;
        }

        if (TextUtils.isEmpty(username)) {
            usernameInputText.setError("Username is required");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            passwordInputText.setError("Password is required");
            return;
        }

        // Check if username already exists
        if (databaseHelper.isUsernameExists(username)) {
            usernameInputText.setError("Username already exists");
            return;
        }

        // Add user to database
        boolean isUserAdded = databaseHelper.addUser(name, username, password);

        if (isUserAdded) {
            Toast.makeText(this, "Signup successful! Please login.", Toast.LENGTH_SHORT).show();

            // Navigate to LoginActivity after successful signup
            Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Signup failed. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }
}
