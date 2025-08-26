package com.s23010234.devnextdoor;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class handles all communication with Firebase database.
 * Firebase is where we store user information in the cloud.
 * Think of this class as the bridge between our app and the online database.
 * It can create new users, check passwords, save preferences, and more.
 */
public class FirebaseHelper {

    // Connection to the Firebase database where user information is stored
    private DatabaseReference databaseReference;

    /**
     * Creates a new FirebaseHelper and sets up the connection to the database.
     * This sets up the link to the "users" section of our Firebase database.
     */
    public FirebaseHelper() {
        // Connect to the Firebase database and point to the "users" section
        databaseReference = FirebaseDatabase.getInstance().getReference("users");
    }

    /**
     * Interface for methods that need to tell us if something worked or failed.
     * This is used when we do things like create users or delete accounts.
     */
    public interface DatabaseCallback {
        void onSuccess(boolean result);
        void onError(String error);
    }

    /**
     * Interface for methods that need to tell us if a username exists or not.
     * This is used when checking if someone can use a particular username.
     */
    public interface UserExistsCallback {
        void onResult(boolean exists);
        void onError(String error);
    }

    /**
     * Interface for methods that need to tell us about dark mode preferences.
     * This is used when loading a user's theme preference from the database.
     */
    public interface DarkModeCallback {
        void onResult(boolean isDarkMode);
        void onError(String error);
    }

