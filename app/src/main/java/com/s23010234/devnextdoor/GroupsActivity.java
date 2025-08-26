package com.s23010234.devnextdoor;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * Groups Activity - The Groups screen of the app
 * 
 * This activity represents the Groups section of the DevNextDoor app.
 * It would be where users can discover, join, and participate in
 * developer groups and communities. Think of it like the "Groups"
 * section in Facebook where people with similar interests gather.
 * 
 * Currently this screen shows the basic layout and navigation,
 * but the actual group features are still being developed.
 * The navigation bar at the bottom allows users to move between
 * different sections of the app.
 */
public class GroupsActivity extends AppCompatActivity {

    /**
     * onCreate - Sets up the Groups screen when it's first created
     * 
     * This method runs when the user navigates to the Groups screen.
     * It sets up the layout, applies the current theme, handles the
     * screen display properly, and sets up the navigation buttons.
     * 
     * @param savedInstanceState Previous state data (if the activity was recreated)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Call the parent class method to properly create the activity
        super.onCreate(savedInstanceState);
        
        // Enable edge-to-edge display for a modern full-screen look
        EdgeToEdge.enable(this);
        
        // Load and display the groups screen layout from the XML file
        setContentView(R.layout.activity_groups);

        // Apply the user's chosen theme (Dark mode, Light mode, etc.)
        // This ensures the screen matches the user's preferred appearance
        ThemeManager.applyTheme(this);

        // Handle system bars (status bar, navigation bar) properly
        // This ensures our content doesn't get hidden behind system elements
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.groupsMain), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Set up all the navigation buttons at the bottom of the screen
        setupNavigationClicks();
    }
    
    /**
     * onResume - Runs when the user returns to this screen
     * 
     * This method is called whenever the user comes back to the Groups screen
     * after being on a different screen. For example, if they go to Settings
     * and change their theme, this ensures the new theme is applied when
     * they return to Groups.
     */
    @Override
    protected void onResume() {
        // Call the parent class method
        super.onResume();
        
        // Reapply the current theme in case the user changed it in Settings
        ThemeManager.applyTheme(this);
    }

    /**
     * Setup Navigation Clicks - Configures all the bottom navigation buttons
     * 
     * This method finds all the navigation buttons at the bottom of the screen
     * and sets up what happens when each one is clicked. Think of it like
     * programming a TV remote - each button needs to know what to do when pressed.
     */
    private void setupNavigationClicks() {
        // Find all the navigation buttons from the layout
        LinearLayout navHomepage = findViewById(R.id.navHomepage);
        LinearLayout navGroups = findViewById(R.id.navGroups);
        LinearLayout navDashboard = findViewById(R.id.navDashboard);
        LinearLayout navNotifications = findViewById(R.id.navNotifications);
        LinearLayout navChats = findViewById(R.id.navChats);

        // Homepage navigation - takes user to the main homepage
        navHomepage.setOnClickListener(v -> {
            Intent intent = new Intent(GroupsActivity.this, HomepageActivity.class);
            startActivity(intent);
        });

        // Groups navigation - user is already here, so just show a message
        navGroups.setOnClickListener(v -> {
            Toast.makeText(GroupsActivity.this, "Already on Groups", Toast.LENGTH_SHORT).show();
        });

        // Dashboard navigation - takes user to their personal dashboard
        navDashboard.setOnClickListener(v -> {
            Intent intent = new Intent(GroupsActivity.this, DashboardActivity.class);
            startActivity(intent);
        });

        // Notifications navigation - takes user to see their notifications
        navNotifications.setOnClickListener(v -> {
            Intent intent = new Intent(GroupsActivity.this, NotificationsActivity.class);
            startActivity(intent);
        });

        // Chats navigation - takes user to their chat conversations
        navChats.setOnClickListener(v -> {
            Intent intent = new Intent(GroupsActivity.this, ChatsActivity.class);
            startActivity(intent);
        });
    }
}
