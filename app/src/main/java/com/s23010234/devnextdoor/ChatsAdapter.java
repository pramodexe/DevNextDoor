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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Chats Adapter - This class manages the chat list display
 * 
 * Think of this like the manager of your messaging app's conversation list.
 * Just like how WhatsApp or iMessage shows you a list of all your chats,
 * this adapter creates and manages that list. For each chat, it shows:
 * - The other person's profile picture
 * - Their username
 * - The last message that was sent
 * - When that message was sent
 * 
 * This adapter is smart and caches (remembers) profile information so it
 * doesn't have to ask Firebase for the same data over and over again.
 */
public class ChatsAdapter extends RecyclerView.Adapter<ChatsAdapter.ChatViewHolder> {

    // The list of all chat conversations that will be shown to the user
    private List<Chat> chatList;
    
    // Reference to the app context - this gives us access to resources and activities
    private Context context;
    
    // The username of the person currently using the app
    private String currentUsername;
    
    // A memory cache that stores profile information we've already loaded
    // This makes the app faster by avoiding repeated Firebase requests
    private static Map<String, ProfileData> profileCache = new HashMap<>();
    
    /**
     * Profile Data Container - This inner class stores basic profile info in our cache
     * 
     * Think of this like a small notecard that holds just the essential information
     * we need about each user for the chat list. We store this to avoid asking
     * Firebase for the same information repeatedly.
     */
    private static class ProfileData {
        // Whether the user is Male or Female (used to pick default profile picture)
        String gender;
        
        // The filename of the user's chosen profile picture
        String profilePictureFilename;
        
        /**
         * Constructor - Creates a new profile data entry for our cache
         * 
         * @param gender The user's gender (Male or Female)
         * @param profilePictureFilename The filename of their profile picture
         */
        ProfileData(String gender, String profilePictureFilename) {
            this.gender = gender;
            this.profilePictureFilename = profilePictureFilename;
        }
    }

    /**
     * Constructor - Creates a new chat adapter
     * 
     * This sets up everything needed to display the list of chat conversations.
     * It's like setting up a new display manager for your messaging app.
     * 
     * @param context The app context (gives access to resources and activities)
     * @param chatList The list of chat conversations to display
     * @param currentUsername The username of the person using the app
     */
    public ChatsAdapter(Context context, List<Chat> chatList, String currentUsername) {
        this.context = context;
        this.chatList = chatList;
        this.currentUsername = currentUsername;
    }

