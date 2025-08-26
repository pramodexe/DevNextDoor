package com.s23010234.devnextdoor;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;
import java.util.Calendar;

/**
 * Utility class to add specific test users (kamal and shehan) to the database
 */
public class AddTestUsers {
    
    private DatabaseReference databaseReference;
    
    public interface AddUsersCallback {
        void onSuccess(String message);
        void onError(String error);
    }
    
    public AddTestUsers() {
        databaseReference = FirebaseDatabase.getInstance().getReference("users");
    }
    
    /**
     * Add kamal and shehan users with dummy profile data
     */
    public void addUsers(AddUsersCallback callback) {
        // Create user data for kamal
        Map<String, Object> kamalData = createKamalUserData();
        
        // Create user data for shehan
        Map<String, Object> shehanData = createShehanUserData();
        
        // Add kamal first
        databaseReference.child("kamal").setValue(kamalData)
            .addOnSuccessListener(aVoid -> {
                // Then add shehan
                databaseReference.child("shehan").setValue(shehanData)
                    .addOnSuccessListener(aVoid2 -> {
                        callback.onSuccess("Successfully added users kamal and shehan!");
                    })
                    .addOnFailureListener(e -> {
                        callback.onError("Failed to add shehan: " + e.getMessage());
                    });
            })
            .addOnFailureListener(e -> {
                callback.onError("Failed to add kamal: " + e.getMessage());
            });
    }
    
    /**
     * Create user data for kamal
     */
    private Map<String, Object> createKamalUserData() {
        Map<String, Object> userData = new HashMap<>();
        
        // Basic account info
        userData.put("username", "kamal");
        userData.put("password", "kamal");
        userData.put("isDarkMode", false);
        
        // Account creation date: Random date in May 2025 (May 8, 2025)
        Calendar may2025 = Calendar.getInstance();
        may2025.set(2025, Calendar.MAY, 8, 14, 30, 0); // May 8, 2025 2:30 PM
        userData.put("accountCreationDate", may2025.getTimeInMillis());
        
        // Profile completion timestamp (a few days after account creation)
        Calendar profileCompletion = Calendar.getInstance();
        profileCompletion.set(2025, Calendar.MAY, 12, 10, 15, 0); // May 12, 2025 10:15 AM
        userData.put("timestamp", profileCompletion.getTimeInMillis());
        
        // Profile data
        userData.put("gender", "Male");
        userData.put("bio", "Full-stack developer passionate about mobile app development and cloud technologies. Always excited to collaborate on innovative projects! 🚀");
        userData.put("level", "University Student");
        userData.put("city", "Colombo");
        userData.put("availability", "Weekdays, Weekends");
        userData.put("timeOfDay", "Evening, Night");
        userData.put("techStack", "Java, Spring Boot, React, MySQL, Docker");
        userData.put("wantToLearn", "Kubernetes, Microservices, React Native, AWS");
        userData.put("goals", "Build a comprehensive project management platform that helps teams collaborate more effectively and track progress in real-time.");
        userData.put("profilePicture", "male_2.png");
        userData.put("profileCompleted", true);
        
        return userData;
    }
    
    /**
     * Create user data for shehan
     */
    private Map<String, Object> createShehanUserData() {
        Map<String, Object> userData = new HashMap<>();
        
        // Basic account info
        userData.put("username", "shehan");
        userData.put("password", "shehan");
        userData.put("isDarkMode", true); // Different dark mode preference
        
        // Account creation date: Random date in May 2025 (May 23, 2025)
        Calendar may2025 = Calendar.getInstance();
        may2025.set(2025, Calendar.MAY, 23, 9, 45, 0); // May 23, 2025 9:45 AM
        userData.put("accountCreationDate", may2025.getTimeInMillis());
        
        // Profile completion timestamp (same day as account creation)
        Calendar profileCompletion = Calendar.getInstance();
        profileCompletion.set(2025, Calendar.MAY, 23, 16, 20, 0); // May 23, 2025 4:20 PM
        userData.put("timestamp", profileCompletion.getTimeInMillis());
        
        // Profile data
        userData.put("gender", "Male");
        userData.put("bio", "Frontend enthusiast with a keen eye for UI/UX design. Love creating beautiful, responsive web applications that provide excellent user experiences! ✨");
        userData.put("level", "Self-taught");
        userData.put("city", "Kandy");
        userData.put("availability", "Weekends");
        userData.put("timeOfDay", "Morning, Day");
        userData.put("techStack", "JavaScript, React, Vue.js, HTML, CSS, Figma");
        userData.put("wantToLearn", "TypeScript, Next.js, Node.js, GraphQL");
        userData.put("goals", "Create a modern e-learning platform with interactive courses and real-time collaboration features for students and educators.");
        userData.put("profilePicture", "male_4.png");
        userData.put("profileCompleted", true);
        
        return userData;
    }
}
