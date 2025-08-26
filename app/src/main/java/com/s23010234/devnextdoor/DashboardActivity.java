package com.s23010234.devnextdoor;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * Dashboard Activity - The user's personal control center
 * 
 * This activity serves as the main dashboard where users can access
 * all the key features of the app. Think of it like the home screen
 * of the app where users can:
 * - View their profile
 * - Edit their profile information
 * - Access settings (themes, preferences)
 * - View their favorites list
 * - Meet new friends
 * - Navigate to other parts of the app
 * 
 * The dashboard extends ShakeBaseActivity, which means it can detect
 * when the user shakes their phone to quickly access certain features.
 * It also handles theme changes properly when users return from Settings.
 */
public class DashboardActivity extends ShakeBaseActivity {

    // Variables to handle theme changes when returning from Settings
    private boolean themeChangedInSettings = false;           // Tracks if theme was changed
    private ActivityResultLauncher<Intent> settingsLauncher;  // Handles returning from Settings

    /**
     * onCreate - Sets up the Dashboard screen when it's first created
     * 
     * This method runs when the user navigates to the Dashboard screen.
     * It sets up the layout, applies the current theme, handles system
     * bars properly, initializes shake detection, and sets up all the
     * clickable elements (navigation and dashboard boxes).
     * 
     * @param savedInstanceState Previous state data (if the activity was recreated)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Call the parent class method to properly create the activity
        super.onCreate(savedInstanceState);
        
        // Enable edge-to-edge display for a modern full-screen look
        EdgeToEdge.enable(this);
        
        // Load and display the dashboard screen layout from the XML file
        setContentView(R.layout.activity_dashboard);

        // Apply the user's chosen theme (Dark mode, Light mode, etc.)
        ThemeManager.applyTheme(this);

        // Set up a special launcher for the Settings screen
        // This is needed because when users change themes in Settings,
        // we need to recreate this screen to apply the new theme
        settingsLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                // When returning from Settings, recreate the activity to apply any theme changes
                recreate();
            }
        );

        // Handle system bars (status bar, navigation bar) properly
        // This ensures our content doesn't get hidden behind system elements
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.dashboardContent), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Set up shake detection (inherited from ShakeBaseActivity)
        // This allows users to shake their phone for quick actions
        initializeShakeDetection();

        // Set up all the clickable elements on the screen
        setupNavigationClicks();    // Bottom navigation bar
        setupDashboardBoxClicks();  // Main dashboard boxes (Profile, Settings, etc.)
    }

    /**
     * Setup Navigation Clicks - Configures the bottom navigation bar
     * 
     * This method sets up all the navigation buttons at the bottom of the screen.
     * Each button takes users to a different section of the app. The dashboard
     * button shows a message since the user is already on the dashboard.
     */
    private void setupNavigationClicks() {
        // Find all the navigation buttons from the layout
        LinearLayout navHomepage = findViewById(R.id.navHomepage);
        LinearLayout navSearch = findViewById(R.id.navSearch);
        LinearLayout navDashboard = findViewById(R.id.navDashboard);
        LinearLayout navNotifications = findViewById(R.id.navNotifications);
        LinearLayout navChats = findViewById(R.id.navChats);

        // Homepage navigation - takes user to the main homepage
        navHomepage.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, HomepageActivity.class);
            startActivity(intent);
        });

        // Search navigation - takes user to search for other users
        navSearch.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, SearchActivity.class);
            startActivity(intent);
        });

        // Dashboard navigation - user is already here, so just show a message
        navDashboard.setOnClickListener(v -> {
            Toast.makeText(DashboardActivity.this, "Already on Dashboard", Toast.LENGTH_SHORT).show();
        });

        // Notifications navigation - takes user to see their notifications
        navNotifications.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, NotificationsActivity.class);
            startActivity(intent);
        });

        // Chats navigation - takes user to their chat conversations
        navChats.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, ChatsActivity.class);
            startActivity(intent);
        });
    }

    /**
     * Setup Dashboard Box Clicks - Configures the main dashboard feature boxes
     * 
     * This method sets up all the main feature boxes on the dashboard screen.
     * These boxes give users quick access to key app features like viewing
     * their profile, editing their profile, changing settings, etc.
     */
    private void setupDashboardBoxClicks() {
        // Profile box - shows the user's profile information
        findViewById(R.id.boxProfile).setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        // Settings box - opens app settings (themes, preferences, etc.)
        // Uses special launcher to handle theme changes properly
        findViewById(R.id.boxSettings).setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, SettingsActivity.class);
            settingsLauncher.launch(intent);
        });

        // Favorites box - shows users the people they've favorited
        findViewById(R.id.boxFavorites).setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, FavoritesActivity.class);
            startActivity(intent);
        });

        // Meet Friends box - helps users discover and connect with new people
        findViewById(R.id.boxMeetFriends).setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, MeetFriendsActivity.class);
            startActivity(intent);
        });

        // Edit Profile box - allows users to modify their profile information
        findViewById(R.id.boxEditProfile).setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, EditProfileActivity.class);
            
            // Get the current user's username from saved preferences
            android.content.SharedPreferences sharedPreferences = getSharedPreferences("DevNextDoorPrefs", MODE_PRIVATE);
            String currentUsername = sharedPreferences.getString("username", "");
            
            // Pass information to EditProfileActivity so it knows this is an editing session
            intent.putExtra("username", currentUsername);        // Which user to edit
            intent.putExtra("isEditing", true);                  // Flag indicating this is editing mode
            startActivity(intent);
        });
    }

}
