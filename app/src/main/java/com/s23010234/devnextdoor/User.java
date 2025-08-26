package com.s23010234.devnextdoor;

/**
 * This class represents a user in the app.
 * It holds all the information about a person, like their name, what they want to learn,
 * where they live, and their preferences. Think of it as a digital ID card for each user.
 * This information gets saved to Firebase database so other users can see profiles.
 */
public class User {
    
    // The user's unique username that they use to log in
    private String username;
    
    // Whether the user is Male or Female
    private String gender;
    
    // A short description the user writes about themselves
    private String bio;
    
    // List of programming topics or skills this user wants to learn
    private String wantToLearn;
    
    // The filename of the user's chosen profile picture
    private String profilePicture;
    
    // The user's experience level (like Beginner, Intermediate, Advanced)
    private String level;
    
    // The city where the user lives
    private String city;
    
    // The programming languages and tools the user knows
    private String techStack;
    
    // What the user hopes to achieve through learning
    private String goals;
    
    // When the user is available to chat or meet (like weekdays, weekends)
    private String availability;
    
    // What time of day the user prefers to be active (morning, evening, etc.)
    private String timeOfDay;
    
    // Whether the user has finished filling out their complete profile
    private boolean profileCompleted;
    
    // When the user created their account (used to show newest users first)
    private long timestamp;
    
    // The user's exact location coordinates (latitude - how far north/south)
    private double latitude;
    
    // The user's exact location coordinates (longitude - how far east/west)
    private double longitude;

    /**
     * Empty constructor that Firebase needs to create User objects.
     * Firebase uses this when loading user data from the database.
     */
    public User() {
    }

    /**
     * Constructor to create a new User with basic information.
     * This is used when creating a user without location coordinates.
     */
    public User(String username, String gender, String bio, String wantToLearn, String profilePicture,
                String level, String city, String techStack, String goals, String availability,
                String timeOfDay, boolean profileCompleted, long timestamp) {
        this.username = username;
        this.gender = gender;
        this.bio = bio;
        this.wantToLearn = wantToLearn;
        this.profilePicture = profilePicture;
        this.level = level;
        this.city = city;
        this.techStack = techStack;
        this.goals = goals;
        this.availability = availability;
        this.timeOfDay = timeOfDay;
        this.profileCompleted = profileCompleted;
        this.timestamp = timestamp;
        
        // Set default location coordinates to 0 if not provided
        this.latitude = 0.0;
        this.longitude = 0.0;
    }

