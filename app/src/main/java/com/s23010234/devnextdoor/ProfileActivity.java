package com.s23010234.devnextdoor;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * This is the Profile screen that shows detailed information about a user.
 * It can display either the current user's own profile or another user's profile.
 * The screen shows profile picture, bio, skills, availability, goals, and more.
 * Users can chat with others, add them to favorites, or edit their own profile.
 * Think of it as a detailed business card that shows everything about a person.
 */
public class ProfileActivity extends AppCompatActivity {

    // Visual elements that display the user's profile information
    private ImageView profilePicture;      // The user's profile photo
    private ImageView genderIcon;          // Small icon showing male/female
    private TextView usernameText;         // The user's display name
    private TextView bioText;              // Short description about the user
    private TextView statusText;           // User's current status or level
    private TextView locationText;         // Where the user lives
    private TextView techStackText;        // Programming languages they know
    private TextView wantToLearnText;      // What they want to learn
    private TextView projectGoalsText;     // Their learning goals
    private ImageView backArrow;           // Button to go back to previous screen
    
    // Action buttons that appear when viewing other users' profiles
    private ImageView chatIcon;            // Button to start a chat with this user
    private ImageView favoriteIcon;        // Button to add/remove user from favorites
    
    // Indicators that show when the user is available
    private TextView weekdayIndicator;     // Shows if available on weekdays
    private TextView weekendIndicator;     // Shows if available on weekends
    private TextView morningIndicator;     // Shows if available in morning
    private TextView dayIndicator;         // Shows if available during day
    private TextView eveningIndicator;     // Shows if available in evening
    private TextView nightIndicator;       // Shows if available at night
    
    // Layout elements for different screen states
    private LinearLayout loadingLayout;    // Shown while profile is loading
    private ScrollView profileContent;     // The main profile information area
    
    // Database and helper objects
    private DatabaseReference databaseReference;  // Connection to Firebase database
    private String currentUsername;                // Username of profile being viewed
    private String currentUser;                   // Username of logged-in user
    private FirebaseHelper firebaseHelper;        // Helper for database operations
    private ChatManager chatManager;              // Helper for chat functionality
    private boolean isFavorite = false;           // Whether this user is in favorites

