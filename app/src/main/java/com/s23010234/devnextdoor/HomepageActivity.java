package com.s23010234.devnextdoor;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * This is the main Homepage screen that users see after logging in.
 * It shows a grid of user profile cards for people they can connect with.
 * Users can browse through different profiles and tap on them to view more details.
 * The homepage also has navigation buttons to access other parts of the app.
 * This extends ShakeBaseActivity to enable shake-to-refresh functionality.
 */
public class HomepageActivity extends ShakeBaseActivity {

    // The scrollable grid that displays user profile cards
    private RecyclerView userProfilesRecyclerView;
    
    // The adapter that manages how user cards are displayed in the grid
    private UserProfileCardAdapter adapter;
    
    // Layout shown while user profiles are being loaded from the database
    private LinearLayout loadingLayout;
    
    // Layout shown when there are no users to display
    private LinearLayout emptyStateLayout;
    
    // Connection to Firebase database where user profiles are stored
    private DatabaseReference databaseReference;
    
    // The username of the current logged-in user
    private String currentUsername;
    
    // List that holds all the user profiles to be displayed
    private List<User> userList;

    /**
     * This method runs when the Homepage screen is created and shown to the user.
     * It sets up the layout, loads user profiles, and prepares all the functionality.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Call the parent class method to properly create the activity
        super.onCreate(savedInstanceState);
        
        // Make the app use the full screen (edge to edge display)
        EdgeToEdge.enable(this);
        
        // Load and display the homepage layout from the XML file
        setContentView(R.layout.activity_homepage);

        // Apply the user's preferred theme (dark or light mode)
        ThemeManager.applyTheme(this);

        // Handle system bars (like status bar and navigation bar) properly
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Set up connection to Firebase database where user profiles are stored
        databaseReference = FirebaseDatabase.getInstance().getReference("users");
        
        // Get the username of the currently logged-in user from device storage
        SharedPreferences sharedPreferences = getSharedPreferences("DevNextDoorPrefs", MODE_PRIVATE);
        currentUsername = sharedPreferences.getString("username", "");

        // Set up shake detection so users can refresh by shaking their phone
        initializeShakeDetection();

        // Set up all the visual elements on the screen
        initializeViews();
        
        // Set up the grid that will display user profile cards
        setupRecyclerView();
        
        // Load user profiles from the database and display them
        loadUserProfiles();
        
        // Set up the navigation buttons at the bottom of the screen
        setupNavigationClicks();
    }
    
    /**
     * This method handles what happens when the user presses the back button.
     * Instead of going back to login screens, it moves the app to the background.
     * This prevents users from accidentally logging out.
     */
    @Override
    public void onBackPressed() {
        // Don't let users go back to authentication screens by accident
        // Instead, move the app to the background (like pressing the home button)
        moveTaskToBack(true);
    }
    
    /**
     * This method runs every time the user returns to the Homepage screen.
     * It refreshes the user profiles to show any new users who might have joined.
     */
    @Override
    protected void onResume() {
        super.onResume();
        // Reapply theme when returning to this activity (e.g., from Settings)
        ThemeManager.applyTheme(this);
        // Reload user profiles to get latest data
        loadUserProfiles();
    }
    
    /**
     * Initialize view components
     */
    private void initializeViews() {
        userProfilesRecyclerView = findViewById(R.id.userProfilesRecyclerView);
        loadingLayout = findViewById(R.id.loadingLayout);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        userList = new ArrayList<>();
    }
    
    /**
     * Setup RecyclerView with GridLayoutManager for 2 columns
     */
    private void setupRecyclerView() {
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        userProfilesRecyclerView.setLayoutManager(layoutManager);
        
        adapter = new UserProfileCardAdapter(this, userList, currentUsername);
        userProfilesRecyclerView.setAdapter(adapter);
    }
    
