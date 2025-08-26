package com.s23010234.devnextdoor;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

/**
 * Settings Activity - The app settings and preferences screen
 * 
 * This activity allows users to customize their app experience and
 * manage their account. Think of it like the control panel for the app
 * where users can adjust various settings and preferences.
 * 
 * Users can:
 * - Toggle between Dark Mode and Light Mode themes
 * - Change their username
 * - Change their password
 * - Delete their account permanently
 * - Log out of the app
 * 
 * The settings are saved using SharedPreferences (for app preferences
 * like theme) and Firebase (for account information like username and
 * password). All changes take effect immediately and are remembered
 * the next time the user opens the app.
 */
public class SettingsActivity extends AppCompatActivity {

    // Objects needed for managing settings and user data
    private SharedPreferences sharedPreferences;  // For storing app preferences locally
    private FirebaseHelper firebaseHelper;        // For updating user account information
    private String currentUsername;               // The current user's username

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

            // Apply current theme
            ThemeManager.applyTheme(this);
            
            // Initialize views and listeners
            setupBackButton();
            setupDarkModeToggle();
            setupUsernameChangeFunctionality();
            setupPasswordChangeFunctionality();
            setupLogoutFunctionality();
            setupDeleteAccountFunctionality();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error loading settings: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Sets up the back button functionality
     */
    private void setupBackButton() {
        try {
            ImageView backArrow = findViewById(R.id.backArrow);
            if (backArrow != null) {
                backArrow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish(); // Go back to previous activity
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error setting up back button: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Sets up the dark mode toggle switch functionality
     * Loads current preference from Firebase and sets up switch listener
     */
    private void setupDarkModeToggle() {
        try {
            Switch darkModeSwitch = findViewById(R.id.dark_mode_switch);
            if (darkModeSwitch != null && !currentUsername.isEmpty()) {
                // We need to prevent infinite loops, so we'll use a flag to track if we're in the middle of a programmatic change
                final boolean[] isSettingProgrammatically = {false};
                
                // Set initial switch state from local preference to prevent flicker
                boolean localDarkMode = ThemeManager.isDarkModeEnabled(this);
                isSettingProgrammatically[0] = true;
                darkModeSwitch.setChecked(localDarkMode);
                isSettingProgrammatically[0] = false;
                
                // Set switch listener
                darkModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    // Skip if this is a programmatic change, not a user interaction
                    if (isSettingProgrammatically[0]) {
                        return; // Ignore programmatic changes
                    }
                    
                    if (currentUsername.isEmpty()) {
                        Toast.makeText(this, "Error: User not logged in", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Update Firebase preference
                    firebaseHelper.updateDarkModePreference(currentUsername, isChecked, new FirebaseHelper.DatabaseCallback() {
                        @Override
                        public void onSuccess(boolean result) {
                            if (result) {
                                // Show toast for user feedback
                                String mode = isChecked ? "Dark" : "Light";
                                Toast.makeText(SettingsActivity.this, mode + " mode enabled", Toast.LENGTH_SHORT).show();
                                
                                // Apply theme globally for both dark and light mode changes
                                // This ensures all activities in the stack get properly updated
                                ThemeManager.applyThemeGlobally(SettingsActivity.this, isChecked);
                            } else {
                                // Reset switch on failure - use the flag to prevent triggering listener
                                isSettingProgrammatically[0] = true;
                                darkModeSwitch.setChecked(!isChecked);
                                isSettingProgrammatically[0] = false;
                                Toast.makeText(SettingsActivity.this, "Failed to update theme preference", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onError(String error) {
                            // Reset switch on error - use the flag to prevent triggering listener
                            isSettingProgrammatically[0] = true;
                            darkModeSwitch.setChecked(!isChecked);
                            isSettingProgrammatically[0] = false;
                            Toast.makeText(SettingsActivity.this, "Error updating theme: " + error, Toast.LENGTH_SHORT).show();
                        }
                    });
                });
                
                // Load current dark mode preference from Firebase
                firebaseHelper.getDarkModePreference(currentUsername, new FirebaseHelper.DarkModeCallback() {
                    @Override
                    public void onResult(boolean isDarkMode) {
                        // Set switch state using flag to prevent triggering the listener
                        isSettingProgrammatically[0] = true;
                        darkModeSwitch.setChecked(isDarkMode);
                        isSettingProgrammatically[0] = false;
                        
                        // Save the preference locally
                        ThemeManager.saveDarkModePreference(SettingsActivity.this, isDarkMode);
                        
                        // Apply theme without recreation
                        ThemeManager.applyThemeToActivity(SettingsActivity.this, isDarkMode);
                    }

                    @Override
                    public void onError(String error) {
                        // If error loading from Firebase, use local preference
                        boolean localDarkMode = ThemeManager.isDarkModeEnabled(SettingsActivity.this);
                        isSettingProgrammatically[0] = true;
                        darkModeSwitch.setChecked(localDarkMode);
                        isSettingProgrammatically[0] = false;
                        Toast.makeText(SettingsActivity.this, "Could not load theme preference: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error setting up dark mode toggle: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void setupUsernameChangeFunctionality() {
        try {
            CardView changeUsernameCard = findViewById(R.id.change_username_card);
            if (changeUsernameCard != null) {
                changeUsernameCard.setOnClickListener(v -> showChangeUsernameDialog());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupPasswordChangeFunctionality() {
        try {
            CardView changePasswordCard = findViewById(R.id.change_password_card);
            if (changePasswordCard != null) {
                changePasswordCard.setOnClickListener(v -> showChangePasswordDialog());
            }
        } catch (Exception e) {
            e.printStackTrace();
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

    private void showChangeUsernameDialog() {
        // Create EditText for username input
        EditText usernameInput = new EditText(this);
        usernameInput.setHint("Enter new username");
        usernameInput.setText(currentUsername); // Pre-fill with current username
        usernameInput.setSelection(usernameInput.getText().length()); // Move cursor to end

        new AlertDialog.Builder(this)
                .setTitle("Change Username")
                .setMessage("Enter your new username:")
                .setView(usernameInput)
                .setPositiveButton("Update", (dialog, which) -> {
                    String newUsername = usernameInput.getText().toString().trim();
                    if (newUsername.isEmpty()) {
                        Toast.makeText(this, "Username cannot be empty", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (newUsername.equals(currentUsername)) {
                        Toast.makeText(this, "New username is the same as current username", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    performUsernameChange(newUsername);
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void showChangePasswordDialog() {
        // Create EditText for password input
        EditText passwordInput = new EditText(this);
        passwordInput.setHint("Enter new password");
        passwordInput.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);

        new AlertDialog.Builder(this)
                .setTitle("Change Password")
                .setMessage("Enter your new password:")
                .setView(passwordInput)
                .setPositiveButton("Update", (dialog, which) -> {
                    String newPassword = passwordInput.getText().toString().trim();
                    if (newPassword.isEmpty()) {
                        Toast.makeText(this, "Password cannot be empty", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (newPassword.length() < 3) {
                        Toast.makeText(this, "Password must be at least 3 characters long", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    performPasswordChange(newPassword);
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void performUsernameChange(String newUsername) {
        if (currentUsername.isEmpty()) {
            Toast.makeText(this, "Error: Current user not found", Toast.LENGTH_SHORT).show();
            return;
        }

        String oldUsername = currentUsername;

        // Show loading message with more detail
        Toast.makeText(this, "Updating username...", Toast.LENGTH_SHORT).show();

        firebaseHelper.updateUsername(currentUsername, newUsername, new FirebaseHelper.DatabaseCallback() {
            @Override
            public void onSuccess(boolean result) {
                if (result) {
                    // Update SharedPreferences with new username
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("username", newUsername);
                    editor.apply();

                    // Add username change notification
                    addUsernameChangeNotification(newUsername, oldUsername, newUsername);

                    // Update current username variable
                    currentUsername = newUsername;

                    Toast.makeText(SettingsActivity.this, "Username changed successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(SettingsActivity.this, "Failed to update username", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String error) {
                Toast.makeText(SettingsActivity.this, "Error updating username: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void performPasswordChange(String newPassword) {
        if (currentUsername.isEmpty()) {
            Toast.makeText(this, "Error: Current user not found", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading message
        Toast.makeText(this, "Updating password...", Toast.LENGTH_SHORT).show();

        firebaseHelper.updatePassword(currentUsername, newPassword, new FirebaseHelper.DatabaseCallback() {
            @Override
            public void onSuccess(boolean result) {
                if (result) {
                    // Add password change notification
                    addPasswordChangeNotification(currentUsername);
                    
                    Toast.makeText(SettingsActivity.this, "Password updated successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(SettingsActivity.this, "Failed to update password", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String error) {
                Toast.makeText(SettingsActivity.this, "Error updating password: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void addPasswordChangeNotification(String username) {
        NotificationManager notificationManager = new NotificationManager();
        Notification passwordNotification = NotificationManager.createPasswordChangedNotification();
        
        notificationManager.addNotification(username, passwordNotification, new NotificationManager.NotificationCallback() {
            @Override
            public void onSuccess(boolean result) {
                // Notification added successfully (no need to show message to user)
            }

            @Override
            public void onError(String error) {
                // Failed to add notification (no need to show error to user)
            }
        });
    }

    private void addUsernameChangeNotification(String username, String oldUsername, String newUsername) {
        NotificationManager notificationManager = new NotificationManager();
        Notification usernameNotification = NotificationManager.createUsernameChangedNotification(oldUsername, newUsername);
        
        notificationManager.addNotification(username, usernameNotification, new NotificationManager.NotificationCallback() {
            @Override
            public void onSuccess(boolean result) {
                // Notification added successfully (no need to show message to user)
            }

            @Override
            public void onError(String error) {
                // Failed to add notification (no need to show error to user)
            }
        });
    }

}