    /**
     * This method runs when the Profile screen is created and shown to the user.
     * It sets up the layout, gets user information, and loads the profile data.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Call the parent class method to properly create the activity
        super.onCreate(savedInstanceState);
        
        // Make the app use the full screen (edge to edge display)
        EdgeToEdge.enable(this);
        
        // Load and display the profile screen layout from the XML file
        setContentView(R.layout.activity_profile);
        
        // Apply the user's preferred theme (dark or light mode)
        ThemeManager.applyTheme(this);
        
        // Handle system bars (like status bar and navigation bar) properly
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize database connections and helper objects
        // These objects help us communicate with Firebase and handle chat functionality
        databaseReference = FirebaseDatabase.getInstance().getReference("users");
        firebaseHelper = new FirebaseHelper();
        chatManager = new ChatManager();
        
        // Figure out whose profile we're viewing
        // The username can come from two places:
        // 1. From another screen (when viewing someone else's profile)
        // 2. From SharedPreferences (when viewing our own profile)
        String viewUsername = getIntent().getStringExtra("username");
        SharedPreferences sharedPreferences = getSharedPreferences("DevNextDoorPrefs", MODE_PRIVATE);
        String myUsername = sharedPreferences.getString("username", "");
        currentUser = myUsername; // Remember who the logged-in user is
        
        // Decide which profile to show:
        // If a username was passed from another screen, show that person's profile
        // Otherwise, show our own profile
        currentUsername = (viewUsername != null) ? viewUsername : myUsername;

        // Set up all the visual elements and load the profile data
        initializeViews(myUsername);
        loadUserProfileFromFirebase();
        setupClickListeners();
        
        // If we're viewing someone else's profile, check if they're in our favorites
        if (!currentUsername.equals(currentUser)) {
            checkFavoriteStatus();
        }
    }
    
    /**
     * onResume - Runs when the user returns to this screen
     * 
     * This method is called whenever the user comes back to the Profile screen
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
     * This method finds all the TextViews, ImageViews, and other visual
     * elements from the layout file and stores references to them.
     * It also decides which elements to show based on whether this is
     * the user's own profile or someone else's profile.
     * 
     * @param myUsername The username of the currently logged-in user
     */
    private void initializeViews(String myUsername) {
        // Find the loading and content areas
        loadingLayout = findViewById(R.id.loadingLayout);
        profileContent = findViewById(R.id.profileContent);
        
        // Find all the profile information display elements
        profilePicture = findViewById(R.id.profilePicture);
        genderIcon = findViewById(R.id.genderIcon);
        usernameText = findViewById(R.id.usernameText);
        bioText = findViewById(R.id.bioText);
        statusText = findViewById(R.id.statusText);
        locationText = findViewById(R.id.locationText);
        techStackText = findViewById(R.id.techStackText);
        wantToLearnText = findViewById(R.id.wantToLearnText);
        projectGoalsText = findViewById(R.id.projectGoalsText);
        backArrow = findViewById(R.id.backArrow);
        
        // Find the action buttons (chat and favorite)
        chatIcon = findViewById(R.id.chatIcon);
        favoriteIcon = findViewById(R.id.favoriteIcon);
        
        // Decide whether to show the action buttons
        // Only show them when viewing someone else's profile, not our own
        LinearLayout actionIconsContainer = findViewById(R.id.actionIconsContainer);
        if (currentUsername.equals(myUsername)) {
            // This is our own profile - hide the chat and favorite buttons
            actionIconsContainer.setVisibility(View.GONE);
        } else {
            // This is someone else's profile - show the action buttons
            actionIconsContainer.setVisibility(View.VISIBLE);
        }
        
        // Find all the availability indicator elements
        weekdayIndicator = findViewById(R.id.weekdayIndicator);
        weekendIndicator = findViewById(R.id.weekendIndicator);
        morningIndicator = findViewById(R.id.morningIndicator);
        dayIndicator = findViewById(R.id.dayIndicator);
        eveningIndicator = findViewById(R.id.eveningIndicator);
        nightIndicator = findViewById(R.id.nightIndicator);
    }

