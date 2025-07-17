package com.s23010234.devnextdoor;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    private TextView usernameDisplay;
    private RadioGroup genderRadioGroup;
    private RadioButton maleRadioButton, femaleRadioButton;
    private TextInputEditText bioInputText;
    private Spinner levelSpinner, citySpinner;
    private RadioGroup availableRadioGroup;
    private RadioButton weekdayRadioButton, weekendRadioButton;
    private TextInputEditText timesInputText, techStackInputText, wantToLearnInputText, goalsInputText;
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
        setupSubmitButton();
    }

    private void initializeViews() {
        usernameDisplay = findViewById(R.id.usernameDisplay);
        genderRadioGroup = findViewById(R.id.genderRadioGroup);
        maleRadioButton = findViewById(R.id.maleRadioButton);
        femaleRadioButton = findViewById(R.id.femaleRadioButton);
        bioInputText = findViewById(R.id.bioInputText);
        levelSpinner = findViewById(R.id.levelSpinner);
        citySpinner = findViewById(R.id.citySpinner);
        availableRadioGroup = findViewById(R.id.availableRadioGroup);
        weekdayRadioButton = findViewById(R.id.radioButton);
        weekendRadioButton = findViewById(R.id.radioButton2);
        timesInputText = findViewById(R.id.timesInputText);
        techStackInputText = findViewById(R.id.techStackInputText);
        wantToLearnInputText = findViewById(R.id.wantToLearnInputText);
        goalsInputText = findViewById(R.id.goalsInputText);
        submitButton = findViewById(R.id.submitButton);

        usernameDisplay.setText(username);
    }

    private void setupSpinners() {
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
    }

    private void setupSubmitButton() {
        submitButton.setOnClickListener(v -> handleSubmit());
    }

    private void handleSubmit() {
        String gender = "";
        int genderId = genderRadioGroup.getCheckedRadioButtonId();
        if (genderId == R.id.maleRadioButton) gender = "Male";
        else if (genderId == R.id.femaleRadioButton) gender = "Female";

        String availability = "";
        int availabilityId = availableRadioGroup.getCheckedRadioButtonId();
        if (availabilityId == R.id.radioButton) availability = "Weekdays";
        else if (availabilityId == R.id.radioButton2) availability = "Weekends";

        String bio = bioInputText.getText().toString().trim();
        String level = levelSpinner.getSelectedItem().toString();
        String city = citySpinner.getSelectedItem().toString();
        String times = timesInputText.getText().toString().trim();
        String techStack = techStackInputText.getText().toString().trim();
        String wantToLearn = wantToLearnInputText.getText().toString().trim();
        String goals = goalsInputText.getText().toString().trim();

        if (TextUtils.isEmpty(gender)) { toast("Please select a gender"); return; }
        if (TextUtils.isEmpty(bio)) { bioInputText.setError("Bio is required"); return; }
        if ("Select Level".equals(level)) { toast("Please select a level"); return; }
        if ("Select City".equals(city)) { toast("Please select a city"); return; }
        if (TextUtils.isEmpty(availability)) { toast("Please select availability"); return; }
        if (TextUtils.isEmpty(times)) { timesInputText.setError("Times field is required"); return; }
        if (TextUtils.isEmpty(techStack)) { techStackInputText.setError("Tech stack is required"); return; }
        if (TextUtils.isEmpty(wantToLearn)) { wantToLearnInputText.setError("Want to learn field is required"); return; }
        if (TextUtils.isEmpty(goals)) { goalsInputText.setError("Goals field is required"); return; }

        saveProfileData(gender, bio, level, city, availability, times, techStack, wantToLearn, goals);
    }

    private void saveProfileData(String gender, String bio, String level, String city,
                                 String availability, String times, String techStack,
                                 String wantToLearn, String goals) {
        Map<String, Object> profileData = new HashMap<>();
        profileData.put("gender", gender);
        profileData.put("bio", bio);
        profileData.put("level", level);
        profileData.put("city", city);
        profileData.put("availability", availability);
        profileData.put("times", times);
        profileData.put("techStack", techStack);
        profileData.put("wantToLearn", wantToLearn);
        profileData.put("goals", goals);
        profileData.put("profileCompleted", true);

        databaseReference.child(username).updateChildren(profileData)
                .addOnSuccessListener(aVoid -> {
                    toast("Profile updated successfully!");
                    startActivity(new Intent(this, HomepageActivity.class).putExtra("username", username));
                    finish();
                })
                .addOnFailureListener(e -> toast("Failed to update profile: " + e.getMessage()));
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