    /**
     * Creates a new user account in the Firebase database.
     * This saves the username, password, and sets light mode as default.
     */
    public void addUser(String username, String password, DatabaseCallback callback) {
        // Create a map (like a dictionary) to hold the user's information
        Map<String, Object> user = new HashMap<>();
        user.put("username", username);
        user.put("password", password);
        user.put("isDarkMode", false); // Start new users with light mode

        // Save this information to Firebase under the user's username
        databaseReference.child(username).setValue(user)
            .addOnSuccessListener(aVoid -> callback.onSuccess(true))
            .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    /**
     * Checks if a username is already taken by looking in the database.
     * This prevents two users from having the same username.
     */
    public void isUsernameExists(String username, UserExistsCallback callback) {
        // Look up this username in the database
        databaseReference.child(username).addListenerForSingleValueEvent(new ValueEventListener() {
            /**
             * This method runs when we get an answer from the database.
             * It tells us whether the username exists or not.
             */
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // If the username exists in the database, return true
                callback.onResult(dataSnapshot.exists());
            }

            /**
             * This method runs if there's an error connecting to the database.
             */
            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onError(databaseError.getMessage());
            }
        });
    }

    /**
     * Checks if a username and password combination is correct.
     * This is used during login to verify the user's credentials.
     */
    public void validateUser(String username, String password, DatabaseCallback callback) {
        // Look up this username in the database
        databaseReference.child(username).addListenerForSingleValueEvent(new ValueEventListener() {
            /**
             * This method runs when we get the user's data from the database.
             * It checks if the provided password matches the stored password.
             */
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Check if this username exists in the database
                if (dataSnapshot.exists()) {
                    // Get the stored password for this username
                    String storedPassword = dataSnapshot.child("password").getValue(String.class);
                    
                    // Check if the provided password matches the stored password
                    boolean isValid = storedPassword != null && storedPassword.equals(password);
                    callback.onSuccess(isValid);
                } else {
                    // Username doesn't exist, so login is invalid
                    callback.onSuccess(false);
                }
            }

            /**
             * This method runs if there's an error connecting to the database.
             */
            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onError(databaseError.getMessage());
            }
        });
    }

    /**
     * Completely removes a user's account from the Firebase database.
     * This is used when someone wants to delete their account permanently.
     */
    public void deleteUser(String username, DatabaseCallback callback) {
        // Remove all data for this username from the database
        databaseReference.child(username).removeValue()
                .addOnSuccessListener(aVoid -> callback.onSuccess(true))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    /**
     * Changes a user's username by moving their data to a new username.
     * This is used when someone wants to change their username.
     * It also updates all references to the old username throughout the database.
     */
    public void updateUsername(String oldUsername, String newUsername, DatabaseCallback callback) {
        // First check if the new username is already taken
        isUsernameExists(newUsername, new UserExistsCallback() {
            /**
             * This method runs when we find out if the new username is available.
             */
            @Override
            public void onResult(boolean exists) {
                if (exists) {
                    callback.onError("Username already exists");
                    return;
                }
                
                // Get current user data
                databaseReference.child(oldUsername).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            // Get all user data
                            Map<String, Object> userData = (Map<String, Object>) dataSnapshot.getValue();
                            if (userData != null) {
                                // Update username field in the data
                                userData.put("username", newUsername);
                                
                                // Create new user entry with new username
                                databaseReference.child(newUsername).setValue(userData)
                                    .addOnSuccessListener(aVoid -> {
                                        // Update all chats and notifications, then delete old username entry
                                        updateChatsAndNotifications(oldUsername, newUsername, new DatabaseCallback() {
                                            @Override
                                            public void onSuccess(boolean result) {
                                                // Delete old username entry
                                                databaseReference.child(oldUsername).removeValue()
                                                    .addOnSuccessListener(aVoid2 -> callback.onSuccess(true))
                                                    .addOnFailureListener(e -> callback.onError("Failed to delete old username: " + e.getMessage()));
                                            }
                                            
                                            @Override
                                            public void onError(String error) {
                                                callback.onError("Failed to update chats and notifications: " + error);
                                            }
                                        });
                                    })
                                    .addOnFailureListener(e -> callback.onError("Failed to create new username: " + e.getMessage()));
                            } else {
                                callback.onError("User data not found");
                            }
                        } else {
                            callback.onError("Original user not found");
                        }
                    }
                    
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        callback.onError(databaseError.getMessage());
                    }
                });
            }
            
            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }
    
    /**
     * Update all chats and notifications when username changes
     */
    private void updateChatsAndNotifications(String oldUsername, String newUsername, DatabaseCallback callback) {
        // Get references to chats and notifications
        DatabaseReference chatsRef = FirebaseDatabase.getInstance().getReference("chats");
        DatabaseReference notificationsRef = FirebaseDatabase.getInstance().getReference("notifications");
        
        // First, move notifications
        notificationsRef.child(oldUsername).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot notificationsSnapshot) {
                if (notificationsSnapshot.exists()) {
                    // Move all notifications to new username
                    notificationsRef.child(newUsername).setValue(notificationsSnapshot.getValue())
                        .addOnSuccessListener(aVoid -> {
                            // Delete old notifications
                            notificationsRef.child(oldUsername).removeValue()
                                .addOnSuccessListener(aVoid2 -> {
                                    // Now update chats
                                    updateChatsReferences(chatsRef, oldUsername, newUsername, callback);
                                })
                                .addOnFailureListener(e -> callback.onError("Failed to delete old notifications: " + e.getMessage()));
                        })
                        .addOnFailureListener(e -> callback.onError("Failed to move notifications: " + e.getMessage()));
                } else {
                    // No notifications to move, just update chats
                    updateChatsReferences(chatsRef, oldUsername, newUsername, callback);
                }
            }
            
            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onError("Failed to read notifications: " + databaseError.getMessage());
            }
        });
    }
    
    /**
     * Update chat participant references
     */
    private void updateChatsReferences(DatabaseReference chatsRef, String oldUsername, String newUsername, DatabaseCallback callback) {
        chatsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot chatsSnapshot) {
                Map<String, Object> chatUpdates = new HashMap<>();
                boolean hasUpdates = false;
                
                for (DataSnapshot chatSnapshot : chatsSnapshot.getChildren()) {
                    String chatId = chatSnapshot.getKey();
                    String participant1 = chatSnapshot.child("participant1").getValue(String.class);
                    String participant2 = chatSnapshot.child("participant2").getValue(String.class);
                    
                    boolean needsUpdate = false;
                    
                    // Check if this user is participant1
                    if (oldUsername.equals(participant1)) {
                        chatUpdates.put(chatId + "/participant1", newUsername);
                        needsUpdate = true;
                    }
                    
                    // Check if this user is participant2
                    if (oldUsername.equals(participant2)) {
                        chatUpdates.put(chatId + "/participant2", newUsername);
                        needsUpdate = true;
                    }
                    
                    if (needsUpdate) {
                        hasUpdates = true;
                    }
                }
                
                if (hasUpdates) {
                    // Update all chats at once
                    chatsRef.updateChildren(chatUpdates)
                        .addOnSuccessListener(aVoid -> {
                            // Now update message references
                            updateMessageReferences(oldUsername, newUsername, callback);
                        })
                        .addOnFailureListener(e -> callback.onError("Failed to update chat participants: " + e.getMessage()));
                } else {
                    // No chats to update, just update message references
                    updateMessageReferences(oldUsername, newUsername, callback);
                }
            }
            
            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onError("Failed to read chats: " + databaseError.getMessage());
            }
        });
    }
    
    /**
     * Update message sender/receiver references
     */
    private void updateMessageReferences(String oldUsername, String newUsername, DatabaseCallback callback) {
        DatabaseReference messagesRef = FirebaseDatabase.getInstance().getReference("messages");
        
        messagesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot messagesSnapshot) {
                Map<String, Object> messageUpdates = new HashMap<>();
                boolean hasUpdates = false;
                
                for (DataSnapshot chatSnapshot : messagesSnapshot.getChildren()) {
                    String chatId = chatSnapshot.getKey();
                    
                    for (DataSnapshot messageSnapshot : chatSnapshot.getChildren()) {
                        String messageId = messageSnapshot.getKey();
                        String senderId = messageSnapshot.child("senderId").getValue(String.class);
                        String receiverId = messageSnapshot.child("receiverId").getValue(String.class);
                        
                        // Check if this user is the sender
                        if (oldUsername.equals(senderId)) {
                            messageUpdates.put(chatId + "/" + messageId + "/senderId", newUsername);
                            hasUpdates = true;
                        }
                        
                        // Check if this user is the receiver
                        if (oldUsername.equals(receiverId)) {
                            messageUpdates.put(chatId + "/" + messageId + "/receiverId", newUsername);
                            hasUpdates = true;
                        }
                    }
                }
                
                if (hasUpdates) {
                    // Update all messages at once
                    messagesRef.updateChildren(messageUpdates)
                        .addOnSuccessListener(aVoid -> callback.onSuccess(true))
                        .addOnFailureListener(e -> callback.onError("Failed to update message references: " + e.getMessage()));
                } else {
                    // No messages to update
                    callback.onSuccess(true);
                }
            }
            
            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onError("Failed to read messages: " + databaseError.getMessage());
            }
        });
    }

    // Update password for existing user
    public void updatePassword(String username, String newPassword, DatabaseCallback callback) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("password", newPassword);
        
        databaseReference.child(username).updateChildren(updates)
                .addOnSuccessListener(aVoid -> callback.onSuccess(true))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    /**
     * Updates the dark mode preference for a user in Firebase database
     * @param username String - The username of the user
     * @param isDarkMode boolean - True for dark mode, false for light mode
     * @param callback DatabaseCallback - Callback to handle success/error
     */
    public void updateDarkModePreference(String username, boolean isDarkMode, DatabaseCallback callback) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("isDarkMode", isDarkMode);
        
        databaseReference.child(username).updateChildren(updates)
                .addOnSuccessListener(aVoid -> callback.onSuccess(true))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    /**
     * Retrieves the dark mode preference for a user from Firebase database
     * @param username String - The username of the user
     * @param callback DarkModeCallback - Callback to handle the result
     */
    public void getDarkModePreference(String username, DarkModeCallback callback) {
        databaseReference.child(username).child("isDarkMode").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Boolean isDarkMode = dataSnapshot.getValue(Boolean.class);
                    // If isDarkMode is null, default to false (light mode)
                    callback.onResult(isDarkMode != null ? isDarkMode : false);
                } else {
                    // If the field doesn't exist, default to false (light mode)
                    callback.onResult(false);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onError(databaseError.getMessage());
            }
        });
    }

    // Interface for favorites callbacks
    public interface FavoritesCallback {
        void onSuccess(boolean isFavorite);
        void onError(String error);
    }

    public interface FavoriteListCallback {
        void onSuccess(List<String> favorites);
        void onError(String error);
    }

    /**
     * Adds a user to the current user's favorites list
     * @param currentUsername String - The username of the current user
     * @param favoriteUsername String - The username of the user to add to favorites
     * @param callback DatabaseCallback - Callback to handle success/error
     */
    public void addToFavorites(String currentUsername, String favoriteUsername, DatabaseCallback callback) {
        databaseReference.child(currentUsername).child("favorites").child(favoriteUsername).setValue(true)
                .addOnSuccessListener(aVoid -> callback.onSuccess(true))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    /**
     * Removes a user from the current user's favorites list
     * @param currentUsername String - The username of the current user
     * @param favoriteUsername String - The username of the user to remove from favorites
     * @param callback DatabaseCallback - Callback to handle success/error
     */
    public void removeFromFavorites(String currentUsername, String favoriteUsername, DatabaseCallback callback) {
        databaseReference.child(currentUsername).child("favorites").child(favoriteUsername).removeValue()
                .addOnSuccessListener(aVoid -> callback.onSuccess(true))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    /**
     * Checks if a user is in the current user's favorites list
     * @param currentUsername String - The username of the current user
     * @param favoriteUsername String - The username to check
     * @param callback FavoritesCallback - Callback to handle the result
     */
    public void isFavorite(String currentUsername, String favoriteUsername, FavoritesCallback callback) {
        databaseReference.child(currentUsername).child("favorites").child(favoriteUsername)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        callback.onSuccess(dataSnapshot.exists());
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        callback.onError(databaseError.getMessage());
                    }
                });
    }

    /**
     * Gets the list of favorite usernames for the current user
     * @param currentUsername String - The username of the current user
     * @param callback FavoriteListCallback - Callback to handle the result
     */
    public void getFavoritesList(String currentUsername, FavoriteListCallback callback) {
        databaseReference.child(currentUsername).child("favorites")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        List<String> favoritesList = new ArrayList<>();
                        for (DataSnapshot favoriteSnapshot : dataSnapshot.getChildren()) {
                            favoritesList.add(favoriteSnapshot.getKey());
                        }
                        callback.onSuccess(favoritesList);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        callback.onError(databaseError.getMessage());
                    }
                });
    }
}
