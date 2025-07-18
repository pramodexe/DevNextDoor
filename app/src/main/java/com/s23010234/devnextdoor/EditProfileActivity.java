package com.s23010234.devnextdoor;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    private TextView usernameDisplay;
    private RadioGroup genderRadioGroup;
    private RadioButton maleRadioButton, femaleRadioButton;
    private Spinner profilePictureSpinner;
    private TextInputEditText bioInputText;
    private Spinner levelSpinner, citySpinner;
    private CheckBox weekdayCheckBox, weekendCheckBox;
    private CheckBox morningCheckBox, dayCheckBox, eveningCheckBox, nightCheckBox;
    private TextInputEditText techStackInputText, wantToLearnInputText, goalsInputText;
    private Button submitButton;
    private DatabaseReference databaseReference;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        databaseReference = FirebaseDatabase.getInstance().getReference("users");
        username = getIntent().getStringExtra("username");

        initializeViews();
        setupSpinners();
        setupGenderListener();
        setupSubmitButton();
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

        // Set username display
        if (username != null) {
            usernameDisplay.setText(username);
        }
    }

    private void setupSpinners() {
        // Level Spinner Setup
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

        // City Spinner Setup
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

        // Profile Picture Spinner Setup (initially empty)
        setupProfilePictureSpinner("");
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
        } else {
            ProfilePictureItem[] items = {
                    new ProfilePictureItem("Select Profile Picture", 0)
            };
            ProfilePictureAdapter adapter = new ProfilePictureAdapter(this, items);
            profilePictureSpinner.setAdapter(adapter);
        }

        // Enable/disable spinner based on gender selection
        profilePictureSpinner.setEnabled(!gender.isEmpty());
    }

    private void setupSubmitButton() {
        submitButton.setOnClickListener(v -> handleSubmit());
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

        if (TextUtils.isEmpty(wantToLearn)) {
            wantToLearnInputText.setError("Want to learn field is required");
            toast("Please specify what you want to learn");
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

        databaseReference.child(username).updateChildren(profileData)
                .addOnSuccessListener(aVoid -> {
                    toast("Profile updated successfully!");

                    // Save username to SharedPreferences
                    SharedPreferences sharedPreferences = getSharedPreferences("DevNextDoorPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("username", username);
                    editor.apply();

                    startActivity(new Intent(this, HomepageActivity.class).putExtra("username", username));
                    finish();
                })
                .addOnFailureListener(e -> toast("Failed to update profile: " + e.getMessage()));
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
            return createItemView(position, convertView, parent);
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            return createItemView(position, convertView, parent);
        }

        private View createItemView(int position, View convertView, ViewGroup parent) {
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
            }

            return convertView;
        }
    }
}
