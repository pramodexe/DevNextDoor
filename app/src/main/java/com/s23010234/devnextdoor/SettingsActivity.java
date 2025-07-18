package com.s23010234.devnextdoor;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class SettingsActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;
    private FirebaseHelper firebaseHelper;
    private String currentUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            setContentView(R.layout.activity_settings);

            // Initialize SharedPreferences and FirebaseHelper
            sharedPreferences = getSharedPreferences("DevNextDoorPrefs", MODE_PRIVATE);
            firebaseHelper = new FirebaseHelper();

            // Get current username from SharedPreferences
            currentUsername = sharedPreferences.getString("username", "");

            // Initialize views and listeners
            setupLogoutFunctionality();
            setupDeleteAccountFunctionality();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error loading settings: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void setupLogoutFunctionality() {
        try {
            CardView logoutCard = findViewById(R.id.logout_card);
            if (logoutCard != null) {
                logoutCard.setOnClickListener(v -> showLogoutDialog());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupDeleteAccountFunctionality() {
        try {
            CardView deleteAccountCard = findViewById(R.id.delete_account_card);
            if (deleteAccountCard != null) {
                deleteAccountCard.setOnClickListener(v -> showDeleteAccountDialog());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> performLogout())
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void showDeleteAccountDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to permanently delete your account?\n\nThis action cannot be undone and all your data will be lost.")
                .setPositiveButton("Delete", (dialog, which) -> showFinalDeleteConfirmation())
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void showFinalDeleteConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Final Confirmation")
                .setMessage("This is your final warning!\n\nDeleting your account will permanently remove all your data from our servers. Are you absolutely sure?")
                .setPositiveButton("Yes, Delete Forever", (dialog, which) -> performDeleteAccount())
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void performLogout() {
        // Clear user session data
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        // Show logout success message
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();

        // Navigate to GetStarted screen
        Intent intent = new Intent(SettingsActivity.this, GetStartedActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void performDeleteAccount() {
        if (currentUsername.isEmpty()) {
            Toast.makeText(this, "Error: User not found", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading message
        Toast.makeText(this, "Deleting account...", Toast.LENGTH_SHORT).show();

        // Delete user from Firebase database
        firebaseHelper.deleteUser(currentUsername, new FirebaseHelper.DatabaseCallback() {
            @Override
            public void onSuccess(boolean result) {
                if (result) {
                    // Clear local data only after successful database deletion
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.clear();
                    editor.apply();

                    // Show success message
                    Toast.makeText(SettingsActivity.this, "Account deleted successfully", Toast.LENGTH_LONG).show();

                    // Navigate to GetStarted screen
                    Intent intent = new Intent(SettingsActivity.this, GetStartedActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(SettingsActivity.this, "Failed to delete account", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String error) {
                Toast.makeText(SettingsActivity.this, "Error deleting account: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }
}
