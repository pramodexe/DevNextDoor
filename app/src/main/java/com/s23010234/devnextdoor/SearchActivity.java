package com.s23010234.devnextdoor;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Search Activity - The user search and discovery screen
 * 
 * This activity allows users to search for and discover other developers
 * on the platform. Think of it like a search engine specifically for
 * finding other users based on different criteria. Users can:
 * - Search by username (find specific people)
 * - Search by tech stack (find people who know certain programming languages)
 * - Search by what they want to learn (find study partners)
 * - Browse all users
 * 
 * The results are displayed in a grid layout showing user profile cards.
 * The search happens in real-time as the user types, and they can filter
 * results using chips (filter buttons) at the top.
 */
public class SearchActivity extends ShakeBaseActivity {

    // Visual elements for search interface
    private EditText searchEditText;           // Text box where users type their search
    private ChipGroup searchFilterChips;       // Container for filter buttons
    private Chip chipAll, chipUsername, chipTechStack, chipWantToLearn;  // Filter buttons
    private RecyclerView searchResultsRecyclerView;  // Grid that shows search results
    private LinearLayout loadingLayout;        // Shown while loading users from database
    private LinearLayout emptyStateLayout;     // Shown when no search results found
    private TextView emptyStateText;           // Message explaining why no results found

    // Database and user data management
    private DatabaseReference databaseReference;  // Connection to Firebase database
    private String currentUsername;               // Username of the person using the app
    private List<User> allUsers;                 // Complete list of all users from database
    private List<User> filteredUsers;            // Users that match current search/filter
    private UserProfileCardAdapter adapter;      // Manages displaying user cards in the grid

    /**
     * Search Filter Types - Different ways to search for users
     * 
     * These represent the different filter options available to users.
     * Each filter searches through different parts of user profiles.
     */
    private enum SearchFilter {
        ALL,            // Search through all profile information
        USERNAME,       // Search only usernames
        TECH_STACK,     // Search only programming languages/skills
        WANT_TO_LEARN   // Search only what users want to learn
    }
    private SearchFilter currentFilter = SearchFilter.ALL;  // Currently selected filter

    /**
     * onCreate - Sets up the Search screen when it's first created
     * 
     * This method runs when the user navigates to the Search screen.
     * It sets up the layout, connects to Firebase, initializes all the
     * visual elements, and loads the list of users that can be searched.
     * 
     * @param savedInstanceState Previous state data (if the activity was recreated)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Call the parent class method to properly create the activity
        super.onCreate(savedInstanceState);
        
        // Enable edge-to-edge display for a modern full-screen look
        EdgeToEdge.enable(this);
        
        // Load and display the search screen layout from the XML file
        setContentView(R.layout.activity_search);
        
        // Apply the user's chosen theme (Dark mode, Light mode, etc.)
        ThemeManager.applyTheme(this);
        
        // Handle system bars (status bar, navigation bar) properly
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        
        // Connect to Firebase database and get the current user's information
        databaseReference = FirebaseDatabase.getInstance().getReference("users");
        SharedPreferences sharedPreferences = getSharedPreferences("DevNextDoorPrefs", MODE_PRIVATE);
        currentUsername = sharedPreferences.getString("username", "");

        // Set up shake detection (inherited from ShakeBaseActivity)
        initializeShakeDetection();

        // Set up all the components of the search screen
        initializeViews();           // Find and set up visual elements
        setupRecyclerView();         // Set up the grid that shows search results
        setupSearchFunctionality();  // Make the search box work
        setupFilterChips();          // Make the filter buttons work
        setupNavigationClicks();     // Set up bottom navigation
        
        // Download all users from Firebase so we can search through them
        loadAllUsers();
    }
    
    /**
     * onResume - Runs when the user returns to this screen
     * 
     * This method is called whenever the user comes back to the Search screen
     * after being on a different screen. It reapplies the theme in case the
     * user changed it while they were away.
     */
    @Override
    protected void onResume() {
        super.onResume();
        // Reapply the current theme in case the user changed it in Settings
        ThemeManager.applyTheme(this);
    }
    
