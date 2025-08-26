package com.s23010234.devnextdoor;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is the Edit Profile screen where users can create or modify their profile information.
 * It has forms for personal details like bio, skills, availability, and goals.
 * Users can also choose their profile picture and set their experience level.
 * This screen works for both creating a new profile and editing an existing one.
 * Think of it as a detailed registration form that builds your digital identity.
 */
public class EditProfileActivity extends AppCompatActivity {

    // Display elements
    private TextView usernameDisplay;           // Shows the user's username (read-only)
    private RadioGroup genderRadioGroup;        // Radio buttons for selecting Male/Female
    private RadioButton maleRadioButton;        // Option to select Male gender
    private RadioButton femaleRadioButton;      // Option to select Female gender
    private Spinner profilePictureSpinner;      // Dropdown to choose profile picture
    private TextInputEditText bioInputText;     // Text field for writing bio
    private Spinner levelSpinner;               // Dropdown to choose experience level
    private Spinner citySpinner;                // Dropdown to choose city
    
    // Availability checkboxes for days
    private CheckBox weekdayCheckBox;           // Available on weekdays
    private CheckBox weekendCheckBox;           // Available on weekends
    
    // Availability checkboxes for times of day
    private CheckBox morningCheckBox;           // Available in morning
    private CheckBox dayCheckBox;               // Available during day
    private CheckBox eveningCheckBox;           // Available in evening
    private CheckBox nightCheckBox;             // Available at night
    
    // Text input fields for detailed information
    private TextInputEditText techStackInputText;    // What programming languages they know
    private TextInputEditText wantToLearnInputText;  // What they want to learn
    private TextInputEditText goalsInputText;        // Their learning goals
    
    // Action buttons
    private Button submitButton;                // Button to save the profile
    private ImageView backArrow;               // Button to go back to previous screen
    private TextView titleText;                // Title at top of screen
    
    // Database and state variables
    private DatabaseReference databaseReference;  // Connection to Firebase database
    private String username;                      // Username of the profile being edited
    private boolean isEditing = false;            // Whether we're editing existing profile or creating new one

