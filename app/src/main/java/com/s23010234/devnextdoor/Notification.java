package com.s23010234.devnextdoor;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * This class represents a notification that appears in the user's notification list.
 * Notifications tell users about important events like profile creation, password changes, etc.
 * Each notification has a title, message, timestamp, and can be marked as read or unread.
 * Think of it as a digital note that the app leaves for the user.
 */
public class Notification {
    
    // A unique ID that identifies this specific notification
    private String id;
    
    // The main heading of the notification (like "Welcome!" or "Profile Updated")
    private String title;
    
    // The detailed text that explains what happened
    private String message;
    
    // What category this notification belongs to (like "profile_created" or "password_changed")
    private String type;
    
    // When this notification was created (as a timestamp number)
    private long timestamp;
    
    // Whether the user has seen this notification yet (true = read, false = unread)
    private boolean isRead;

    /**
     * Empty constructor that Firebase needs to create Notification objects.
     * Firebase uses this when loading notification data from the database.
     */
    public Notification() {}

    /**
     * Constructor to create a new notification with title, message, and type.
     * This automatically sets the ID, timestamp, and marks it as unread.
     */
    public Notification(String title, String message, String type) {
        // Create a unique ID using the current time
        this.id = String.valueOf(System.currentTimeMillis());
        this.title = title;
        this.message = message;
        this.type = type;
        
        // Set when this notification was created
        this.timestamp = System.currentTimeMillis();
        
        // New notifications start as unread
        this.isRead = false;
    }

    // These are getter and setter methods - they let other parts of the app
    // read and change the notification information safely

    /**
     * Get the notification's unique ID.
     * Returns the ID that identifies this specific notification.
     */
    public String getId() {
        return id;
    }

    /**
     * Set the notification's unique ID.
     * Changes the ID that identifies this specific notification.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Get the notification's title.
     * Returns the main heading of the notification.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Set the notification's title.
     * Changes the main heading of the notification.
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Get the notification's message.
     * Returns the detailed text that explains what happened.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Set the notification's message.
     * Changes the detailed text that explains what happened.
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Get the notification's type.
     * Returns what category this notification belongs to.
     */
    public String getType() {
        return type;
    }

    /**
     * Set the notification's type.
     * Changes what category this notification belongs to.
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Get when the notification was created.
     * Returns the timestamp showing exactly when this notification was made.
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Set when the notification was created.
     * Changes the timestamp showing when this notification was made.
     */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Check if the notification has been read.
     * Returns true if the user has seen this notification, false if it's still unread.
     */
    public boolean isRead() {
        return isRead;
    }

    /**
     * Set whether the notification has been read.
     * Changes if the user has seen this notification (true = read, false = unread).
     */
    public void setRead(boolean read) {
        isRead = read;
    }

    /**
     * Get a nicely formatted date and time for this notification.
     * Returns a string like "Dec 25, 2023 at 14:30" that's easy to read.
     */
    public String getFormattedDate() {
        // Create a formatter that makes dates look nice for users
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault());
        
        // Convert the timestamp to a readable date string
        return sdf.format(new Date(timestamp));
    }

    /**
     * Get how long ago this notification was created.
     * Returns a string like "2 hours ago" or "3 days ago" that shows relative time.
     * This is more user-friendly than exact timestamps for recent notifications.
     */
    public String getTimeAgo() {
        // Calculate how much time has passed since this notification was created
        long now = System.currentTimeMillis();
        long diff = now - timestamp;

        // Convert milliseconds to more understandable units
        long seconds = diff / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        // Return the most appropriate time description
        if (days > 0) {
            return days + (days == 1 ? " day ago" : " days ago");
        } else if (hours > 0) {
            return hours + (hours == 1 ? " hour ago" : " hours ago");
        } else if (minutes > 0) {
            return minutes + (minutes == 1 ? " minute ago" : " minutes ago");
        } else {
            return "Just now";
        }
    }

    /**
     * This inner class defines the different types of notifications we can have.
     * It keeps all the notification categories in one place so we don't make typos
     * and can easily add new types later.
     */
    public static class Types {
        // Notification sent when a user first creates their profile
        public static final String PROFILE_CREATED = "profile_created";
        
        // Notification sent when a user changes their password
        public static final String PASSWORD_CHANGED = "password_changed";
        
        // Notification sent when a user changes their username
        public static final String USERNAME_CHANGED = "username_changed";
        
        // General notification for other types of messages
        public static final String GENERAL = "general";
    }
}
