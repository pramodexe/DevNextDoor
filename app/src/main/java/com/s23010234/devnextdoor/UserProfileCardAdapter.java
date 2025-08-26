package com.s23010234.devnextdoor;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

/**
 * This class is responsible for displaying user profile cards on the homepage.
 * It's like a factory that creates and manages the small cards you see for each user.
 * Each card shows a user's picture, name, bio, and what they want to learn.
 * Think of it as the worker that arranges all the user cards in a neat grid.
 */
public class UserProfileCardAdapter extends RecyclerView.Adapter<UserProfileCardAdapter.UserCardViewHolder> {

    // List of all the users that should be displayed as cards
    private List<User> userList;
    
    // Reference to the app context (used for accessing resources like images)
    private Context context;
    
    // The username of the current user (so we don't show their own card)
    private String currentUsername;

    /**
     * Creates a new adapter that will manage user profile cards.
     * This sets up everything needed to display user cards on the screen.
     */
    public UserProfileCardAdapter(Context context, List<User> userList, String currentUsername) {
        this.context = context;
        this.userList = userList;
        this.currentUsername = currentUsername;
    }

    /**
     * Creates a new view holder for a user card.
     * This method runs when we need to create a new card layout.
     * It inflates (loads) the XML layout for one user card.
     */
    @NonNull
    @Override
    public UserCardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Load the XML layout for a single user card
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_profile_card, parent, false);
        
        // Create and return a new view holder that manages this card
        return new UserCardViewHolder(view);
    }

    /**
     * Fills in the data for a specific user card.
     * This method runs for each card that needs to be displayed.
     * It takes user information and puts it into the card layout.
     */
    @Override
    public void onBindViewHolder(@NonNull UserCardViewHolder holder, int position) {
        // Get the user data for this card position
        User user = userList.get(position);
        
        // Tell the view holder to fill the card with this user's information
        holder.bind(user);
    }

    /**
     * Returns how many user cards should be displayed.
     * This tells the RecyclerView how many cards to create.
     */
    @Override
    public int getItemCount() {
        return userList.size();
    }

    /**
     * Updates the list of users and refreshes all the cards.
     * This is used when new users join or when we filter the list.
     */
    public void updateUserList(List<User> newUserList) {
        this.userList = newUserList;
        
        // Tell the RecyclerView to redraw all the cards with new data
        notifyDataSetChanged();
    }

    /**
     * This inner class manages one individual user card.
     * It holds references to all the parts of a card (like name, picture, bio)
     * and knows how to fill them with user information.
     */
    class UserCardViewHolder extends RecyclerView.ViewHolder {
        
        // References to all the visual elements in one user card
        private ImageView profilePicture;  // The user's profile picture
        private ImageView genderIcon;      // Small icon showing male/female
        private TextView username;         // The user's name
        private TextView bio;              // Short description about the user
        private TextView wantToLearn;      // What programming topics they want to learn
        private TextView status;           // Their current status or level

        /**
         * Sets up one user card by finding all its visual elements.
         * This connects the Java code to the XML layout elements.
         */
        public UserCardViewHolder(@NonNull View itemView) {
            super(itemView);
            
            // Find all the visual elements in this card's layout
            profilePicture = itemView.findViewById(R.id.cardProfilePicture);
            genderIcon = itemView.findViewById(R.id.cardGenderIcon);
            username = itemView.findViewById(R.id.cardUsername);
            bio = itemView.findViewById(R.id.cardBio);
            wantToLearn = itemView.findViewById(R.id.cardWantToLearn);
            status = itemView.findViewById(R.id.cardStatus);
        }

        /**
         * Fills this card with information from a specific user.
         * This method takes all the user's data and puts it into the card's visual elements.
         */
        public void bind(User user) {
            // Set the user's profile picture
            int profilePictureResId = user.getProfilePictureResourceId(context);
            profilePicture.setImageResource(profilePictureResId);

            // Set the gender icon (male or female symbol)
            int genderIconResId = user.getGenderIconResourceId(context);
            genderIcon.setImageResource(genderIconResId);

            // Set username
            username.setText("@" + user.getUsername());

            // Set truncated bio
            bio.setText(user.getTruncatedBio());

            // Set truncated want to learn
            wantToLearn.setText(user.getTruncatedWantToLearn());

            // Set status (education level)
            String statusText = user.getLevel();
            if (statusText == null || statusText.isEmpty()) {
                statusText = "Not specified";
            }
            status.setText(statusText);

            // Removed NEW badge display

            // Set click listener to navigate to user's profile
            itemView.setOnClickListener(v -> {
                if (!user.getUsername().equals(currentUsername)) {
                    Intent intent = new Intent(context, UserProfileViewActivity.class);
                    intent.putExtra("username", user.getUsername());
                    context.startActivity(intent);
                }
            });
        }
    }
}