    /**
     * This method runs when the Edit Profile screen is created and shown to the user.
     * It sets up all the form fields, dropdowns, and loads existing data if editing.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Call the parent class method to properly create the activity
        super.onCreate(savedInstanceState);
        
        // Make the app use the full screen (edge to edge display)
        EdgeToEdge.enable(this);
        
        // Load and display the edit profile screen layout from the XML file
        setContentView(R.layout.activity_edit_profile);
        
        // Apply the user's preferred theme (dark or light mode)
        ThemeManager.applyTheme(this);
        
        // Handle system bars (like status bar and navigation bar) properly
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Set up connection to Firebase database where profiles are stored
        databaseReference = FirebaseDatabase.getInstance().getReference("users");
        
        // Get information passed from the previous screen
        username = getIntent().getStringExtra("username");
        isEditing = getIntent().getBooleanExtra("isEditing", false);

        // Set up all the form elements and their functionality
        initializeViews();        // Find and connect to all UI elements
        setupSpinners();          // Set up dropdown menus with options
        setupGenderListener();    // Set up gender selection functionality
        setupSubmitButton();      // Set up save button functionality
        
        // Load existing data if we're editing
        if (isEditing && username != null) {
            loadExistingUserData();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reapply theme when returning to this activity (e.g., from Settings)
        ThemeManager.applyTheme(this);
    }

    private void initializeViews() {
        usernameDisplay = findViewById(R.id.usernameDisplay);
        genderRadioGroup = findViewById(R.id.genderRadioGroup);
        maleRadioButton = findViewById(R.id.maleRadioButton);
        femaleRadioButton = findViewById(R.id.femaleRadioButton);
        profilePictureSpinner = findViewById(R.id.profilePictureSpinner);
        bioInputText = findViewById(R.id.bioInputText);
        levelSpinner = findViewById(R.id.levelSpinner);
        citySpinner = findViewById(R.id.citySpinner);
        weekdayCheckBox = findViewById(R.id.weekdayCheckBox);
        weekendCheckBox = findViewById(R.id.weekendCheckBox);
        morningCheckBox = findViewById(R.id.morningCheckBox);
        dayCheckBox = findViewById(R.id.dayCheckBox);
        eveningCheckBox = findViewById(R.id.eveningCheckBox);
        nightCheckBox = findViewById(R.id.nightCheckBox);
        techStackInputText = findViewById(R.id.techStackInputText);
        wantToLearnInputText = findViewById(R.id.wantToLearnInputText);
        goalsInputText = findViewById(R.id.goalsInputText);
        submitButton = findViewById(R.id.submitButton);
        backArrow = findViewById(R.id.backArrow);
        titleText = findViewById(R.id.titleText);

        // Set username display
        if (username != null) {
            usernameDisplay.setText(username);
        }
        
        // Configure UI based on whether we're editing or creating
        if (isEditing) {
            submitButton.setText("Update Profile");
            titleText.setText("Edit Profile");
            backArrow.setVisibility(View.VISIBLE);
            backArrow.setOnClickListener(v -> finish()); // Go back to previous activity
        } else {
            submitButton.setText("Submit");
            titleText.setText("Complete Your Profile");
            backArrow.setVisibility(View.GONE);
        }
    }

    private void setupSpinners() {
        // Level Spinner Setup with standard Android layouts
        List<String> levelOptions = Arrays.asList(
                "Select Level",
                "Grade 6-11", "After O/L", "After A/L",
                "1st Year Undergraduate", "2nd Year Undergraduate",
                "3rd Year Undergraduate", "Final Year Undergraduate",
                "Graduate", "Master's Student",
                "Intern (Undergraduate / Graduate)",
                "Professional / Employed", "Other"
        );
        ArrayAdapter<String> levelAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, levelOptions);
        levelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        levelSpinner.setAdapter(levelAdapter);
        
        // Ensure spinner is properly configured for touch events
        levelSpinner.setFocusable(true);
        levelSpinner.setClickable(true);

        // City Spinner Setup with standard Android layouts
        List<String> cityOptions = Arrays.asList(
                "Select City",
                "Ampara", "Anuradhapura", "Badulla", "Batticaloa", "Colombo", "Galle",
                "Gampaha", "Hambantota", "Jaffna", "Kalutara", "Kandy", "Kegalle",
                "Kilinochchi", "Kurunegala", "Mannar", "Matale", "Matara", "Monaragala",
                "Mullaitivu", "Nuwara Eliya", "Polonnaruwa", "Puttalam", "Ratnapura",
                "Trincomalee", "Vavuniya"
        );
        ArrayAdapter<String> cityAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, cityOptions);
        cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        citySpinner.setAdapter(cityAdapter);
        
        // Ensure spinner is properly configured for touch events
        citySpinner.setFocusable(true);
        citySpinner.setClickable(true);

        // Profile Picture Spinner Setup (initially empty)
        setupProfilePictureSpinner("");
        
        // Setup spinner listeners to ensure selections work properly
        setupSpinnerListeners();
    }

    private void setupGenderListener() {
        genderRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.maleRadioButton) {
                setupProfilePictureSpinner("Male");
            } else if (checkedId == R.id.femaleRadioButton) {
                setupProfilePictureSpinner("Female");
            }
        });
    }

    private void setupProfilePictureSpinner(String gender) {
        if (gender.equals("Male")) {
            ProfilePictureItem[] items = {
                    new ProfilePictureItem("Select Profile Picture", 0),
                    new ProfilePictureItem("Male Avatar 1", getResources().getIdentifier("male_1", "drawable", getPackageName())),
                    new ProfilePictureItem("Male Avatar 2", getResources().getIdentifier("male_2", "drawable", getPackageName())),
                    new ProfilePictureItem("Male Avatar 3", getResources().getIdentifier("male_3", "drawable", getPackageName())),
                    new ProfilePictureItem("Male Avatar 4", getResources().getIdentifier("male_4", "drawable", getPackageName())),
                    new ProfilePictureItem("Male Avatar 5", getResources().getIdentifier("male_5", "drawable", getPackageName())),
                    new ProfilePictureItem("Male Avatar 6", getResources().getIdentifier("male_6", "drawable", getPackageName()))
            };
            ProfilePictureAdapter adapter = new ProfilePictureAdapter(this, items);
            profilePictureSpinner.setAdapter(adapter);
            
            // Set popup background programmatically for better compatibility - same as other spinners
            profilePictureSpinner.setPopupBackgroundResource(R.color.edit_profile_dropdown_background);
            
            // Ensure spinner is properly configured for touch events
            profilePictureSpinner.setFocusable(true);
            profilePictureSpinner.setClickable(true);
            
            // Set up listener for this spinner
            setupProfilePictureSpinnerListener();
        } else if (gender.equals("Female")) {
            ProfilePictureItem[] items = {
                    new ProfilePictureItem("Select Profile Picture", 0),
                    new ProfilePictureItem("Female Avatar 1", getResources().getIdentifier("female_1", "drawable", getPackageName())),
                    new ProfilePictureItem("Female Avatar 2", getResources().getIdentifier("female_2", "drawable", getPackageName())),
                    new ProfilePictureItem("Female Avatar 3", getResources().getIdentifier("female_3", "drawable", getPackageName())),
                    new ProfilePictureItem("Female Avatar 4", getResources().getIdentifier("female_4", "drawable", getPackageName())),
                    new ProfilePictureItem("Female Avatar 5", getResources().getIdentifier("female_5", "drawable", getPackageName())),
                    new ProfilePictureItem("Female Avatar 6", getResources().getIdentifier("female_6", "drawable", getPackageName()))
            };
            ProfilePictureAdapter adapter = new ProfilePictureAdapter(this, items);
            profilePictureSpinner.setAdapter(adapter);
            
            // Set popup background programmatically for better compatibility - same as other spinners
            profilePictureSpinner.setPopupBackgroundResource(R.color.edit_profile_dropdown_background);
            
            // Ensure spinner is properly configured for touch events
            profilePictureSpinner.setFocusable(true);
            profilePictureSpinner.setClickable(true);
            
            // Set up listener for this spinner
            setupProfilePictureSpinnerListener();
        } else {
            ProfilePictureItem[] items = {
                    new ProfilePictureItem("Select Profile Picture", 0)
            };
            ProfilePictureAdapter adapter = new ProfilePictureAdapter(this, items);
            profilePictureSpinner.setAdapter(adapter);
            
            // Set popup background programmatically for better compatibility - same as other spinners
            profilePictureSpinner.setPopupBackgroundResource(R.color.edit_profile_dropdown_background);
            
            // Ensure spinner is properly configured for touch events
            profilePictureSpinner.setFocusable(true);
            profilePictureSpinner.setClickable(true);
            
            // Set up listener for this spinner
            setupProfilePictureSpinnerListener();
        }

        // Enable/disable spinner based on gender selection
        profilePictureSpinner.setEnabled(!gender.isEmpty());
    }

    private void setupProfilePictureSpinnerListener() {
        // Profile Picture Spinner Listener - separate method since it gets recreated
        profilePictureSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Handle profile picture selection if needed
                ProfilePictureItem selectedItem = (ProfilePictureItem) parent.getItemAtPosition(position);
                // Selection handling logic can be added here if needed
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Handle case where nothing is selected
            }
        });
    }

    private void setupSpinnerListeners() {
        // Level Spinner Listener
        levelSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Handle level selection if needed
                String selectedLevel = parent.getItemAtPosition(position).toString();
                // Selection handling logic can be added here if needed
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Handle case where nothing is selected
            }
        });

        // City Spinner Listener
        citySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Handle city selection if needed
                String selectedCity = parent.getItemAtPosition(position).toString();
                // Selection handling logic can be added here if needed
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Handle case where nothing is selected
            }
        });
    }

    private void setupSubmitButton() {
        submitButton.setOnClickListener(v -> handleSubmit());
    }

    private void loadExistingUserData() {
        databaseReference.child(username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    try {
                        // Load gender and set radio button
                        String gender = dataSnapshot.child("gender").getValue(String.class);
                        if ("Male".equals(gender)) {
                            maleRadioButton.setChecked(true);
                            setupProfilePictureSpinner("Male");
                        } else if ("Female".equals(gender)) {
                            femaleRadioButton.setChecked(true);
                            setupProfilePictureSpinner("Female");
                        }

                        // Load and set profile picture selection
                        String profilePicture = dataSnapshot.child("profilePicture").getValue(String.class);
                        if (profilePicture != null) {
                            setProfilePictureSelection(gender, profilePicture);
                        }

                        // Load bio
                        String bio = dataSnapshot.child("bio").getValue(String.class);
                        if (bio != null) {
                            bioInputText.setText(bio);
                        }

                        // Load level
                        String level = dataSnapshot.child("level").getValue(String.class);
                        if (level != null) {
                            setSpinnerSelection(levelSpinner, level);
                        }

                        // Load city
                        String city = dataSnapshot.child("city").getValue(String.class);
                        if (city != null) {
                            setSpinnerSelection(citySpinner, city);
                        }

                        // Load availability
                        String availability = dataSnapshot.child("availability").getValue(String.class);
                        if (availability != null) {
                            if (availability.contains("Weekdays")) weekdayCheckBox.setChecked(true);
                            if (availability.contains("Weekends")) weekendCheckBox.setChecked(true);
                        }

                        // Load time of day preferences
                        String timeOfDay = dataSnapshot.child("timeOfDay").getValue(String.class);
                        if (timeOfDay != null) {
                            if (timeOfDay.contains("Morning")) morningCheckBox.setChecked(true);
                            if (timeOfDay.contains("Day")) dayCheckBox.setChecked(true);
                            if (timeOfDay.contains("Evening")) eveningCheckBox.setChecked(true);
                            if (timeOfDay.contains("Night")) nightCheckBox.setChecked(true);
                        }

                        // Load tech stack
                        String techStack = dataSnapshot.child("techStack").getValue(String.class);
                        if (techStack != null) {
                            techStackInputText.setText(techStack);
                        }

                        // Load want to learn
                        String wantToLearn = dataSnapshot.child("wantToLearn").getValue(String.class);
                        if (wantToLearn != null) {
                            wantToLearnInputText.setText(wantToLearn);
                        }

                        // Load goals
                        String goals = dataSnapshot.child("goals").getValue(String.class);
                        if (goals != null) {
                            goalsInputText.setText(goals);
                        }

                    } catch (Exception e) {
                        toast("Error loading profile data: " + e.getMessage());
                    }
                } else {
                    toast("Profile data not found");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                toast("Failed to load profile data: " + databaseError.getMessage());
            }
        });
    }

    private void setSpinnerSelection(Spinner spinner, String value) {
        ArrayAdapter adapter = (ArrayAdapter) spinner.getAdapter();
        if (adapter != null) {
            int position = adapter.getPosition(value);
            if (position >= 0) {
                spinner.setSelection(position);
            }
        }
    }

    private void setProfilePictureSelection(String gender, String profilePictureFileName) {
        // Convert filename back to display name and set selection
        String displayName = getProfilePictureDisplayName(gender, profilePictureFileName);
        if (displayName != null) {
            ProfilePictureAdapter adapter = (ProfilePictureAdapter) profilePictureSpinner.getAdapter();
            if (adapter != null) {
                for (int i = 0; i < adapter.getCount(); i++) {
                    ProfilePictureItem item = adapter.getItem(i);
                    if (item != null && displayName.equals(item.getName())) {
                        profilePictureSpinner.setSelection(i);
                        break;
                    }
                }
            }
        }
    }

    private String getProfilePictureDisplayName(String gender, String fileName) {
        if (fileName == null) return null;
        
        if ("Male".equals(gender)) {
            switch (fileName) {
                case "male_1.png": return "Male Avatar 1";
                case "male_2.png": return "Male Avatar 2";
                case "male_3.png": return "Male Avatar 3";
                case "male_4.png": return "Male Avatar 4";
                case "male_5.png": return "Male Avatar 5";
                case "male_6.png": return "Male Avatar 6";
            }
        } else if ("Female".equals(gender)) {
            switch (fileName) {
                case "female_1.png": return "Female Avatar 1";
                case "female_2.png": return "Female Avatar 2";
                case "female_3.png": return "Female Avatar 3";
                case "female_4.png": return "Female Avatar 4";
                case "female_5.png": return "Female Avatar 5";
                case "female_6.png": return "Female Avatar 6";
            }
        }
        return null;
    }

    private void handleSubmit() {
        // Clear any existing errors first
        clearErrors();

        // Get gender selection
        String gender = "";
        int genderId = genderRadioGroup.getCheckedRadioButtonId();
        if (genderId == R.id.maleRadioButton) gender = "Male";
        else if (genderId == R.id.femaleRadioButton) gender = "Female";

        // Get profile picture selection
        ProfilePictureItem selectedItem = (ProfilePictureItem) profilePictureSpinner.getSelectedItem();
        String profilePicture = selectedItem != null ? selectedItem.getName() : "";

        // Get availability selections (multiple checkboxes)
        List<String> availabilityList = new ArrayList<>();
        if (weekdayCheckBox.isChecked()) availabilityList.add("Weekdays");
        if (weekendCheckBox.isChecked()) availabilityList.add("Weekends");
        String availability = String.join(", ", availabilityList);

        // Get time of day selections (multiple checkboxes)
        List<String> timeOfDayList = new ArrayList<>();
        if (morningCheckBox.isChecked()) timeOfDayList.add("Morning");
        if (dayCheckBox.isChecked()) timeOfDayList.add("Day");
        if (eveningCheckBox.isChecked()) timeOfDayList.add("Evening");
        if (nightCheckBox.isChecked()) timeOfDayList.add("Night");
        String timeOfDay = String.join(", ", timeOfDayList);

        // Get text inputs
        String bio = bioInputText.getText().toString().trim();
        String level = levelSpinner.getSelectedItem().toString();
        String city = citySpinner.getSelectedItem().toString();
        String techStack = techStackInputText.getText().toString().trim();
        String wantToLearn = wantToLearnInputText.getText().toString().trim();
        String goals = goalsInputText.getText().toString().trim();

        // Validation with both error indicators and toast messages
        if (TextUtils.isEmpty(gender)) {
            toast("Please select a gender");
            return;
        }

        if ("Select Profile Picture".equals(profilePicture)) {
            toast("Please select a profile picture");
            return;
        }

        if (TextUtils.isEmpty(bio)) {
            bioInputText.setError("Bio is required");
            toast("Bio field cannot be empty");
            bioInputText.requestFocus();
            return;
        }

        if ("Select Level".equals(level)) {
            toast("Please select your education level");
            return;
        }

        if ("Select City".equals(city)) {
            toast("Please select your city");
            return;
        }

        if (availabilityList.isEmpty()) {
            toast("Please select at least one availability option");
            return;
        }

        if (timeOfDayList.isEmpty()) {
            toast("Please select at least one preferred time of day");
            return;
        }

        if (TextUtils.isEmpty(techStack)) {
            techStackInputText.setError("Tech stack is required");
            toast("Please enter your tech stack");
            techStackInputText.requestFocus();
            return;
        }

        // Validate tech stack format (comma separation for multiple items)
        if (!isValidCommaSeparatedFormat(techStack)) {
            techStackInputText.setError("Please separate multiple technologies with commas (e.g., Java, Python, React)");
            toast("Use commas to separate multiple technologies in tech stack");
            techStackInputText.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(wantToLearn)) {
            wantToLearnInputText.setError("Want to learn field is required");
            toast("Please specify what you want to learn");
            wantToLearnInputText.requestFocus();
            return;
        }

        // Validate want to learn format (comma separation for multiple items)
        if (!isValidCommaSeparatedFormat(wantToLearn)) {
            wantToLearnInputText.setError("Please separate multiple technologies with commas (e.g., Spring Boot, React Native)");
            toast("Use commas to separate multiple technologies in want to learn");
            wantToLearnInputText.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(goals)) {
            goalsInputText.setError("Goals field is required");
            toast("Please enter your goals");
            goalsInputText.requestFocus();
            return;
        }

        // Save profile data
        saveProfileData(gender, profilePicture, bio, level, city, availability, timeOfDay, techStack, wantToLearn, goals);
    }

    private void clearErrors() {
        bioInputText.setError(null);
        techStackInputText.setError(null);
        wantToLearnInputText.setError(null);
        goalsInputText.setError(null);
    }

    private void saveProfileData(String gender, String profilePicture, String bio, String level, String city,
                                 String availability, String timeOfDay, String techStack,
                                 String wantToLearn, String goals) {
    Map<String, Object> profileData = new HashMap<>();
        profileData.put("gender", gender);
        profileData.put("profilePicture", getProfilePictureFileName(gender, profilePicture));
        profileData.put("bio", bio);
        profileData.put("level", level);
        profileData.put("city", city);
        profileData.put("availability", availability);
        profileData.put("timeOfDay", timeOfDay);
        profileData.put("techStack", techStack);
        profileData.put("wantToLearn", wantToLearn);
        profileData.put("goals", goals);
        profileData.put("profileCompleted", true);

        // Set timestamp only if it doesn't already exist (first time profile creation)
        databaseReference.child(username).child("timestamp").addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {
                boolean isFirstTimeProfileCreation = !dataSnapshot.exists() && !isEditing;
                if (isFirstTimeProfileCreation) {
                    profileData.put("timestamp", System.currentTimeMillis());
                }

                // Proceed with update after deciding timestamp
                databaseReference.child(username).updateChildren(profileData)
                        .addOnSuccessListener(aVoid -> {
                            toast("Profile updated successfully!");

                            // Add profile completion notification for first-time users
                            if (isFirstTimeProfileCreation) {
                                addProfileCompletionNotification(username);
                            }

                            // Save username to SharedPreferences
                            SharedPreferences sharedPreferences = getSharedPreferences("DevNextDoorPrefs", MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("username", username);
                            editor.apply();

                            // Navigation behavior depends on whether we're editing or creating
                            if (isEditing) {
                                // When editing, go back to dashboard
                                Intent intent = new Intent(EditProfileActivity.this, DashboardActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                // When creating new profile, go to homepage and clear back stack
                                Intent intent = new Intent(EditProfileActivity.this, HomepageActivity.class);
                                intent.putExtra("username", username);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            }
                        })
                        .addOnFailureListener(e -> toast("Failed to update profile: " + e.getMessage()));
            }

            @Override
            public void onCancelled(com.google.firebase.database.DatabaseError databaseError) {
                toast("Failed to check profile timestamp: " + databaseError.getMessage());
            }
        });
    }

    private String getProfilePictureFileName(String gender, String profilePicture) {
        // Convert display name to actual file name
        if (gender.equals("Male")) {
            switch (profilePicture) {
                case "Male Avatar 1": return "male_1.png";
                case "Male Avatar 2": return "male_2.png";
                case "Male Avatar 3": return "male_3.png";
                case "Male Avatar 4": return "male_4.png";
                case "Male Avatar 5": return "male_5.png";
                case "Male Avatar 6": return "male_6.png";
            }
        } else if (gender.equals("Female")) {
            switch (profilePicture) {
                case "Female Avatar 1": return "female_1.png";
                case "Female Avatar 2": return "female_2.png";
                case "Female Avatar 3": return "female_3.png";
                case "Female Avatar 4": return "female_4.png";
                case "Female Avatar 5": return "female_5.png";
                case "Female Avatar 6": return "female_6.png";
            }
        }
        return "";
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Validates if the input text follows proper comma-separated format
     * Checks for multiple words without commas and suggests comma separation
     * @param text Input text to validate
     * @return true if format is valid, false if needs comma separation
     */
    private boolean isValidCommaSeparatedFormat(String text) {
        if (text == null || text.trim().isEmpty()) {
            return true; // Empty text is handled by other validation
        }
        
        String trimmedText = text.trim();
        
        // If text contains commas, it's likely properly formatted
        if (trimmedText.contains(",")) {
            return true;
        }
        
        // Split by whitespace to count words
        String[] words = trimmedText.split("\\s+");
        
        // If more than 2 words without commas, suggest comma separation
        // Allow single words or two-word phrases (like "React Native")
        if (words.length > 2) {
            // Check if it might be a valid multi-word technology name
            // Allow common patterns like "Spring Boot", "React Native", "Node.js", etc.
            if (words.length <= 3 && containsCommonTechPatterns(trimmedText)) {
                return true;
            }
            return false;
        }
        
        return true;
    }
    
