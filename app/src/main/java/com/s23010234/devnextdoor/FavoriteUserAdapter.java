package com.s23010234.devnextdoor;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

/**
 * Favorite User Adapter - Manages the display of favorite users
 * 
 * This adapter is responsible for showing the list of users that the
 * current user has marked as favorites. Think of it like a photo album
 * manager that displays your favorite people in a organized list.
 * 
 * It creates a visual card for each favorite user showing their:
 * - Profile picture
 * - Username
 * - Basic information
 * 
 * When users tap on a favorite person's card, it opens their full profile.
 * This adapter is used specifically in the Favorites screen to show
 * all the people a user has added to their favorites list.
 */
public class FavoriteUserAdapter extends RecyclerView.Adapter<FavoriteUserAdapter.FavoriteUserViewHolder> {

    // Data and context needed for displaying favorite users
    private List<User> favoriteUsers;                          // List of users marked as favorites
    private Context context;                                   // App context for accessing resources
    private ActivityResultLauncher<Intent> profileViewLauncher; // Handler for opening user profiles

    /**
     * Constructor - Creates a new adapter for displaying favorite users
     * 
     * @param context The app context for accessing resources and starting activities
     * @param favoriteUsers The list of users that have been marked as favorites
     * @param profileViewLauncher Handler for launching profile viewing activities
     */
    public FavoriteUserAdapter(Context context, List<User> favoriteUsers, ActivityResultLauncher<Intent> profileViewLauncher) {
        this.context = context;
        this.favoriteUsers = favoriteUsers;
        this.profileViewLauncher = profileViewLauncher;
    }

    @NonNull
    @Override
    public FavoriteUserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.favorite_user_item, parent, false);
        return new FavoriteUserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FavoriteUserViewHolder holder, int position) {
        User user = favoriteUsers.get(position);
        holder.bind(user);
    }

    @Override
    public int getItemCount() {
        return favoriteUsers.size();
    }

    public void updateFavoriteUsers(List<User> newFavoriteUsers) {
        this.favoriteUsers = newFavoriteUsers;
        notifyDataSetChanged();
    }

    class FavoriteUserViewHolder extends RecyclerView.ViewHolder {
        private ImageView profilePicture;
        private ImageView genderIcon;
        private TextView username;
        private TextView bio;

        public FavoriteUserViewHolder(@NonNull View itemView) {
            super(itemView);
            profilePicture = itemView.findViewById(R.id.favoriteProfilePicture);
            genderIcon = itemView.findViewById(R.id.favoriteGenderIcon);
            username = itemView.findViewById(R.id.favoriteUsername);
            bio = itemView.findViewById(R.id.favoriteBio);
        }

        public void bind(User user) {
            // Set profile picture
            int profilePictureResId = user.getProfilePictureResourceId(context);
            profilePicture.setImageResource(profilePictureResId);

            // Set gender icon
            int genderIconResId = user.getGenderIconResourceId(context);
            genderIcon.setImageResource(genderIconResId);

            // Set username
            username.setText("@" + user.getUsername());

            // Set bio (truncated for favorites view)
            String bioText = user.getBio();
            if (bioText == null || bioText.trim().isEmpty()) {
                bioText = "No bio available";
            }
            bio.setText(bioText);

            // Set click listener to navigate to user's profile
            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, UserProfileViewActivity.class);
                intent.putExtra("username", user.getUsername());
                profileViewLauncher.launch(intent);
            });
        }
    }
}
