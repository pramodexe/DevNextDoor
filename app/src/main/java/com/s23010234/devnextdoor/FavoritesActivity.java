package com.s23010234.devnextdoor;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Favorites Activity - The user's favorites list screen
 * 
 * This activity displays all the users that the current user has marked
 * as favorites. Think of it like a bookmark list or contacts favorites
 * where you keep the people you want to easily find again.
 * 
 * Users can:
 * - View all the people they've added to their favorites
 * - Tap on any favorite person to view their full profile
 * - See when their favorites list is empty
 * - Navigate back to other parts of the app
 * 
 * The favorites list loads automatically when the screen opens and
 * updates when users add or remove people from their favorites in
 * other parts of the app. Each favorite person is displayed as a
 * card showing their basic information.
 */
public class FavoritesActivity extends AppCompatActivity {

    // Visual elements for displaying the favorites list
    private RecyclerView favoritesRecyclerView;  // Scrollable list of favorite users
    private FavoriteUserAdapter adapter;         // Manages displaying each favorite user
    private LinearLayout loadingLayout;          // Shown while loading favorites from Firebase
    private LinearLayout emptyStateLayout;       // Shown when user has no favorites yet
    
    // Database and helper objects
    private DatabaseReference databaseReference; // Connection to Firebase database
    private FirebaseHelper firebaseHelper;       // Helper for database operations
    private String currentUsername;              // Username of the person using the app
    private List<User> favoriteUsers;
    private boolean isLoading = false; // Add loading state flag
    
