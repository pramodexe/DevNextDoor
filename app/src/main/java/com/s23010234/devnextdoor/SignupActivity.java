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

// Handles user registration
public class SignupActivity extends AppCompatActivity {

    private TextInputEditText usernameInputText;
    private TextInputEditText passwordInputText;
    private TextInputEditText confirmPasswordInputText;
    private Button signupButton;
    private Button loginButton;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize views
        usernameInputText = findViewById(R.id.usernameInputText);
        passwordInputText = findViewById(R.id.passwordInputText);
        confirmPasswordInputText = findViewById(R.id.confirmPasswordInputText);
        signupButton = findViewById(R.id.signupButton);
        loginButton = findViewById(R.id.loginButton);
        databaseHelper = new DatabaseHelper(this);

        signupButton.setOnClickListener(v -> handleSignup());

        loginButton.setOnClickListener(v -> {
            startActivity(new Intent(SignupActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void handleSignup() {
        String username = usernameInputText.getText() != null ?
                usernameInputText.getText().toString().trim() : "";
        String password = passwordInputText.getText() != null ?
                passwordInputText.getText().toString() : "";
        String confirmPassword = confirmPasswordInputText.getText() != null ?
                confirmPasswordInputText.getText().toString() : "";

        // Validate inputs
        if (TextUtils.isEmpty(username)) {
            usernameInputText.setError("Username required");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            passwordInputText.setError("Password required");
            return;
        }
        if (!password.equals(confirmPassword)) {
            confirmPasswordInputText.setError("Passwords must match");
            return;
        }

        // Check username availability
        if (databaseHelper.isUsernameExists(username)) {
            usernameInputText.setError("Username taken");
            return;
        }

        // Create account
        if (databaseHelper.addUser(username, password)) {
            Toast.makeText(this, "Account created!", Toast.LENGTH_SHORT).show();
            // Redirect new users to EditProfileActivity
            startActivity(new Intent(this, EditProfileActivity.class));
            finish();
        } else {
            Toast.makeText(this, "Signup failed", Toast.LENGTH_SHORT).show();
        }
    }
}