    /**
     * Initialize Views - Find and set up all the visual elements
     * 
     * This method finds all the visual elements from the layout file
     * and stores references to them. It also sets up the initial
     * appearance and creates empty lists for storing user data.
     */
    private void initializeViews() {
        // Find the search text box and make sure text appears in black
        searchEditText = findViewById(R.id.searchEditText);
        searchEditText.setTextColor(android.graphics.Color.BLACK);
        
        // Find all the filter components
        searchFilterChips = findViewById(R.id.searchFilterChips);
        chipAll = findViewById(R.id.chipAll);
        chipUsername = findViewById(R.id.chipUsername);
        chipTechStack = findViewById(R.id.chipTechStack);
        chipWantToLearn = findViewById(R.id.chipWantToLearn);
        
        // Find the results display components
        searchResultsRecyclerView = findViewById(R.id.searchResultsRecyclerView);
        loadingLayout = findViewById(R.id.loadingLayout);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        emptyStateText = findViewById(R.id.emptyStateText);
        
        // Create empty lists to store user data
        allUsers = new ArrayList<>();      // Will hold all users from Firebase
        filteredUsers = new ArrayList<>(); // Will hold users that match current search
    }

    /**
     * Setup RecyclerView - Configures the grid that displays search results
     * 
     * This method sets up the RecyclerView to display user profile cards
     * in a 2-column grid layout. Think of it like arranging a photo grid
     * where each photo represents one user's profile card.
     */
    private void setupRecyclerView() {
        // Create a grid layout with 2 columns
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        searchResultsRecyclerView.setLayoutManager(layoutManager);
        
        // Create the adapter that will manage displaying user cards
        adapter = new UserProfileCardAdapter(this, filteredUsers, currentUsername);
        searchResultsRecyclerView.setAdapter(adapter);
    }

