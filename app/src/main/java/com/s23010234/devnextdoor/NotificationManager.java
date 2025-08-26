package com.s23010234.devnextdoor;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.Query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Notification Manager - This class manages all notifications in the app
 * 
 * Think of this like a notification center that handles all the messages
 * and alerts users receive in the app. Just like your phone's notification
 * center can show you new messages, this class can:
 * - Create new notifications for users
 * - Show all notifications to a user
 * - Mark notifications as read (like when you've seen them)
 * - Delete notifications users don't want anymore
 * - Count how many unread notifications someone has
 * 
 * All notifications are stored in Firebase (cloud database) so users
 * can see them on any device they log in from.
 */
public class NotificationManager {
    // This is our connection to the notifications section of the Firebase database
    // Think of it like having a direct phone line to the notification storage room
    private DatabaseReference notificationsRef;

    /**
     * Constructor - Sets up the notification manager when it's created
     * 
     * This runs automatically when someone creates a new NotificationManager.
     * It connects to the Firebase database and gets ready to handle notifications.
     */
    public NotificationManager() {
        // Connect to the "notifications" section of our Firebase database
        // This is like opening the door to the notification storage room
        notificationsRef = FirebaseDatabase.getInstance().getReference("notifications");
    }

    /**
     * Callback Interface for Simple Operations
     * 
     * This interface is like a contract that defines what happens
     * when notification operations succeed or fail. Think of it like
     * agreeing on what to do if something works or doesn't work.
     */
    public interface NotificationCallback {
        // Called when the operation works successfully
        void onSuccess(boolean result);
        // Called when something goes wrong
        void onError(String error);
    }

    /**
     * Callback Interface for Getting Lists of Notifications
     * 
     * This interface is specifically for operations that return
     * a list of notifications (like getting all notifications for a user).
     */
    public interface NotificationsListCallback {
        // Called when we successfully get the list of notifications
        void onSuccess(List<Notification> notifications);
        // Called when something goes wrong while getting the list
        void onError(String error);
    }

    /**
     * Add a New Notification for a User
     * 
     * This method creates a new notification for a specific user.
     * It's like sending a message to someone's notification inbox.
     * 
     * @param username The person who should receive this notification
     * @param notification The actual notification message to send
     * @param callback What to do when this succeeds or fails
     */
    public void addNotification(String username, Notification notification, NotificationCallback callback) {
        // Check if the username is valid (not empty or null)
        // This is like making sure we have a valid address before sending mail
        if (username == null || username.isEmpty()) {
            callback.onError("Username cannot be empty");
            return;
        }

        // Check if the notification is valid (not null)
        // This is like making sure we actually have a message to send
        if (notification == null) {
            callback.onError("Notification cannot be null");
            return;
        }

        // Save the notification to Firebase under the user's folder
        // This is like putting the notification in the user's personal mailbox
        notificationsRef.child(username)
                .child(notification.getId())
                .setValue(notification)
                .addOnSuccessListener(aVoid -> callback.onSuccess(true))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    /**
     * Get All Notifications for a User
     * 
     * This method gets all notifications that belong to a specific user.
     * It's like opening someone's notification inbox and reading all their messages.
     * The notifications are automatically sorted with newest ones first.
     * 
     * @param username The person whose notifications we want to get
     * @param callback What to do with the notifications when we get them (or if we fail)
     */
    public void getUserNotifications(String username, NotificationsListCallback callback) {
        // Check if the username is valid (not empty or null)
        // This is like making sure we know whose mailbox to check
        if (username == null || username.isEmpty()) {
            callback.onError("Username cannot be empty");
            return;
        }

        // Go to Firebase and get all notifications for this user
        // This is like going to the user's personal notification folder
        notificationsRef.child(username)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // Create a list to store all the notifications we find
                        List<Notification> notifications = new ArrayList<>();
                        
                        // Go through each notification in the database
                        // This is like reading each message in the mailbox one by one
                        for (DataSnapshot notificationSnapshot : dataSnapshot.getChildren()) {
                            // Convert the database data back into a Notification object
                            Notification notification = notificationSnapshot.getValue(Notification.class);
                            if (notification != null) {
                                // Add this notification to our list
                                notifications.add(notification);
                            }
                        }
                        
                        // Sort notifications by timestamp (newest first)
                        // This is like arranging the messages with the most recent ones on top
                        Collections.sort(notifications, new Comparator<Notification>() {
                            @Override
                            public int compare(Notification n1, Notification n2) {
                                // Compare timestamps - newer notifications come first
                                return Long.compare(n2.getTimestamp(), n1.getTimestamp());
                            }
                        });
                        
                        // Send the sorted list back to whoever requested it
                        callback.onSuccess(notifications);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        // If something went wrong while getting the notifications
                        callback.onError(databaseError.getMessage());
                    }
                });
    }

    /**
     * Mark a Notification as Read
     * 
     * This method marks a specific notification as "read" so the user
     * knows they've already seen it. It's like marking an email as read
     * after you've opened it.
     * 
     * @param username The person whose notification we're marking as read
     * @param notificationId The specific notification to mark as read
     * @param callback What to do when this succeeds or fails
     */
    public void markNotificationAsRead(String username, String notificationId, NotificationCallback callback) {
        // Check if the username is valid (not empty or null)
        if (username == null || username.isEmpty()) {
            callback.onError("Username cannot be empty");
            return;
        }

        // Check if the notification ID is valid (not empty or null)
        if (notificationId == null || notificationId.isEmpty()) {
            callback.onError("Notification ID cannot be empty");
            return;
        }

        // Find the specific notification in Firebase and mark it as read
        // This is like finding a specific message and putting a "read" stamp on it
        notificationsRef.child(username)
                .child(notificationId)
                .child("read")
                .setValue(true)
                .addOnSuccessListener(aVoid -> callback.onSuccess(true))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    /**
     * Delete a Notification
     * 
     * This method completely removes a notification from the database.
     * It's like throwing away a message you don't want anymore.
     * Once deleted, the notification cannot be recovered.
     * 
     * @param username The person whose notification we're deleting
     * @param notificationId The specific notification to delete
     * @param callback What to do when this succeeds or fails
     */
    public void deleteNotification(String username, String notificationId, NotificationCallback callback) {
        // Check if the username is valid (not empty or null)
        if (username == null || username.isEmpty()) {
            callback.onError("Username cannot be empty");
            return;
        }

        // Check if the notification ID is valid (not empty or null)
        if (notificationId == null || notificationId.isEmpty()) {
            callback.onError("Notification ID cannot be empty");
            return;
        }

        // Find the specific notification in Firebase and completely remove it
        // This is like finding a specific message and throwing it in the trash
        notificationsRef.child(username)
                .child(notificationId)
                .removeValue()
                .addOnSuccessListener(aVoid -> callback.onSuccess(true))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    /**
     * Get Count of Unread Notifications
     * 
     * This method counts how many notifications a user has that they
     * haven't read yet. It's like counting how many unopened emails
     * someone has in their inbox.
     * 
     * @param username The person whose unread notifications we want to count
     * @param callback What to do with the count when we get it (or if we fail)
     */
    public void getUnreadNotificationsCount(String username, final UnreadCountCallback callback) {
        // Check if the username is valid (not empty or null)
        if (username == null || username.isEmpty()) {
            callback.onError("Username cannot be empty");
            return;
        }

        // Create a query to find only notifications that are marked as unread (read = false)
        // This is like asking "show me only the unopened mail"
        Query unreadQuery = notificationsRef.child(username).orderByChild("read").equalTo(false);
        unreadQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Count how many unread notifications we found
                // This is like counting the unopened messages
                int count = (int) dataSnapshot.getChildrenCount();
                callback.onSuccess(count);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // If something went wrong while counting
                callback.onError(databaseError.getMessage());
            }
        });
    }

    /**
     * Callback Interface for Unread Count Operations
     * 
     * This interface is specifically for operations that return
     * a count of unread notifications.
     */
    public interface UnreadCountCallback {
        // Called when we successfully get the unread count
        void onSuccess(int count);
        // Called when something goes wrong while counting
        void onError(String error);
    }

    /**
     * Helper Methods for Creating Specific Notification Types
     * 
     * These methods are like templates for creating common types of notifications.
     * Instead of manually creating the same notifications over and over,
     * we can use these helper methods to create them quickly and consistently.
     */

    /**
     * Create a "Profile Created" Notification
     * 
     * This creates a welcome notification for when someone first creates their profile.
     * It's like sending a welcome card to a new member.
     * 
     * @return A pre-made notification welcoming the user
     */
    public static Notification createProfileCreatedNotification() {
        return new Notification(
                "Welcome to DevNextDoor!",
                "Your profile has been created successfully. Start connecting with other developers!",
                Notification.Types.PROFILE_CREATED
        );
    }

    /**
     * Create a "Password Changed" Notification
     * 
     * This creates a security notification for when someone changes their password.
     * It's like sending a security alert to let users know their account was modified.
     * 
     * @return A pre-made notification about password changes
     */
    public static Notification createPasswordChangedNotification() {
        return new Notification(
                "Password Updated",
                "Your password has been changed successfully. If this wasn't you, please contact support.",
                Notification.Types.PASSWORD_CHANGED
        );
    }

    /**
     * Create a "Username Changed" Notification
     * 
     * This creates a notification for when someone changes their username.
     * It shows both the old and new username so users can see what changed.
     * 
     * @param oldUsername What their username used to be
     * @param newUsername What their username is now
     * @return A pre-made notification about the username change
     */
    public static Notification createUsernameChangedNotification(String oldUsername, String newUsername) {
        return new Notification(
                "Username Updated",
                "Your username has been changed from '" + oldUsername + "' to '" + newUsername + "'.",
                Notification.Types.USERNAME_CHANGED
        );
    }
}
