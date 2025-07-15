package com.s23010234.devnextdoor;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

// LoginActivity handles user login by validating credentials against the SQLite database.
public class LoginActivity extends AppCompatActivity {

    // Input fields and buttons
    private TextInputEditText usernameInputText;
    private TextInputEditText passwordInputText;
    private Button loginButton;
    private Button signupButton;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize views
        usernameInputText = findViewById(R.id.timesInputText);
        passwordInputText = findViewById(R.id.passwordInputText);
        loginButton = findViewById(R.id.loginButton);
        signupButton = findViewById(R.id.signupButton);

        // Initialize database helper
        databaseHelper = new DatabaseHelper(this);

        // Set click listener for login button
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleLogin();
            }
        });

        // Navigate to SignupActivity on signup button click
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    // Handles the login process by validating user credentials.
    private void handleLogin() {
        String username = usernameInputText.getText() != null ? usernameInputText.getText().toString().trim() : "";
        String password = passwordInputText.getText() != null ? passwordInputText.getText().toString() : "";

        // Validate inputs
        if (TextUtils.isEmpty(username)) {
            usernameInputText.setError("Username is required");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            passwordInputText.setError("Password is required");
            return;
        }

        // Validate user credentials
        boolean isValidUser = databaseHelper.validateUser(username, password);
        if (isValidUser) {
            Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();
            // Navigate to HomepageActivity after successful login
            Intent intent = new Intent(LoginActivity.this, HomepageActivity.class);
            intent.putExtra("isNewUser", false);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show();
        }
    }
}