    /**
     * Load user profiles from Firebase and populate the RecyclerView
     */
    private void loadUserProfiles() {
        showLoading();
        
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userList.clear();
                
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    try {
                        String username = userSnapshot.getKey();
                        
                        // Skip current user and users without completed profiles
                        if (username != null && !username.equals(currentUsername)) {
                            Boolean profileCompleted = userSnapshot.child("profileCompleted").getValue(Boolean.class);
                            if (profileCompleted != null && profileCompleted) {
                                
                                // Extract user data
                                String gender = userSnapshot.child("gender").getValue(String.class);
                                String bio = userSnapshot.child("bio").getValue(String.class);
                                String wantToLearn = userSnapshot.child("wantToLearn").getValue(String.class);
                                String profilePicture = userSnapshot.child("profilePicture").getValue(String.class);
                                String level = userSnapshot.child("level").getValue(String.class);
                                String city = userSnapshot.child("city").getValue(String.class);
                                String techStack = userSnapshot.child("techStack").getValue(String.class);
                                String goals = userSnapshot.child("goals").getValue(String.class);
                                String availability = userSnapshot.child("availability").getValue(String.class);
                                String timeOfDay = userSnapshot.child("timeOfDay").getValue(String.class);
                                
                                // Get timestamp of profile creation/completion
                                // IMPORTANT: Do NOT default to current time, as that falsely marks old users as NEW
                                Long timestamp = userSnapshot.child("timestamp").getValue(Long.class);
                                if (timestamp == null) {
                                    timestamp = 0L; // Missing timestamp -> treat as oldest
                                }
                                
                                // Create User object
                                User user = new User(username, gender, bio, wantToLearn, profilePicture,
                                                   level, city, techStack, goals, availability, timeOfDay,
                                                   true, timestamp);
                                
                                userList.add(user);
                            }
                        }
                    } catch (Exception e) {
                        // Skip this user if there's an error parsing their data
                        continue;
                    }
                }
                
                // Sort users - newest first (based on timestamp)
                Collections.sort(userList, new Comparator<User>() {
                    @Override
                    public int compare(User u1, User u2) {
                        return Long.compare(u2.getTimestamp(), u1.getTimestamp());
                    }
                });
                
                // Update UI
                if (userList.isEmpty()) {
                    showEmptyState();
                } else {
                    showUserProfiles();
                }
                
                adapter.updateUserList(userList);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(HomepageActivity.this, "Failed to load user profiles: " + databaseError.getMessage(), 
                             Toast.LENGTH_SHORT).show();
                showEmptyState();
            }
        });
    }
    
    /**
     * Show loading state
     */
    private void showLoading() {
        loadingLayout.setVisibility(View.VISIBLE);
        userProfilesRecyclerView.setVisibility(View.GONE);
        emptyStateLayout.setVisibility(View.GONE);
    }
    
    /**
     * Show user profiles RecyclerView
     */
    private void showUserProfiles() {
        loadingLayout.setVisibility(View.GONE);
        userProfilesRecyclerView.setVisibility(View.VISIBLE);
        emptyStateLayout.setVisibility(View.GONE);
    }
    
    /**
     * Show empty state message
     */
    private void showEmptyState() {
        loadingLayout.setVisibility(View.GONE);
        userProfilesRecyclerView.setVisibility(View.GONE);
        emptyStateLayout.setVisibility(View.VISIBLE);
    }

    private void setupNavigationClicks() {
        LinearLayout navHomepage = findViewById(R.id.navHomepage);
        LinearLayout navSearch = findViewById(R.id.navSearch);
        LinearLayout navDashboard = findViewById(R.id.navDashboard);
        LinearLayout navNotifications = findViewById(R.id.navNotifications);
        LinearLayout navChats = findViewById(R.id.navChats);

        navHomepage.setOnClickListener(v -> {
            Toast.makeText(HomepageActivity.this, "Already on Homepage", Toast.LENGTH_SHORT).show();
        });

        navSearch.setOnClickListener(v -> {
            Intent intent = new Intent(HomepageActivity.this, SearchActivity.class);
            startActivity(intent);
        });

        navDashboard.setOnClickListener(v -> {
            Intent intent = new Intent(HomepageActivity.this, DashboardActivity.class);
            startActivity(intent);
        });

        navNotifications.setOnClickListener(v -> {
            Intent intent = new Intent(HomepageActivity.this, NotificationsActivity.class);
            startActivity(intent);
        });

        navChats.setOnClickListener(v -> {
            Intent intent = new Intent(HomepageActivity.this, ChatsActivity.class);
            startActivity(intent);
        });
    }
}
