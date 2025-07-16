package com.s23010234.devnextdoor;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import java.util.HashMap;
import java.util.Map;

// FirebaseHelper manages Firebase database operations for user authentication
public class FirebaseHelper {

    private DatabaseReference databaseReference;

    public FirebaseHelper() {
        // Initialize Firebase database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("users");
    }

    // Interface for callbacks
    public interface DatabaseCallback {
        void onSuccess(boolean result);
        void onError(String error);
    }

    public interface UserExistsCallback {
        void onResult(boolean exists);
        void onError(String error);
    }

    // Add new user
    public void addUser(String username, String password, DatabaseCallback callback) {
        Map<String, Object> user = new HashMap<>();
        user.put("username", username);
        user.put("password", password);

        databaseReference.child(username).setValue(user)
                .addOnSuccessListener(aVoid -> callback.onSuccess(true))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    // Check if username exists
    public void isUsernameExists(String username, UserExistsCallback callback) {
        databaseReference.child(username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                callback.onResult(dataSnapshot.exists());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onError(databaseError.getMessage());
            }
        });
    }

    // Validate user credentials
    public void validateUser(String username, String password, DatabaseCallback callback) {
        databaseReference.child(username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String storedPassword = dataSnapshot.child("password").getValue(String.class);
                    boolean isValid = storedPassword != null && storedPassword.equals(password);
                    callback.onSuccess(isValid);
                } else {
                    callback.onSuccess(false);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onError(databaseError.getMessage());
            }
        });
    }
}