    /**
     * Create View Holder - Creates a new view holder for a chat item
     * 
     * This method runs when the RecyclerView needs to create a new chat row.
     * Think of it like creating a new row template that will be filled with
     * specific chat information later. It loads the XML layout for one chat item.
     * 
     * @param parent The parent view group
     * @param viewType The type of view (not used here since all chats look the same)
     * @return A new ChatViewHolder that manages one chat item
     */
    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Load the XML layout file that defines how one chat item should look
        // This is like creating a blank form that will be filled with chat data
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat, parent, false);
        
        // Create and return a new view holder that will manage this chat item
        return new ChatViewHolder(view);
    }

    /**
     * Bind View Holder - Fills in the data for a specific chat item
     * 
     * This method runs for each chat that needs to be displayed on screen.
     * It takes the information from a Chat object and puts it into the
     * visual elements (profile picture, username, last message, etc.).
     * Think of it like filling out a form with specific chat details.
     * 
     * @param holder The view holder that manages the chat item's visual elements
     * @param position Which chat in the list this is (0 = first, 1 = second, etc.)
     */
    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        // Get the chat data for this specific position in our list
        Chat chat = chatList.get(position);
        
        // Tell the view holder to fill the visual elements with this chat's information
        holder.bind(chat);
    }

    /**
     * Get Item Count - Returns how many chat conversations should be displayed
     * 
     * This tells the RecyclerView how many chat items it needs to show.
     * It's like counting how many conversation rows should appear in the list.
     * 
     * @return The number of chats in our list
     */
    @Override
    public int getItemCount() {
        return chatList.size();
    }

    /**
     * Update Chat List - Updates the list of chats and refreshes the display
     * 
     * This method is called when the chat list changes (new messages arrive,
     * new chats are created, etc.). It updates our internal list and tells
     * the RecyclerView to redraw everything with the new data.
     * 
     * @param newChatList The updated list of chat conversations
     */
    public void updateChatList(List<Chat> newChatList) {
        // Replace our old chat list with the new one
        this.chatList = newChatList;
        
        // Tell the RecyclerView to redraw all the chat items with the new data
        // This is like refreshing the screen to show the latest information
        notifyDataSetChanged();
    }
    
    /**
     * Clear Profile Cache - Clears the cached profile information
     * 
     * This method empties our memory cache of profile information.
     * It can be called if we want to refresh all profile data from Firebase.
     * Think of it like clearing your browser's cache to get fresh data.
     */
    public static void clearProfileCache() {
        profileCache.clear();
    }

    /**
     * Format Timestamp - Converts a timestamp into human-readable time format
     * 
     * This method takes a raw timestamp (like 1234567890) and converts it
     * into friendly text that users can understand (like "2 hours ago").
     * It's like translating computer time into human time.
     * 
     * @param timestamp The raw timestamp from when the message was sent
     * @return A human-readable string showing when the message was sent
     */
    private String formatTimestamp(long timestamp) {
        // If there's no valid timestamp, return empty string
        if (timestamp <= 0) return "";
        
        // Get the current time and calculate how much time has passed
        long currentTime = System.currentTimeMillis();
        long timeDiff = currentTime - timestamp;
        
        // Convert the time difference to minutes, hours, and days
        long minutes = timeDiff / (1000 * 60);
        long hours = timeDiff / (1000 * 60 * 60);
        long days = timeDiff / (1000 * 60 * 60 * 24);
        
        // Return different formats based on how long ago the message was sent
        if (minutes < 1) {
            return "Just now";
        } else if (minutes < 60) {
            return minutes + " min ago";
        } else if (hours < 24) {
            return hours + " hour" + (hours > 1 ? "s" : "") + " ago";
        } else if (days < 7) {
            return days + " day" + (days > 1 ? "s" : "") + " ago";
        } else {
            // For older messages, show a simple date format (like "Jan 15")
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM dd", java.util.Locale.getDefault());
            return sdf.format(new java.util.Date(timestamp));
        }
    }

    /**
     * Chat View Holder - Manages the visual elements of one chat item
     * 
     * This inner class is responsible for managing one row in the chat list.
     * Think of it like a manager for one chat conversation's display.
     * It holds references to all the visual elements (profile picture,
     * username, last message, timestamp) and fills them with data.
     */
    class ChatViewHolder extends RecyclerView.ViewHolder {
        // Visual elements that make up one chat item
        private ImageView profilePicture;  // The other user's profile picture
        private TextView username;         // The other user's username
        private TextView lastMessage;      // The last message that was sent
        private TextView timestamp;        // When the last message was sent

        /**
         * Constructor - Sets up the view holder for one chat item
         * 
         * This finds all the visual elements in the chat item layout
         * and stores references to them so we can update them later.
         * 
         * @param itemView The view that contains all the visual elements for one chat
         */
        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            // Find and store references to all the visual elements
            profilePicture = itemView.findViewById(R.id.profilePicture);
            username = itemView.findViewById(R.id.usernameText);
            lastMessage = itemView.findViewById(R.id.lastMessageText);
            timestamp = itemView.findViewById(R.id.timestampText);
        }

        /**
         * Bind - Fills the visual elements with data from a specific chat
         * 
         * This method takes a Chat object and uses its data to fill in
         * all the visual elements (profile picture, username, etc.).
         * It's like filling out a form with specific chat information.
         * 
         * @param chat The chat conversation whose data we want to display
         */
        public void bind(Chat chat) {
            // Figure out who the other person in the chat is
            // Since each chat has two participants, we need to find the one who isn't us
            String otherUser = chat.getParticipant1().equals(currentUsername) ? 
                              chat.getParticipant2() : chat.getParticipant1();

            // Display the other user's username with an @ symbol
            username.setText("@" + otherUser);

            // Set up the last message display
            if (chat.getLastMessage() != null && !chat.getLastMessage().isEmpty()) {
                // Get information about who sent the last message
                String lastMessageSender = chat.getLastMessageSender();
                String messageText = chat.getLastMessage();
                
                if (lastMessageSender != null && !lastMessageSender.isEmpty()) {
                    // Figure out how to display the sender's name
                    String senderDisplayName;
                    if (lastMessageSender.equals(currentUsername)) {
                        // If we sent the message, show "You"
                        senderDisplayName = "You";
                    } else {
                        // If the other person sent it, show their username
                        senderDisplayName = lastMessageSender;
                    }
                    
                    // Format the message as "Sender: Message text"
                    lastMessage.setText(senderDisplayName + ": " + messageText);
                } else {
                    // If we don't know who sent it, just show the message
                    lastMessage.setText(messageText);
                }
            } else {
                // If there are no messages yet, show a placeholder
                lastMessage.setText("No messages yet");
            }

            // Set up the timestamp display
            if (chat.getLastMessageTimestamp() > 0) {
                // Convert the timestamp to a human-readable format
                timestamp.setText(formatTimestamp(chat.getLastMessageTimestamp()));
            } else {
                // If there's no timestamp, show nothing
                timestamp.setText("");
            }
            
            // Set the timestamp color to black for consistency
            timestamp.setTextColor(android.graphics.Color.BLACK);

            // Set up text styles for visual hierarchy
            username.setTypeface(null, android.graphics.Typeface.BOLD);     // Username is bold
            lastMessage.setTypeface(null, android.graphics.Typeface.NORMAL); // Message is normal
            
            // Set all text colors to black for consistency
            username.setTextColor(android.graphics.Color.BLACK);
            lastMessage.setTextColor(android.graphics.Color.BLACK);

            // Load and set the other user's profile picture
            setProfilePicture(otherUser);

            // Set up click listener to open the chat when user taps on this item
            itemView.setOnClickListener(v -> {
                // Create an intent to open the ChatActivity
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("chatId", chat.getChatId());
                intent.putExtra("otherUser", otherUser);
                context.startActivity(intent);
            });
        }

        /**
         * Set Profile Picture - Loads and sets the profile picture for a user
         * 
         * This method tries to load the user's profile picture efficiently.
         * First it checks our cache to see if we already have the information.
         * If not, it asks Firebase for the user's profile data.
         * This approach makes the app faster by avoiding repeated database calls.
         * 
         * @param username The username of the person whose profile picture we want
         */
        private void setProfilePicture(String username) {
            // First, check if we already have this user's profile data in our cache
            if (profileCache.containsKey(username)) {
                // We have it! Use the cached data instead of asking Firebase again
                ProfileData cachedData = profileCache.get(username);
                setProfilePictureFromData(cachedData.gender, cachedData.profilePictureFilename);
                return;
            }

            // We don't have it cached, so we need to get it from Firebase
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(username);
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // Get the user's gender and profile picture filename from Firebase
                        String gender = dataSnapshot.child("gender").getValue(String.class);
                        String profilePictureFilename = dataSnapshot.child("profilePicture").getValue(String.class);
                        
                        // Save this information in our cache for next time
                        if (gender != null && profilePictureFilename != null) {
                            profileCache.put(username, new ProfileData(gender, profilePictureFilename));
                        }
                        
                        // Now set the profile picture using this data
                        setProfilePictureFromData(gender, profilePictureFilename);
                    } else {
                        // User doesn't exist in the database, use default male picture
                        setProfilePictureFromData("Male", "male_1.png");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // If something went wrong with Firebase, use default male picture
                    setProfilePictureFromData("Male", "male_1.png");
                }
            });
        }

        /**
         * Set Profile Picture From Data - Actually displays the profile picture
         * 
         * This method takes the user's gender and profile picture filename
         * and sets the appropriate image in the ImageView. It handles both
         * custom profile pictures and default pictures based on gender.
         * 
         * @param gender The user's gender (Male or Female)
         * @param profilePictureFilename The filename of their chosen profile picture
         */
        private void setProfilePictureFromData(String gender, String profilePictureFilename) {
            if (profilePictureFilename != null && !profilePictureFilename.isEmpty()) {
                // User has a custom profile picture, try to load it
                
                // Remove the .png extension to get the drawable name
                String drawableName = profilePictureFilename.replace(".png", "");
                
                // Try to find this image in our app's drawable resources
                int resourceId = context.getResources().getIdentifier(drawableName, "drawable", context.getPackageName());
                
                if (resourceId != 0) {
                    // We found the image! Use it.
                    profilePicture.setImageResource(resourceId);
                } else {
                    // We couldn't find the custom image, use default based on gender
                    profilePicture.setImageResource("Male".equals(gender) ? R.drawable.male_1 : R.drawable.female_1);
                }
            } else {
                // User doesn't have a custom profile picture, use default based on gender
                profilePicture.setImageResource("Male".equals(gender) ? R.drawable.male_1 : R.drawable.female_1);
            }
        }
    }
}
