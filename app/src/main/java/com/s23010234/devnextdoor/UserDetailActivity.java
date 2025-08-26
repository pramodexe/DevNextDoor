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
 * Activity to display another user's profile with chat and favorite functionality
 */
public class UserDetailActivity extends AppCompatActivity {

    private ImageView profilePicture;
    private ImageView genderIcon;
    private TextView usernameText;
    private TextView bioText;
    private TextView statusText;
    private TextView locationText;
    private TextView techStackText;
    private TextView wantToLearnText;
    private TextView projectGoalsText;
    private ImageView backArrow;
    private ImageView chatIcon;
    private ImageView favoriteIcon;
    
    // Availability indicators
    private TextView weekdayIndicator;
    private TextView weekendIndicator;
    private TextView morningIndicator;
    private TextView dayIndicator;
    private TextView eveningIndicator;
    private TextView nightIndicator;
    
    // Loading state components
    private LinearLayout loadingLayout;
    private ScrollView profileContent;
    
    private DatabaseReference databaseReference;
    private FirebaseHelper firebaseHelper;
    private ChatManager chatManager;
    private String currentUsername;
    private String targetUsername;
    private boolean isFavorite = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_detail);
        
        // Apply current theme
        ThemeManager.applyTheme(this);
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firebase and managers
        databaseReference = FirebaseDatabase.getInstance().getReference("users");
        firebaseHelper = new FirebaseHelper();
        chatManager = new ChatManager();
        
        // Get usernames
        SharedPreferences sharedPreferences = getSharedPreferences("DevNextDoorPrefs", MODE_PRIVATE);
        currentUsername = sharedPreferences.getString("username", "");
        targetUsername = getIntent().getStringExtra("username");

        if (targetUsername == null || targetUsername.isEmpty()) {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();
        loadUserProfileFromFirebase();
        setupClickListeners();
        checkIfFavorite();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Reapply theme when returning to this activity (e.g., from Settings)
        ThemeManager.applyTheme(this);
    }

    /**
     * Initialize all view components from the layout
     */
    private void initializeViews() {
        // Loading state components
        loadingLayout = findViewById(R.id.loadingLayout);
        profileContent = findViewById(R.id.profileContent);
        
        // Profile components
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
        
        // Action icons - show them for other users' profiles
        LinearLayout actionIconsContainer = findViewById(R.id.actionIconsContainer);
        actionIconsContainer.setVisibility(View.VISIBLE);
        chatIcon = findViewById(R.id.chatIcon);
        favoriteIcon = findViewById(R.id.favoriteIcon);
        
        // Availability indicators
        weekdayIndicator = findViewById(R.id.weekdayIndicator);
        weekendIndicator = findViewById(R.id.weekendIndicator);
        morningIndicator = findViewById(R.id.morningIndicator);
        dayIndicator = findViewById(R.id.dayIndicator);
        eveningIndicator = findViewById(R.id.eveningIndicator);
        nightIndicator = findViewById(R.id.nightIndicator);
    }

    /**
     * Load user profile data from Firebase and display in UI
     */
    private void loadUserProfileFromFirebase() {
        if (targetUsername.isEmpty()) {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Set username with @ symbol
        usernameText.setText("@" + targetUsername);

        // Fetch user data from Firebase
        databaseReference.child(targetUsername).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    try {
                        // Extract user data from Firebase
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
                        Toast.makeText(UserDetailActivity.this, "Error loading profile data", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } else {
                    Toast.makeText(UserDetailActivity.this, "Profile not found", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(UserDetailActivity.this, "Database error: " + databaseError.getMessage(), 
                             Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    /**
     * Update UI components with fetched user data
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
     * Format text as bullet points for better readability
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
     */
    private void setProfilePictureAndGenderIcon(String gender, String profilePictureFileName) {
        // Set gender icon
        if ("Male".equals(gender)) {
            genderIcon.setImageResource(R.drawable.male_icon);
            genderIcon.setColorFilter(ContextCompat.getColor(this, R.color.male_color));
        } else if ("Female".equals(gender)) {
            genderIcon.setImageResource(R.drawable.female_icon);
            genderIcon.setColorFilter(ContextCompat.getColor(this, R.color.female_color));
        }
        
        // Set profile picture (placeholder implementation)
        if (profilePictureFileName != null && !profilePictureFileName.isEmpty()) {
            // In a real app, you would load the actual image file
            // For now, use default based on gender
            if ("Male".equals(gender)) {
                profilePicture.setImageResource(R.drawable.male_1);
            } else {
                profilePicture.setImageResource(R.drawable.female_1);
            }
        }
    }

    /**
     * Set availability indicators based on user selections
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
            weekdayIndicator.setTextColor(getResources().getColor(android.R.color.black));
            weekdayIndicator.setTypeface(null, Typeface.BOLD);
        } else {
            weekdayIndicator.setBackground(defaultCircleBackground);
            weekdayIndicator.setTextColor(getResources().getColor(R.color.text_secondary));
            weekdayIndicator.setTypeface(null, Typeface.NORMAL);
        }
        
        if (availability != null && availability.contains("Weekends")) {
            weekendIndicator.setBackground(selectedCircleBackground);
            weekendIndicator.setTextColor(getResources().getColor(android.R.color.black));
            weekendIndicator.setTypeface(null, Typeface.BOLD);
        } else {
            weekendIndicator.setBackground(defaultCircleBackground);
            weekendIndicator.setTextColor(getResources().getColor(R.color.text_secondary));
            weekendIndicator.setTypeface(null, Typeface.NORMAL);
        }
        
        // Set time of day indicators
        if (timeOfDay != null && timeOfDay.contains("Morning")) {
            morningIndicator.setBackground(selectedBoxBackground);
            morningIndicator.setTextColor(getResources().getColor(android.R.color.black));
            morningIndicator.setTypeface(null, Typeface.BOLD);
        } else {
            morningIndicator.setBackground(defaultBoxBackground);
            morningIndicator.setTextColor(getResources().getColor(R.color.text_secondary));
            morningIndicator.setTypeface(null, Typeface.NORMAL);
        }
        
        if (timeOfDay != null && timeOfDay.contains("Day")) {
            dayIndicator.setBackground(selectedBoxBackground);
            dayIndicator.setTextColor(getResources().getColor(android.R.color.black));
            dayIndicator.setTypeface(null, Typeface.BOLD);
        } else {
            dayIndicator.setBackground(defaultBoxBackground);
            dayIndicator.setTextColor(getResources().getColor(R.color.text_secondary));
            dayIndicator.setTypeface(null, Typeface.NORMAL);
        }
        
        if (timeOfDay != null && timeOfDay.contains("Evening")) {
            eveningIndicator.setBackground(selectedBoxBackground);
            eveningIndicator.setTextColor(getResources().getColor(android.R.color.black));
            eveningIndicator.setTypeface(null, Typeface.BOLD);
        } else {
            eveningIndicator.setBackground(defaultBoxBackground);
            eveningIndicator.setTextColor(getResources().getColor(R.color.text_secondary));
            eveningIndicator.setTypeface(null, Typeface.NORMAL);
        }
        
        if (timeOfDay != null && timeOfDay.contains("Night")) {
            nightIndicator.setBackground(selectedBoxBackground);
            nightIndicator.setTextColor(getResources().getColor(android.R.color.black));
            nightIndicator.setTypeface(null, Typeface.BOLD);
        } else {
            nightIndicator.setBackground(defaultBoxBackground);
            nightIndicator.setTextColor(getResources().getColor(R.color.text_secondary));
            nightIndicator.setTypeface(null, Typeface.NORMAL);
        }
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
        backArrow.setOnClickListener(v -> finish());

        // Chat icon click listener
        chatIcon.setOnClickListener(v -> startChat());

        // Favorite icon click listener
        favoriteIcon.setOnClickListener(v -> toggleFavorite());
    }

    /**
     * Start a chat with this user
     */
    private void startChat() {
        String chatId = Chat.generateChatId(currentUsername, targetUsername);
        
        // Create chat if it doesn't exist, then open it
        chatManager.createChat(currentUsername, targetUsername, new ChatManager.DatabaseCallback() {
            @Override
            public void onSuccess(boolean result) {
                // Open chat activity
                Intent intent = new Intent(UserDetailActivity.this, ChatActivity.class);
                intent.putExtra("chatId", chatId);
                intent.putExtra("otherUser", targetUsername);
                startActivity(intent);
            }

            @Override
            public void onError(String error) {
                Toast.makeText(UserDetailActivity.this, "Failed to start chat: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Toggle favorite status for this user
     */
    private void toggleFavorite() {
        if (isFavorite) {
            // Remove from favorites
            firebaseHelper.removeFromFavorites(currentUsername, targetUsername, new FirebaseHelper.DatabaseCallback() {
                @Override
                public void onSuccess(boolean result) {
                    isFavorite = false;
                    updateFavoriteIcon();
                    Toast.makeText(UserDetailActivity.this, "Removed from favorites", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(String error) {
                    Toast.makeText(UserDetailActivity.this, "Failed to remove from favorites: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Add to favorites
            firebaseHelper.addToFavorites(currentUsername, targetUsername, new FirebaseHelper.DatabaseCallback() {
                @Override
                public void onSuccess(boolean result) {
                    isFavorite = true;
                    updateFavoriteIcon();
                    Toast.makeText(UserDetailActivity.this, "Added to favorites", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(String error) {
                    Toast.makeText(UserDetailActivity.this, "Failed to add to favorites: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    /**
     * Check if this user is in current user's favorites
     */
    private void checkIfFavorite() {
        firebaseHelper.isFavorite(currentUsername, targetUsername, new FirebaseHelper.FavoritesCallback() {
            @Override
            public void onSuccess(boolean favorite) {
                isFavorite = favorite;
                updateFavoriteIcon();
            }

            @Override
            public void onError(String error) {
                // Default to not favorite
                isFavorite = false;
                updateFavoriteIcon();
            }
        });
    }

    /**
     * Update favorite icon based on current status
     */
    private void updateFavoriteIcon() {
        if (isFavorite) {
            favoriteIcon.setImageResource(R.drawable.ic_favorite);
            favoriteIcon.clearColorFilter(); // Clear any existing tint
            favoriteIcon.setColorFilter(ContextCompat.getColor(this, R.color.favorite_color));
        } else {
            favoriteIcon.setImageResource(R.drawable.ic_favorite_border);
            favoriteIcon.clearColorFilter(); // Clear any existing tint
            favoriteIcon.setColorFilter(ContextCompat.getColor(this, R.color.favorite_icon_unfavorited));
        }
    }
}