    /**
     * Setup Search Functionality - Makes the search box work in real-time
     * 
     * This method sets up a text watcher that listens for changes in the
     * search box. Every time the user types or deletes a character, it
     * automatically searches through the user list and updates the results.
     * It's like having a search that updates as you type.
     */
    private void setupSearchFunctionality() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // This runs before the text changes - we don't need to do anything here
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // This runs while the text is changing - we don't need to do anything here
            }

            @Override
            public void afterTextChanged(Editable s) {
                // This runs after the text has changed - perform the search
                performSearch(s.toString().trim());
            }
        });
    }

    /**
     * Setup Filter Chips - Makes the filter buttons work
     * 
     * This method sets up the chip group (filter buttons) so that when
     * users tap on different filters, the search results change accordingly.
     * Only one filter can be active at a time.
     */
    private void setupFilterChips() {
        searchFilterChips.setOnCheckedStateChangeListener(new ChipGroup.OnCheckedStateChangeListener() {
            @Override
            public void onCheckedChanged(ChipGroup group, List<Integer> checkedIds) {
                if (checkedIds.isEmpty()) {
                    // If no filter is selected, default to "All"
                    chipAll.setChecked(true);
                    currentFilter = SearchFilter.ALL;
                } else {
                    // Figure out which filter was selected and update accordingly
                    int checkedId = checkedIds.get(0);
                    if (checkedId == R.id.chipAll) {
                        currentFilter = SearchFilter.ALL;
                    } else if (checkedId == R.id.chipUsername) {
                        currentFilter = SearchFilter.USERNAME;
                    } else if (checkedId == R.id.chipTechStack) {
                        currentFilter = SearchFilter.TECH_STACK;
                    } else if (checkedId == R.id.chipWantToLearn) {
                        currentFilter = SearchFilter.WANT_TO_LEARN;
                    }
                }
                
                // Re-perform search with current query
                String currentQuery = searchEditText.getText() != null ? 
                    searchEditText.getText().toString().trim() : "";
                performSearch(currentQuery);
            }
        });
    }

    /**
     * Load all users from Firebase for searching
     */
    private void loadAllUsers() {
        showLoading();
        
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                allUsers.clear();
                
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
                                
                                // Get timestamp
                                Long timestamp = userSnapshot.child("timestamp").getValue(Long.class);
                                if (timestamp == null) {
                                    timestamp = 0L;
                                }
                                
                                // Create User object
                                User user = new User(username, gender, bio, wantToLearn, profilePicture,
                                                   level, city, techStack, goals, availability, timeOfDay,
                                                   true, timestamp);
                                
                                allUsers.add(user);
                            }
                        }
                    } catch (Exception e) {
                        // Skip this user if there's an error parsing their data
                        continue;
                    }
                }
                
                // Show initial empty state
                showEmptyState();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(SearchActivity.this, 
                    "Failed to load users: " + databaseError.getMessage(), 
                    Toast.LENGTH_SHORT).show();
                showEmptyState();
            }
        });
    }

    /**
     * Perform search based on query and current filter
     */
    private void performSearch(String query) {
        filteredUsers.clear();
        
        if (query.isEmpty()) {
            // Show empty state with instructions when no query
            showEmptyState();
            emptyStateText.setText("Search for users by username, tech stack, or what they want to learn!");
            adapter.updateUserList(filteredUsers);
            return;
        }
        
        String lowercaseQuery = query.toLowerCase();
        
        for (User user : allUsers) {
            boolean matches = false;
            
            switch (currentFilter) {
                case ALL:
                    matches = matchesAnyField(user, lowercaseQuery);
                    break;
                case USERNAME:
                    matches = matchesUsername(user, lowercaseQuery);
                    break;
                case TECH_STACK:
                    matches = matchesTechStack(user, lowercaseQuery);
                    break;
                case WANT_TO_LEARN:
                    matches = matchesWantToLearn(user, lowercaseQuery);
                    break;
            }
            
            if (matches) {
                filteredUsers.add(user);
            }
        }
        
        // Update UI based on results
        if (filteredUsers.isEmpty()) {
            showEmptyState();
            emptyStateText.setText("No users found matching \"" + query + "\"");
        } else {
            showResults();
        }
        
        adapter.updateUserList(filteredUsers);
    }

    /**
     * Check if user matches query in any searchable field
     */
    private boolean matchesAnyField(User user, String query) {
        return matchesUsername(user, query) ||
               matchesTechStack(user, query) ||
               matchesWantToLearn(user, query);
    }

    /**
     * Check if user's username matches query
     */
    private boolean matchesUsername(User user, String query) {
        return user.getUsername() != null && 
               user.getUsername().toLowerCase().contains(query);
    }

    /**
     * Check if user's tech stack matches query
     */
    private boolean matchesTechStack(User user, String query) {
        return user.getTechStack() != null && 
               user.getTechStack().toLowerCase().contains(query);
    }

    /**
     * Check if user's want to learn matches query
     */
    private boolean matchesWantToLearn(User user, String query) {
        return user.getWantToLearn() != null && 
               user.getWantToLearn().toLowerCase().contains(query);
    }

    /**
     * Show loading state
     */
    private void showLoading() {
        loadingLayout.setVisibility(View.VISIBLE);
        searchResultsRecyclerView.setVisibility(View.GONE);
        emptyStateLayout.setVisibility(View.GONE);
    }

    /**
     * Show search results
     */
    private void showResults() {
        loadingLayout.setVisibility(View.GONE);
        searchResultsRecyclerView.setVisibility(View.VISIBLE);
        emptyStateLayout.setVisibility(View.GONE);
    }

    /**
     * Show empty state
     */
    private void showEmptyState() {
        loadingLayout.setVisibility(View.GONE);
        searchResultsRecyclerView.setVisibility(View.GONE);
        emptyStateLayout.setVisibility(View.VISIBLE);
    }

    /**
     * Sets up navigation clicks
     */
    private void setupNavigationClicks() {
        LinearLayout navHomepage = findViewById(R.id.navHomepage);
        LinearLayout navSearch = findViewById(R.id.navSearch);
        LinearLayout navDashboard = findViewById(R.id.navDashboard);
        LinearLayout navNotifications = findViewById(R.id.navNotifications);
        LinearLayout navChats = findViewById(R.id.navChats);

        navHomepage.setOnClickListener(v -> {
            Intent intent = new Intent(SearchActivity.this, HomepageActivity.class);
            startActivity(intent);
        });

        navSearch.setOnClickListener(v -> {
            Toast.makeText(SearchActivity.this, "Already on Search", Toast.LENGTH_SHORT).show();
        });

        navDashboard.setOnClickListener(v -> {
            Intent intent = new Intent(SearchActivity.this, DashboardActivity.class);
            startActivity(intent);
        });

        navNotifications.setOnClickListener(v -> {
            Intent intent = new Intent(SearchActivity.this, NotificationsActivity.class);
            startActivity(intent);
        });

        navChats.setOnClickListener(v -> {
            Intent intent = new Intent(SearchActivity.this, ChatsActivity.class);
            startActivity(intent);
        });
    }
}
