package com.s23010234.devnextdoor;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Meet Friends Activity - Location-based friend discovery screen
 * 
 * This activity helps users find and meet other developers in their area
 * using an interactive map. Think of it like a social discovery app that
 * shows you nearby people with similar interests.
 * 
 * Features include:
 * - Interactive Google Map showing user locations
 * - Search functionality to find users by name, skills, or interests
 * - Location markers showing where other developers are located
 * - User profiles accessible by tapping on map markers
 * - Distance calculations and route suggestions
 * - Filtering by availability and common interests
 * 
 * The map integrates with Firebase to load user location data and
 * display it visually. Users can search for specific people or
 * browse the map to discover developers near them for potential
 * collaborations, study sessions, or meetups.
 */
public class MeetFriendsActivity extends AppCompatActivity implements OnMapReadyCallback {
    
    // Visual elements for search and user interaction
    private TextInputEditText searchEditText;  // Text box for searching users
    private Button searchButton;
    private TextView distanceText;
    
    // Map and Location
    private GoogleMap googleMap;
    private User currentUser;
    private User searchedUser;
    
    // Firebase
    private DatabaseReference databaseReference;
    private String currentUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_meet_friends);
        
        // Apply current theme
        ThemeManager.applyTheme(this);
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        
        // Initialize Firebase and get current user
        databaseReference = FirebaseDatabase.getInstance().getReference("users");
        SharedPreferences sharedPreferences = getSharedPreferences("DevNextDoorPrefs", MODE_PRIVATE);
        currentUsername = sharedPreferences.getString("username", "");
        
        // Initialize views
        initializeViews();
        
        // Setup map
        setupMap();
        
        // Setup click listeners
        setupClickListeners();
        
        // Setup back button
        setupBackButton();
        
        // Load current user profile to get their location
        loadCurrentUserProfile();
    }
    
    /**
     * Initialize view components
     */
    private void initializeViews() {
        searchEditText = findViewById(R.id.searchEditText);
        searchEditText.setTextColor(android.graphics.Color.BLACK);
        searchButton = findViewById(R.id.searchButton);
        distanceText = findViewById(R.id.distanceText);
    }
    
    /**
     * Setup map fragment
     */
    private void setupMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }
    
    /**
     * Setup click listeners for buttons
     */
    private void setupClickListeners() {
        searchButton.setOnClickListener(v -> performUserSearch());
    }
    
    /**
     * Load current user profile from Firebase to get their city
     */
    private void loadCurrentUserProfile() {
        if (currentUsername.isEmpty()) {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
            return;
        }
        
        databaseReference.child(currentUsername).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Extract current user data
                    String username = dataSnapshot.getKey();
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
                    
                    Boolean profileCompleted = dataSnapshot.child("profileCompleted").getValue(Boolean.class);
                    if (profileCompleted == null || !profileCompleted) {
                        Toast.makeText(MeetFriendsActivity.this, 
                                     "Please complete your profile first", 
                                     Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    // Create current User object
                    currentUser = new User(username, gender, bio, wantToLearn, profilePicture,
                                         level, city, techStack, goals, availability, timeOfDay,
                                         true, System.currentTimeMillis());
                    
                    // Set default camera position to Sri Lanka
                    if (googleMap != null) {
                        LatLng sriLanka = new LatLng(7.8731, 80.7718);
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sriLanka, 8));
                    }
                    
                } else {
                    Toast.makeText(MeetFriendsActivity.this, 
                                 "User profile not found", 
                                 Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MeetFriendsActivity.this, 
                             "Failed to load profile: " + databaseError.getMessage(), 
                             Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    /**
     * Perform user search by username
     */
    private void performUserSearch() {
        String searchUsername = searchEditText.getText().toString().trim();
        
        if (TextUtils.isEmpty(searchUsername)) {
            searchEditText.setError("Please enter a username");
            return;
        }
        
        if (searchUsername.equals(currentUsername)) {
            Toast.makeText(this, "You cannot search for yourself!", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Show loading
        searchButton.setText("Searching...");
        searchButton.setEnabled(false);
        
        // Search for user in Firebase
        databaseReference.child(searchUsername).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                searchButton.setText("Search User");
                searchButton.setEnabled(true);
                
                if (dataSnapshot.exists()) {
                    // User found, extract data
                    String username = dataSnapshot.getKey();
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
                    Double latitude = dataSnapshot.child("latitude").getValue(Double.class);
                    Double longitude = dataSnapshot.child("longitude").getValue(Double.class);
                    
                    Boolean profileCompleted = dataSnapshot.child("profileCompleted").getValue(Boolean.class);
                    if (profileCompleted == null || !profileCompleted) {
                        Toast.makeText(MeetFriendsActivity.this, 
                                     "User found but profile is not completed", 
                                     Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    // Create User object
                    searchedUser = new User(username, gender, bio, wantToLearn, profilePicture,
                                          level, city, techStack, goals, availability, timeOfDay,
                                          true, System.currentTimeMillis(),
                                          latitude != null ? latitude : 0.0,
                                          longitude != null ? longitude : 0.0);
                    
                    // Directly show directions on map and update distance
                    showDirectionsOnMap();
                    
                } else {
                    Toast.makeText(MeetFriendsActivity.this, 
                                 "User not found with username: " + searchUsername, 
                                 Toast.LENGTH_SHORT).show();
                    clearMapAndDistance();
                }
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                searchButton.setText("Search User");
                searchButton.setEnabled(true);
                Toast.makeText(MeetFriendsActivity.this, 
                             "Search failed: " + databaseError.getMessage(), 
                             Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    /**
     * Clear map and distance when search fails
     */
    private void clearMapAndDistance() {
        if (googleMap != null) {
            googleMap.clear();
            // Reset to default Sri Lanka view
            LatLng sriLanka = new LatLng(7.8731, 80.7718);
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sriLanka, 8));
        }
        distanceText.setText("");
    }
    
    /**
     * Show directions on map between current user's city and searched user's city
     */
    private void showDirectionsOnMap() {
        if (currentUser == null) {
            Toast.makeText(this, "Current user profile not loaded.", 
                         Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (searchedUser == null) {
            Toast.makeText(this, "Please search for a user first.", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (currentUser.getCity() == null || searchedUser.getCity() == null) {
            Toast.makeText(this, "City information not available for one or both users.", 
                         Toast.LENGTH_LONG).show();
            distanceText.setText("City information not available");
            return;
        }
        
        if (googleMap != null) {
            // Clear existing markers and polylines
            googleMap.clear();
            
            // Get coordinates for both cities (Sri Lankan cities)
            LatLng currentCityLatLng = getCityCoordinates(currentUser.getCity());
            LatLng searchedCityLatLng = getCityCoordinates(searchedUser.getCity());
            
            if (currentCityLatLng == null || searchedCityLatLng == null) {
                Toast.makeText(this, "Could not find coordinates for one or both cities.", 
                             Toast.LENGTH_SHORT).show();
                distanceText.setText("Could not calculate distance");
                return;
            }
            
            // Add current user's city marker
            googleMap.addMarker(new MarkerOptions()
                    .position(currentCityLatLng)
                    .title("You are in " + currentUser.getCity())
                    .snippet("Your location")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
            
            // Add searched user's city marker
            googleMap.addMarker(new MarkerOptions()
                    .position(searchedCityLatLng)
                    .title(searchedUser.getUsername() + " in " + searchedUser.getCity())
                    .snippet(searchedUser.getCity())
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
            
            // Only draw a line between cities if they are different
            if (!currentUser.getCity().equalsIgnoreCase(searchedUser.getCity())) {
                PolylineOptions polylineOptions = new PolylineOptions()
                        .add(currentCityLatLng)
                        .add(searchedCityLatLng)
                        .width(5)
                        .color(Color.parseColor("#F4F0D5"))
                        .geodesic(true);
                
                googleMap.addPolyline(polylineOptions);
            }
            
            // Calculate bounds to show both markers
            com.google.android.gms.maps.model.LatLngBounds.Builder boundsBuilder = 
                    new com.google.android.gms.maps.model.LatLngBounds.Builder();
            boundsBuilder.include(currentCityLatLng);
            boundsBuilder.include(searchedCityLatLng);
            
            com.google.android.gms.maps.model.LatLngBounds bounds = boundsBuilder.build();
            googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
            
            // Calculate and display approximate distance between cities
            float[] results = new float[1];
            Location.distanceBetween(currentCityLatLng.latitude, currentCityLatLng.longitude,
                                   searchedCityLatLng.latitude, searchedCityLatLng.longitude, results);
            
            float distanceInKm = results[0] / 1000;
            
            // Update distance text field
            String distanceMessage;
            if (currentUser.getCity().equalsIgnoreCase(searchedUser.getCity())) {
                distanceMessage = "You're in the same city!";
            } else {
                distanceMessage = String.format("%.1f km between %s and %s", 
                                              distanceInKm, currentUser.getCity(), searchedUser.getCity());
            }
            distanceText.setText(distanceMessage);
            
            // Show success message
            Toast.makeText(this, String.format("Found %s in %s", searchedUser.getUsername(), searchedUser.getCity()), 
                         Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Get coordinates for Sri Lankan cities
     */
    private LatLng getCityCoordinates(String city) {
        switch (city.toLowerCase()) {
            case "colombo": return new LatLng(6.9271, 79.8612);
            case "kandy": return new LatLng(7.2906, 80.6337);
            case "galle": return new LatLng(6.0329, 80.2168);
            case "jaffna": return new LatLng(9.6615, 80.0255);
            case "negombo": return new LatLng(7.2083, 79.8358);
            case "kurunegala": return new LatLng(7.4818, 80.3609);
            case "ratnapura": return new LatLng(6.6828, 80.4126);
            case "matara": return new LatLng(5.9549, 80.5550);
            case "anuradhapura": return new LatLng(8.3114, 80.4037);
            case "polonnaruwa": return new LatLng(7.9403, 81.0188);
            case "badulla": return new LatLng(6.9934, 81.0550);
            case "batticaloa": return new LatLng(7.7102, 81.7088);
            case "ampara": return new LatLng(7.2971, 81.6747);
            case "gampaha": return new LatLng(7.0873, 79.9990);
            case "hambantota": return new LatLng(6.1241, 81.1185);
            case "kalutara": return new LatLng(6.5854, 79.9607);
            case "kegalle": return new LatLng(7.2513, 80.3464);
            case "kilinochchi": return new LatLng(9.3965, 80.4135);
            case "mannar": return new LatLng(8.9810, 79.9043);
            case "matale": return new LatLng(7.4675, 80.6234);
            case "monaragala": return new LatLng(6.8728, 81.3510);
            case "mullaitivu": return new LatLng(9.2654, 80.8142);
            case "nuwara eliya": return new LatLng(6.9497, 80.7891);
            case "puttalam": return new LatLng(8.0362, 79.8283);
            case "trincomalee": return new LatLng(8.5874, 81.2152);
            case "vavuniya": return new LatLng(8.7514, 80.4971);
            default: return new LatLng(7.8731, 80.7718); // Default to center of Sri Lanka
        }
    }
    
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.googleMap = googleMap;
        
        // Set default camera position (Sri Lanka)
        LatLng sriLanka = new LatLng(7.8731, 80.7718);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sriLanka, 8));
        
        // Enable basic map controls
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setCompassEnabled(true);
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