    /**
     * Constructor to create a new User with complete information including location.
     * This is used when creating a user with their exact location coordinates.
     */
    public User(String username, String gender, String bio, String wantToLearn, String profilePicture,
                String level, String city, String techStack, String goals, String availability,
                String timeOfDay, boolean profileCompleted, long timestamp, double latitude, double longitude) {
        this.username = username;
        this.gender = gender;
        this.bio = bio;
        this.wantToLearn = wantToLearn;
        this.profilePicture = profilePicture;
        this.level = level;
        this.city = city;
        this.techStack = techStack;
        this.goals = goals;
        this.availability = availability;
        this.timeOfDay = timeOfDay;
        this.profileCompleted = profileCompleted;
        this.timestamp = timestamp;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // These are getter and setter methods. They allow other parts of the app
    // to read (get) and change (set) the user's information safely.

    /**
     * Get the user's username.
     * Returns the unique name this user uses to log in.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Set the user's username.
     * Changes the unique name this user uses to log in.
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Get the user's gender.
     * Returns whether the user is Male or Female.
     */
    public String getGender() {
        return gender;
    }

    /**
     * Set the user's gender.
     * Changes whether the user is Male or Female.
     */
    public void setGender(String gender) {
        this.gender = gender;
    }

    /**
     * Get the user's bio.
     * Returns the short description the user wrote about themselves.
     */
    public String getBio() {
        return bio;
    }

    /**
     * Set the user's bio.
     * Changes the short description the user wrote about themselves.
     */
    public void setBio(String bio) {
        this.bio = bio;
    }

    /**
     * Get what the user wants to learn.
     * Returns the list of programming topics or skills this user wants to learn.
     */
    public String getWantToLearn() {
        return wantToLearn;
    }

    /**
     * Set what the user wants to learn.
     * Changes the list of programming topics or skills this user wants to learn.
     */
    public void setWantToLearn(String wantToLearn) {
        this.wantToLearn = wantToLearn;
    }

    /**
     * Get the user's profile picture filename.
     * Returns the name of the image file used as the user's profile picture.
     */
    public String getProfilePicture() {
        return profilePicture;
    }

    /**
     * Set the user's profile picture filename.
     * Changes the name of the image file used as the user's profile picture.
     */
    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    /**
     * Get the user's experience level.
     * Returns the user's skill level like Beginner, Intermediate, or Advanced.
     */
    public String getLevel() {
        return level;
    }

    /**
     * Set the user's experience level.
     * Changes the user's skill level like Beginner, Intermediate, or Advanced.
     */
    public void setLevel(String level) {
        this.level = level;
    }

    /**
     * Get the user's city.
     * Returns the name of the city where the user lives.
     */
    public String getCity() {
        return city;
    }

    /**
     * Set the user's city.
     * Changes the name of the city where the user lives.
     */
    public void setCity(String city) {
        this.city = city;
    }

    /**
     * Get the user's tech stack.
     * Returns the programming languages and tools the user knows.
     */
    public String getTechStack() {
        return techStack;
    }

    /**
     * Set the user's tech stack.
     * Changes the programming languages and tools the user knows.
     */
    public void setTechStack(String techStack) {
        this.techStack = techStack;
    }

    /**
     * Get the user's goals.
     * Returns what the user hopes to achieve through learning.
     */
    public String getGoals() {
        return goals;
    }

    /**
     * Set the user's goals.
     * Changes what the user hopes to achieve through learning.
     */
    public void setGoals(String goals) {
        this.goals = goals;
    }

    /**
     * Get the user's availability.
     * Returns when the user is available to chat or meet.
     */
    public String getAvailability() {
        return availability;
    }

    /**
     * Set the user's availability.
     * Changes when the user is available to chat or meet.
     */
    public void setAvailability(String availability) {
        this.availability = availability;
    }

    /**
     * Get the user's preferred time of day.
     * Returns what time of day the user prefers to be active.
     */
    public String getTimeOfDay() {
        return timeOfDay;
    }

    /**
     * Set the user's preferred time of day.
     * Changes what time of day the user prefers to be active.
     */
    public void setTimeOfDay(String timeOfDay) {
        this.timeOfDay = timeOfDay;
    }

    /**
     * Check if the user's profile is completed.
     * Returns true if the user has filled out all their profile information.
     */
    public boolean isProfileCompleted() {
        return profileCompleted;
    }

    /**
     * Set whether the user's profile is completed.
     * Changes whether the user has filled out all their profile information.
     */
    public void setProfileCompleted(boolean profileCompleted) {
        this.profileCompleted = profileCompleted;
    }

    /**
     * Get the user's account creation timestamp.
     * Returns when the user created their account (used to show newest users first).
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Set the user's account creation timestamp.
     * Changes when the user created their account.
     */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Get the user's latitude coordinate.
     * Returns how far north or south the user is located.
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * Set the user's latitude coordinate.
     * Changes how far north or south the user is located.
     */
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    /**
     * Get the user's longitude coordinate.
     * Returns how far east or west the user is located.
     */
    public double getLongitude() {
        return longitude;
    }

    /**
     * Set the user's longitude coordinate.
     * Changes how far east or west the user is located.
     */
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    /**
     * Get a shortened version of the user's bio for display on user cards.
     * If the bio is longer than 80 characters, it cuts it short and adds "..."
     * This makes sure user cards don't become too tall with long descriptions.
     */
    public String getTruncatedBio() {
        // If there's no bio, show a default message
        if (bio == null || bio.isEmpty()) {
            return "No bio available";
        }
        
        // If the bio is short enough, show it all
        if (bio.length() <= 80) {
            return bio;
        }
        
        // If the bio is too long, cut it short and add "..."
        return bio.substring(0, 77) + "...";
    }

    /**
     * Get a shortened version of what the user wants to learn for display on user cards.
     * If there are more than 3 items, it shows only the first 3 and adds "..."
     * This keeps user cards clean and easy to read.
     */
    public String getTruncatedWantToLearn() {
        // If there's nothing specified, show a default message
        if (wantToLearn == null || wantToLearn.isEmpty()) {
            return "Not specified";
        }
        
        // Split the list by commas to count individual items
        String[] items = wantToLearn.split(",");
        
        // If there are 3 or fewer items, show them all
        if (items.length <= 3) {
            return wantToLearn;
        }
        
        // If there are more than 3 items, show only the first 3
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            result.append(items[i].trim());
            if (i < 2) {
                result.append(", ");
            }
        }
        result.append("...");
        
        return result.toString();
    }

    /**
     * Get the correct profile picture image for this user.
     * It looks for the user's chosen picture, and if that doesn't work,
     * it uses a default picture based on their gender.
     */
    public int getProfilePictureResourceId(android.content.Context context) {
        // Try to find the user's chosen profile picture
        if (profilePicture != null && !profilePicture.isEmpty()) {
            // Remove the .png part to get just the name
            String drawableName = profilePicture.replace(".png", "");
            
            // Look for this image in the app's resources
            int resourceId = context.getResources().getIdentifier(drawableName, "drawable", context.getPackageName());
            
            // If we found the image, use it
            if (resourceId != 0) {
                return resourceId;
            }
        }
        
        // If we couldn't find their chosen picture, use a default based on gender
        if ("Female".equals(gender)) {
            return context.getResources().getIdentifier("female_1", "drawable", context.getPackageName());
        } else {
            return context.getResources().getIdentifier("male_1", "drawable", context.getPackageName());
        }
    }

    /**
     * Get the correct gender icon for this user.
     * Returns a small icon image that shows if the user is male or female.
     */
    public int getGenderIconResourceId(android.content.Context context) {
        // Choose the icon based on the user's gender
        if ("Female".equals(gender)) {
            return context.getResources().getIdentifier("female_icon", "drawable", context.getPackageName());
        } else {
            return context.getResources().getIdentifier("male_icon", "drawable", context.getPackageName());
        }
    }
}