    /**
     * Checks if the text contains common technology naming patterns
     * that might be valid as single entries
     */
    private boolean containsCommonTechPatterns(String text) {
        String lowerText = text.toLowerCase();
        
        // Common technology patterns that are typically single entries
        String[] commonPatterns = {
            "spring boot", "react native", "node.js", "vue.js", "next.js",
            "react js", "angular js", "express.js", "nest.js", "socket.io",
            "material ui", "tailwind css", "styled components", "sass css",
            "visual studio", "android studio", "intellij idea", "vs code",
            "machine learning", "artificial intelligence", "data science",
            "web development", "mobile development", "full stack",
            "front end", "back end", "database management"
        };
        
        for (String pattern : commonPatterns) {
            if (lowerText.contains(pattern)) {
                return true;
            }
        }
        
        return false;
    }

    // Custom data class for profile picture items
    private static class ProfilePictureItem {
        private String name;
        private int imageResourceId;

        public ProfilePictureItem(String name, int imageResourceId) {
            this.name = name;
            this.imageResourceId = imageResourceId;
        }

        public String getName() {
            return name;
        }

        public int getImageResourceId() {
            return imageResourceId;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    // Custom adapter for profile picture spinner with image preview
    private static class ProfilePictureAdapter extends ArrayAdapter<ProfilePictureItem> {
        public ProfilePictureAdapter(EditProfileActivity context, ProfilePictureItem[] items) {
            super(context, 0, items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return createItemView(position, convertView, parent, false);
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            return createItemView(position, convertView, parent, true);
        }

        private View createItemView(int position, View convertView, ViewGroup parent, boolean isDropDown) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.spinner_profile_picture_item, parent, false);
            }

            ProfilePictureItem item = getItem(position);
            if (item != null) {
                ImageView imageView = convertView.findViewById(R.id.profilePictureImageView);
                TextView textView = convertView.findViewById(R.id.profilePictureTextView);

                textView.setText(item.getName());

                if (item.getImageResourceId() != 0) {
                    imageView.setImageResource(item.getImageResourceId());
                    imageView.setVisibility(View.VISIBLE);
                } else {
                    imageView.setVisibility(View.GONE);
                }
                
                // Configure touch handling - let parent handle all touch events
                convertView.setClickable(false);
                convertView.setFocusable(false);
                convertView.setFocusableInTouchMode(false);
                
                // Ensure child views don't intercept touch events
                imageView.setClickable(false);
                imageView.setFocusable(false);
                textView.setClickable(false);
                textView.setFocusable(false);
            }

            return convertView;
        }
    }

    private void addProfileCompletionNotification(String username) {
        NotificationManager notificationManager = new NotificationManager();
        Notification profileNotification = new Notification(
                "Profile Completed!",
                "Great! Your profile has been completed successfully. Other developers can now discover and connect with you.",
                Notification.Types.PROFILE_CREATED
        );
        
        notificationManager.addNotification(username, profileNotification, new NotificationManager.NotificationCallback() {
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
