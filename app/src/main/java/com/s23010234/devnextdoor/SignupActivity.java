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

public class SignupActivity extends AppCompatActivity {

    private TextInputEditText usernameInputText;
    private TextInputEditText passwordInputText;
    private TextInputEditText confirmPasswordInputText;
    private Button signupButton;
    private Button loginButton;
    private DatabaseReference databaseReference;

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
        usernameInputText = findViewById(R.id.timesInputText);
        passwordInputText = findViewById(R.id.passwordInputText);
        confirmPasswordInputText = findViewById(R.id.confirmPasswordInputText);
        signupButton = findViewById(R.id.signupButton);
        loginButton = findViewById(R.id.loginButton);

        // Initialize Firebase database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("users");

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
        checkUsernameAvailability(username, password);
    }

    private void checkUsernameAvailability(String username, String password) {
        databaseReference.child(username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    usernameInputText.setError("Username taken");
                } else {
                    createUserAccount(username, password);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(SignupActivity.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // THIS METHOD MUST BE INSIDE THE CLASS
    private void createUserAccount(String username, String password) {
        Map<String, Object> user = new HashMap<>();
        user.put("username", username);
        user.put("password", password);

        databaseReference.child(username).setValue(user)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Account created!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(this, EditProfileActivity.class);
                    intent.putExtra("username", username); // Pass username to EditProfileActivity
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Signup failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