    /**
     * Load User Profile From Firebase - Gets profile data and displays it
     * 
     * This method connects to Firebase, downloads the user's profile information,
     * and then fills in all the visual elements with that data. It's like
     * downloading a digital business card and then displaying it on screen.
     */
    private void loadUserProfileFromFirebase() {
        // Make sure we have a valid username to look up
        if (currentUsername.isEmpty()) {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Display the username with an @ symbol
        usernameText.setText("@" + currentUsername);

        // Connect to Firebase and get this user's profile data
        databaseReference.child(currentUsername).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    try {
                        // Extract all the profile information from Firebase
                        // Think of this like reading different fields from a digital form
                        String gender = dataSnapshot.child("gender").getValue(String.class);
                        String bio = dataSnapshot.child("bio").getValue(String.class);
                        String level = dataSnapshot.child("level").getValue(String.class);
                        String city = dataSnapshot.child("city").getValue(String.class);
                        String availability = dataSnapshot.child("availability").getValue(String.class);
                        String timeOfDay = dataSnapshot.child("timeOfDay").getValue(String.class);
                        String techStack = dataSnapshot.child("techStack").getValue(String.class);
                        String wantToLearn = dataSnapshot.child("wantToLearn").getValue(String.class);
                        String goals = dataSnapshot.child("goals").getValue(String.class);
                        String profilePictureFileName = dataSnapshot.child("profilePicture").getValue(String.class);

                        // Update UI with real data
                        updateUserInterface(gender, bio, level, city, availability, timeOfDay, 
                                          techStack, wantToLearn, goals, profilePictureFileName);
                        
                    } catch (Exception e) {
                        Toast.makeText(ProfileActivity.this, "Error loading profile data", Toast.LENGTH_SHORT).show();
                        loadDefaultData();
                    }
                } else {
                    Toast.makeText(ProfileActivity.this, "Profile not found", Toast.LENGTH_SHORT).show();
                    loadDefaultData();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ProfileActivity.this, "Database error: " + databaseError.getMessage(), 
                             Toast.LENGTH_SHORT).show();
                loadDefaultData();
            }
        });
    }

    /**
     * Update UI components with fetched user data
     * @param gender User's gender selection
     * @param bio User's bio text
     * @param level User's education level
     * @param city User's city
     * @param availability User's availability (Weekdays/Weekends)
     * @param timeOfDay User's preferred time (Morning/Day/Evening/Night)
     * @param techStack User's current tech stack
     * @param wantToLearn Technologies user wants to learn
     * @param goals User's project goals
     * @param profilePictureFileName Profile picture file name
     */
    private void updateUserInterface(String gender, String bio, String level, String city, 
                                   String availability, String timeOfDay, String techStack, 
                                   String wantToLearn, String goals, String profilePictureFileName) {
        
        // Set bio
        bioText.setText(bio != null ? bio : "No bio available");
        
        // Set status (education level)
        statusText.setText(level != null ? level : "Not specified");
        
        // Set location
        locationText.setText(city != null ? city : "Not specified");
        
        // Set tech stack (format as bullet points)
        techStackText.setText(formatAsBulletPoints(techStack));
        
        // Set want to learn (format as bullet points)
        wantToLearnText.setText(formatAsBulletPoints(wantToLearn));
        
        // Set goals
        projectGoalsText.setText(goals != null ? goals : "No goals specified");
        
        // Set profile picture based on filename and gender
        setProfilePictureAndGenderIcon(gender, profilePictureFileName);
        
        // Set availability indicators
        setAvailabilityIndicators(availability, timeOfDay);
        
        // Show content after all data is loaded
        showContent();
    }

    /**
     * Format availability and time of day information
     * @param availability Weekdays/Weekends selection
     * @param timeOfDay Morning/Day/Evening/Night selection
     * @return Formatted availability string
     */
    private String formatAvailabilityText(String availability, String timeOfDay) {
        StringBuilder result = new StringBuilder();
        
        if (availability != null && !availability.isEmpty()) {
            // Convert to abbreviations (WD for Weekdays, WE for Weekends)
            String abbreviated = availability.replace("Weekdays", "WD")
                                           .replace("Weekends", "WE")
                                           .replace(", ", "/");
            result.append(abbreviated);
        } else {
            result.append("Not specified");
        }
        
        if (timeOfDay != null && !timeOfDay.isEmpty()) {
            result.append("\n").append(timeOfDay.toLowerCase());
        }
        
        return result.toString();
    }

    /**
     * Format text as bullet points for better readability
     * @param text Input text (comma-separated or line-separated)
     * @return Formatted text with bullet points
     */
    private String formatAsBulletPoints(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "Not specified";
        }
        
        // Split by comma or newline and add bullet points
        String[] items = text.split("[,\n]+");
        StringBuilder formatted = new StringBuilder();
        
        for (String item : items) {
            String trimmed = item.trim();
            if (!trimmed.isEmpty()) {
                if (formatted.length() > 0) {
                    formatted.append("\n");
                }
                formatted.append("- ").append(trimmed);
            }
        }
        
        return formatted.toString();
    }

    /**
     * Set profile picture and gender icon based on user's selections
     * @param gender User's gender
     * @param profilePictureFileName Profile picture file name
     */
    private void setProfilePictureAndGenderIcon(String gender, String profilePictureFileName) {
        // Set gender icon
        if ("Male".equals(gender)) {
            genderIcon.setImageResource(R.drawable.male_icon);
        } else if ("Female".equals(gender)) {
            genderIcon.setImageResource(R.drawable.female_icon);
        } else {
            genderIcon.setImageResource(R.drawable.male_icon); // Default
        }
        
        // Set profile picture based on filename
        if (profilePictureFileName != null && !profilePictureFileName.isEmpty()) {
            String drawableName = profilePictureFileName.replace(".png", "");
            int resourceId = getResources().getIdentifier(drawableName, "drawable", getPackageName());
            
            if (resourceId != 0) {
                profilePicture.setImageResource(resourceId);
            } else {
                // Fallback to default based on gender
                profilePicture.setImageResource("Male".equals(gender) ? R.drawable.male_1 : R.drawable.female_1);
            }
        } else {
            // Default profile picture based on gender
            profilePicture.setImageResource("Male".equals(gender) ? R.drawable.male_1 : R.drawable.female_1);
        }
    }

    /**
     * Set availability indicators based on user selections
     * @param availability User's availability (Weekdays/Weekends)
     * @param timeOfDay User's preferred time (Morning/Day/Evening/Night)
     */
    private void setAvailabilityIndicators(String availability, String timeOfDay) {
        // Default backgrounds
        Drawable defaultCircleBackground = ContextCompat.getDrawable(this, R.drawable.circle_background);
        Drawable selectedCircleBackground = ContextCompat.getDrawable(this, R.drawable.selected_circle_background_cream);
        Drawable defaultBoxBackground = ContextCompat.getDrawable(this, R.drawable.time_box_bg);
        Drawable selectedBoxBackground = ContextCompat.getDrawable(this, R.drawable.selected_background_cream);
        
        // Set weekday/weekend indicators
        if (availability != null && availability.contains("Weekdays")) {
            weekdayIndicator.setBackground(selectedCircleBackground);
            weekdayIndicator.setTextColor(getResources().getColor(R.color.profile_box_text_color));
            weekdayIndicator.setTypeface(null, Typeface.BOLD);
        } else {
            weekdayIndicator.setBackground(defaultCircleBackground);
            weekdayIndicator.setTextColor(getResources().getColor(R.color.profile_box_text_color));
            weekdayIndicator.setTypeface(null, Typeface.NORMAL);
        }
        
        if (availability != null && availability.contains("Weekends")) {
            weekendIndicator.setBackground(selectedCircleBackground);
            weekendIndicator.setTextColor(getResources().getColor(R.color.profile_box_text_color));
            weekendIndicator.setTypeface(null, Typeface.BOLD);
        } else {
            weekendIndicator.setBackground(defaultCircleBackground);
            weekendIndicator.setTextColor(getResources().getColor(R.color.profile_box_text_color));
            weekendIndicator.setTypeface(null, Typeface.NORMAL);
        }
        
        // Set time of day indicators
        if (timeOfDay != null && timeOfDay.contains("Morning")) {
            morningIndicator.setBackground(selectedBoxBackground);
            morningIndicator.setTextColor(getResources().getColor(R.color.profile_box_text_color));
            morningIndicator.setTypeface(null, Typeface.BOLD);
        } else {
            morningIndicator.setBackground(defaultBoxBackground);
            morningIndicator.setTextColor(getResources().getColor(R.color.profile_box_text_color));
            morningIndicator.setTypeface(null, Typeface.NORMAL);
        }
        
        if (timeOfDay != null && timeOfDay.contains("Day")) {
            dayIndicator.setBackground(selectedBoxBackground);
            dayIndicator.setTextColor(getResources().getColor(R.color.profile_box_text_color));
            dayIndicator.setTypeface(null, Typeface.BOLD);
        } else {
            dayIndicator.setBackground(defaultBoxBackground);
            dayIndicator.setTextColor(getResources().getColor(R.color.profile_box_text_color));
            dayIndicator.setTypeface(null, Typeface.NORMAL);
        }
        
        if (timeOfDay != null && timeOfDay.contains("Evening")) {
            eveningIndicator.setBackground(selectedBoxBackground);
            eveningIndicator.setTextColor(getResources().getColor(R.color.profile_box_text_color));
            eveningIndicator.setTypeface(null, Typeface.BOLD);
        } else {
            eveningIndicator.setBackground(defaultBoxBackground);
            eveningIndicator.setTextColor(getResources().getColor(R.color.profile_box_text_color));
            eveningIndicator.setTypeface(null, Typeface.NORMAL);
        }
        
        if (timeOfDay != null && timeOfDay.contains("Night")) {
            nightIndicator.setBackground(selectedBoxBackground);
            nightIndicator.setTextColor(getResources().getColor(R.color.profile_box_text_color));
            nightIndicator.setTypeface(null, Typeface.BOLD);
        } else {
            nightIndicator.setBackground(defaultBoxBackground);
            nightIndicator.setTextColor(getResources().getColor(R.color.profile_box_text_color));
            nightIndicator.setTypeface(null, Typeface.NORMAL);
        }
    }

    /**
     * Load default data if Firebase fetch fails
     */
    private void loadDefaultData() {
        bioText.setText("No profile data available");
        statusText.setText("Not specified");
        locationText.setText("Not specified");
        techStackText.setText("Not specified");
        wantToLearnText.setText("Not specified");
        projectGoalsText.setText("Not specified");
        profilePicture.setImageResource(R.drawable.male_1);
        genderIcon.setImageResource(R.drawable.male_icon);
        
        // Set default availability indicators (none selected)
        setAvailabilityIndicators(null, null);
        
        // Show content even with default data
        showContent();
    }

    /**
     * Hide loading indicator and show profile content
     */
    private void showContent() {
        loadingLayout.setVisibility(View.GONE);
        profileContent.setVisibility(View.VISIBLE);
    }

    /**
     * Setup click listeners for interactive elements
    */
    private void setupClickListeners() {
        // Back arrow click listener
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Go back to previous activity
            }
        });

        // Set up chat icon click listener
        chatIcon.setOnClickListener(v -> startChat());
        
        // Set up favorite icon click listener
        favoriteIcon.setOnClickListener(v -> toggleFavorite());
    }

    /**
     * Check if the current user is a favorite of the logged-in user
     */
    private void checkFavoriteStatus() {
        if (currentUser == null || currentUsername.isEmpty()) {
            return;
        }
        
        firebaseHelper.isFavorite(currentUser, currentUsername, new FirebaseHelper.FavoritesCallback() {
            @Override
            public void onSuccess(boolean result) {
                isFavorite = result;
                updateFavoriteIcon();
            }
            
            @Override
            public void onError(String error) {
                Toast.makeText(ProfileActivity.this, "Error checking favorite status: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    /**
     * Toggle the favorite status of the current user
     */
    private void toggleFavorite() {
        if (currentUser == null || currentUsername.isEmpty()) {
            Toast.makeText(this, "Error: Unable to update favorites", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (isFavorite) {
            // Remove from favorites
            firebaseHelper.removeFromFavorites(currentUser, currentUsername, new FirebaseHelper.DatabaseCallback() {
                @Override
                public void onSuccess(boolean result) {
                    isFavorite = false;
                    updateFavoriteIcon();
                    Toast.makeText(ProfileActivity.this, "Removed from favorites", Toast.LENGTH_SHORT).show();
                }
                
                @Override
                public void onError(String error) {
                    Toast.makeText(ProfileActivity.this, "Failed to remove from favorites: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Add to favorites
            firebaseHelper.addToFavorites(currentUser, currentUsername, new FirebaseHelper.DatabaseCallback() {
                @Override
                public void onSuccess(boolean result) {
                    isFavorite = true;
                    updateFavoriteIcon();
                    Toast.makeText(ProfileActivity.this, "Added to favorites", Toast.LENGTH_SHORT).show();
                }
                
                @Override
                public void onError(String error) {
                    Toast.makeText(ProfileActivity.this, "Failed to add to favorites: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    
    /**
     * Start a chat with this user
     */
    private void startChat() {
        if (currentUser == null || currentUsername.isEmpty()) {
            Toast.makeText(this, "Error: Unable to start chat", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String chatId = Chat.generateChatId(currentUser, currentUsername);
        
        // Create chat if it doesn't exist, then open it
        chatManager.createChat(currentUser, currentUsername, new ChatManager.DatabaseCallback() {
            @Override
            public void onSuccess(boolean result) {
                // Open chat activity
                Intent intent = new Intent(ProfileActivity.this, ChatActivity.class);
                intent.putExtra("chatId", chatId);
                intent.putExtra("otherUser", currentUsername);
                startActivity(intent);
            }

            @Override
            public void onError(String error) {
                Toast.makeText(ProfileActivity.this, "Failed to start chat: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    /**
     * Update the favorite icon based on current favorite status
     */
    private void updateFavoriteIcon() {
        if (isFavorite) {
            favoriteIcon.setImageResource(R.drawable.ic_favorite);
            favoriteIcon.clearColorFilter(); // Clear any existing tint
            favoriteIcon.setColorFilter(getResources().getColor(android.R.color.holo_red_dark));
        } else {
            favoriteIcon.setImageResource(R.drawable.ic_favorite_border);
            favoriteIcon.clearColorFilter(); // Clear any existing tint
            favoriteIcon.setColorFilter(getResources().getColor(R.color.favorite_icon_unfavorited));
        }
    }
}