    // Activity result launcher for profile viewing
    private ActivityResultLauncher<Intent> profileViewLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_favorites);
        
        // Apply current theme
        ThemeManager.applyTheme(this);
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.loadingLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        
        // Initialize activity result launcher
        profileViewLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            this::handleProfileViewResult
        );
        
        // Initialize Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference("users");
        firebaseHelper = new FirebaseHelper();
        
        // Get current username
        SharedPreferences sharedPreferences = getSharedPreferences("DevNextDoorPrefs", MODE_PRIVATE);
        currentUsername = sharedPreferences.getString("username", "");
        
        initializeViews();
        setupRecyclerView();
        loadFavoriteUsers();
        setupBackButton();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Only reload if not currently loading and apply theme
        ThemeManager.applyTheme(this);
        // Don't reload favorites on resume to prevent duplicates
        // Favorites are loaded in onCreate and will be refreshed when needed
    }
    
    /**
     * Initialize view components
     */
    private void initializeViews() {
        loadingLayout = findViewById(R.id.loadingLayout);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        favoritesRecyclerView = findViewById(R.id.favoritesRecyclerView);
        
        favoriteUsers = new ArrayList<>();
    }
    
    /**
     * Setup RecyclerView with adapter
     */
    private void setupRecyclerView() {
        adapter = new FavoriteUserAdapter(this, favoriteUsers, profileViewLauncher);
        favoritesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        favoritesRecyclerView.setAdapter(adapter);
    }
    
    /**
     * Load favorite users from Firebase
     */
    private void loadFavoriteUsers() {
        if (currentUsername.isEmpty() || isLoading) {
            if (currentUsername.isEmpty()) {
                showEmptyState();
            }
            return;
        }
        
        isLoading = true; // Set loading flag
        showLoading();
        
        // First get the list of favorite usernames
        firebaseHelper.getFavoritesList(currentUsername, new FirebaseHelper.FavoriteListCallback() {
            @Override
            public void onSuccess(List<String> favoriteUsernames) {
                if (favoriteUsernames.isEmpty()) {
                    isLoading = false; // Reset loading flag
                    showEmptyState();
                    return;
                }
                
                // Load full user data for each favorite
                loadFavoriteUserData(favoriteUsernames);
            }
            
            @Override
            public void onError(String error) {
                isLoading = false; // Reset loading flag
                Toast.makeText(FavoritesActivity.this, "Error loading favorites: " + error, Toast.LENGTH_SHORT).show();
                showEmptyState();
            }
        });
    }
    
    /**
     * Load user data for favorite usernames
     */
    private void loadFavoriteUserData(List<String> favoriteUsernames) {
        favoriteUsers.clear(); // Clear existing list to prevent duplicates
        final int totalFavorites = favoriteUsernames.size();
        final int[] loadedCount = {0};
        
        for (String username : favoriteUsernames) {
            databaseReference.child(username).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        try {
                            // Extract user data from Firebase
                            String gender = dataSnapshot.child("gender").getValue(String.class);
                            String bio = dataSnapshot.child("bio").getValue(String.class);
                            String wantToLearn = dataSnapshot.child("wantToLearn").getValue(String.class);
                            String profilePicture = dataSnapshot.child("profilePicture").getValue(String.class);
                            String level = dataSnapshot.child("level").getValue(String.class);
                            String city = dataSnapshot.child("city").getValue(String.class);
                            String techStack = dataSnapshot.child("techStack").getValue(String.class);
                            String goals = dataSnapshot.child("goals").getValue(String.class);
                            String availability = dataSnapshot.child("availability").getValue(String.class);
                            String timeOfDay = dataSnapshot.child("timeOfDay").getValue(String.class);
                            
                            // Create User object
                            User user = new User(username, gender, bio, wantToLearn, profilePicture,
                                    level, city, techStack, goals, availability, timeOfDay,
                                    true, System.currentTimeMillis());
                            
                            // Check for duplicates before adding
                            boolean userExists = false;
                            for (User existingUser : favoriteUsers) {
                                if (existingUser.getUsername().equals(username)) {
                                    userExists = true;
                                    break;
                                }
                            }
                            
                            if (!userExists) {
                                favoriteUsers.add(user);
                            }
                        } catch (Exception e) {
                            // Skip this user if there's an error parsing their data
                        }
                    }
                    
                    loadedCount[0]++;
                    if (loadedCount[0] >= totalFavorites) {
                        // All favorites loaded, update UI
                        isLoading = false; // Reset loading flag
                        if (favoriteUsers.isEmpty()) {
                            showEmptyState();
                        } else {
                            showFavorites();
                            adapter.updateFavoriteUsers(favoriteUsers);
                        }
                    }
                }
                
                @Override
                public void onCancelled(DatabaseError databaseError) {
                    loadedCount[0]++;
                    if (loadedCount[0] >= totalFavorites) {
                        isLoading = false; // Reset loading flag
                        if (favoriteUsers.isEmpty()) {
                            showEmptyState();
                        } else {
                            showFavorites();
                            adapter.updateFavoriteUsers(favoriteUsers);
                        }
                    }
                }
            });
        }
    }
    
    /**
     * Show loading state
     */
    private void showLoading() {
        loadingLayout.setVisibility(View.VISIBLE);
        favoritesRecyclerView.setVisibility(View.GONE);
        emptyStateLayout.setVisibility(View.GONE);
    }
    
    /**
     * Show favorites RecyclerView
     */
    private void showFavorites() {
        loadingLayout.setVisibility(View.GONE);
        favoritesRecyclerView.setVisibility(View.VISIBLE);
        emptyStateLayout.setVisibility(View.GONE);
    }
    
    /**
     * Show empty state message
     */
    private void showEmptyState() {
        loadingLayout.setVisibility(View.GONE);
        favoritesRecyclerView.setVisibility(View.GONE);
        emptyStateLayout.setVisibility(View.VISIBLE);
    }

    /**
     * Public method to refresh favorites when needed (e.g., after removing a favorite)
     */
    public void refreshFavorites() {
        isLoading = false; // Reset loading flag
        loadFavoriteUsers();
    }

    /**
     * Handle result from profile viewing activity
     */
    private void handleProfileViewResult(ActivityResult result) {
        if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
            boolean favoritesChanged = result.getData().getBooleanExtra("favorites_changed", false);
            if (favoritesChanged) {
                // Refresh the favorites list
                refreshFavorites();
            }
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
}
