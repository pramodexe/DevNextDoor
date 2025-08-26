package com.s23010234.devnextdoor;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * This class manages how messages are displayed in a chat conversation.
 * It shows messages in two different styles: messages you sent appear on the right,
 * and messages you received appear on the left.
 * Think of it like the bubbles in a text messaging app.
 */
public class MessagesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // Constants to identify different types of message layouts
    private static final int VIEW_TYPE_SENT = 1;      // Messages sent by current user
    private static final int VIEW_TYPE_RECEIVED = 2;  // Messages received from other user

    // Reference to the app context (used for accessing resources)
    private Context context;
    
    // List of all messages in this chat conversation
    private List<Message> messageList;
    
    // Username of the current user (to determine which messages they sent)
    private String currentUsername;

    /**
     * Creates a new adapter that will manage the display of chat messages.
     * This sets up everything needed to show messages in the chat.
     */
    public MessagesAdapter(Context context, List<Message> messageList, String currentUsername) {
        this.context = context;
        this.messageList = messageList;
        this.currentUsername = currentUsername;
    }

    /**
     * Determines what type of layout to use for each message.
     * Messages sent by the current user get a different style than received messages.
     */
    @Override
    public int getItemViewType(int position) {
        // Get the message at this position
        Message message = messageList.get(position);
        
        // Check if this message was sent by the current user
        if (message.getSenderId().equals(currentUsername)) {
            return VIEW_TYPE_SENT;     // Use "sent" layout (appears on right side)
        } else {
            return VIEW_TYPE_RECEIVED; // Use "received" layout (appears on left side)
        }
    }

    /**
     * Creates a new view holder for a message.
     * This method chooses the right layout based on whether the message was sent or received.
     */
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SENT) {
            // Create layout for messages sent by current user (right-aligned)
            View view = LayoutInflater.from(context).inflate(R.layout.item_message_sent, parent, false);
            return new SentMessageViewHolder(view);
        } else {
            // Create layout for messages received from other user (left-aligned)
            View view = LayoutInflater.from(context).inflate(R.layout.item_message_received, parent, false);
            return new ReceivedMessageViewHolder(view);
        }
    }

    /**
     * Fills in the data for a specific message.
     * This method puts the message text and timestamp into the message bubble.
     */
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        // Get the message data for this position
        Message message = messageList.get(position);
        
        // Fill the message bubble with data based on its type
        if (holder instanceof SentMessageViewHolder) {
            // This is a message sent by the current user
            ((SentMessageViewHolder) holder).bind(message);
        } else if (holder instanceof ReceivedMessageViewHolder) {
            // This is a message received from the other user
            ((ReceivedMessageViewHolder) holder).bind(message);
        }
    }

    /**
     * Returns how many messages should be displayed.
     * This tells the RecyclerView how many message bubbles to create.
     */
    @Override
    public int getItemCount() {
        return messageList.size();
    }

    /**
     * Updates the list of messages and refreshes the display.
     * This is used when new messages arrive or when loading chat history.
     */
    public void updateMessages(List<Message> newMessages) {
        this.messageList = newMessages;
        
        // Tell the RecyclerView to redraw all messages with the new data
        notifyDataSetChanged();
    }

    /**
     * Format timestamp to display date and time for all messages
     */
    private String formatTimestamp(long timestamp) {
        Date messageDate = new Date(timestamp);
        Date today = new Date();
        
        // Check if message is from today
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        boolean isToday = dateFormat.format(messageDate).equals(dateFormat.format(today));
        
        if (isToday) {
            // Show "Today" with time for today's messages
            SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
            return "Today, " + timeFormat.format(messageDate);
        } else {
            // Show date and time for older messages
            SimpleDateFormat fullFormat = new SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault());
            return fullFormat.format(messageDate);
        }
    }

    /**
     * ViewHolder for sent messages
     */
    class SentMessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;
        TextView timestampText;

        public SentMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.messageText);
            timestampText = itemView.findViewById(R.id.timestampText);
        }

        public void bind(Message message) {
            messageText.setText(message.getContent());
            timestampText.setText(formatTimestamp(message.getTimestamp()));
        }
    }

    /**
     * ViewHolder for received messages
     */
    class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;
        TextView timestampText;

        public ReceivedMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.messageText);
            timestampText = itemView.findViewById(R.id.timestampText);
        }

        public void bind(Message message) {
            messageText.setText(message.getContent());
            timestampText.setText(formatTimestamp(message.getTimestamp()));
        }
    }
}
