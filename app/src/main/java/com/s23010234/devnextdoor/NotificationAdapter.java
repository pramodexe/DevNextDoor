package com.s23010234.devnextdoor;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

/**
 * Notification Adapter - Manages the display of notifications in a list
 * 
 * This adapter is responsible for showing notifications to users in a
 * scrollable list. Think of it like a notification center manager that
 * displays each notification with appropriate styling and icons.
 * 
 * Each notification displays:
 * - An icon that represents the type of notification
 * - The notification title
 * - The notification message
 * - When the notification was received
 * - Visual styling that shows if it's been read or not
 * 
 * Users can tap on notifications to mark them as read, and they can
 * delete notifications they don't want anymore. The adapter handles
 * all the visual presentation and user interactions.
 */
public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {
    
    // Data and context needed for displaying notifications
    private Context context;                    // App context for accessing resources
    private List<Notification> notifications;  // List of notifications to display
    private OnNotificationClickListener listener; // Handler for notification interactions

    /**
     * Notification Click Listener Interface
     * 
     * This interface defines what happens when users interact with notifications.
     * Other parts of the app can implement this to respond to notification events.
     */
    public interface OnNotificationClickListener {
        void onNotificationClick(Notification notification);  // When user taps a notification
        void onNotificationDelete(Notification notification); // When user deletes a notification
    }

    /**
     * Constructor - Creates a new adapter for displaying notifications
     * 
     * @param context The app context for accessing resources and themes
     * @param notifications The list of notifications to display
     */
    public NotificationAdapter(Context context, List<Notification> notifications) {
        this.context = context;
        this.notifications = notifications;
    }

    /**
     * Set Notification Click Listener - Register for notification interaction events
     * 
     * @param listener Object that will handle notification clicks and deletions
     */
    public void setOnNotificationClickListener(OnNotificationClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification notification = notifications.get(position);
        
        holder.titleTextView.setText(notification.getTitle());
        holder.messageTextView.setText(notification.getMessage());
        holder.timeTextView.setText(notification.getTimeAgo());

        // Apply custom styling for notification message text in dark mode
        applyNotificationStyling(holder);

        // Set notification icon based on type
        int iconResource = getNotificationIcon(notification.getType());
        holder.iconImageView.setImageResource(iconResource);

        // Set read/unread appearance
        if (notification.isRead()) {
            holder.itemView.setAlpha(0.7f);
            holder.unreadIndicator.setVisibility(View.GONE);
        } else {
            holder.itemView.setAlpha(1.0f);
            holder.unreadIndicator.setVisibility(View.VISIBLE);
        }

        // Set click listeners
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onNotificationClick(notification);
            }
        });

        holder.deleteButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onNotificationDelete(notification);
            }
        });
    }
    
    private void applyNotificationStyling(NotificationViewHolder holder) {
        // Check if dark mode is enabled
        SharedPreferences sharedPreferences = context.getSharedPreferences("DevNextDoorPrefs", Context.MODE_PRIVATE);
        boolean isDarkModeEnabled = sharedPreferences.getBoolean("isDarkModeEnabled", false);
        
        if (isDarkModeEnabled) {
            // Apply the custom color defined in colors.xml for dark mode
            int color = ContextCompat.getColor(context, R.color.notification_body_text);
            holder.messageTextView.setTextColor(color);
            holder.titleTextView.setTextColor(color);
            
            // Ensure title is bold in dark mode
            holder.titleTextView.setTypeface(holder.titleTextView.getTypeface(), Typeface.BOLD);
        }
        // Note: In light mode, the colors are already set correctly via the layout XML
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    private int getNotificationIcon(String type) {
        switch (type) {
            case Notification.Types.PROFILE_CREATED:
                return R.drawable.ic_person_add; // You'll need to add this icon
            case Notification.Types.PASSWORD_CHANGED:
                return R.drawable.ic_lock; // You'll need to add this icon
            case Notification.Types.USERNAME_CHANGED:
                return R.drawable.ic_edit; // You'll need to add this icon
            default:
                return R.drawable.ic_notifications; // Default notification icon
        }
    }

    public void updateNotifications(List<Notification> newNotifications) {
        this.notifications = newNotifications;
        notifyDataSetChanged();
    }

    public void removeNotification(int position) {
        // Don't remove from list here as it's already removed in the activity
        // Just notify the adapter about the item removal
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, notifications.size());
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        ImageView iconImageView;
        TextView titleTextView;
        TextView messageTextView;
        TextView timeTextView;
        View unreadIndicator;
        ImageView deleteButton;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            iconImageView = itemView.findViewById(R.id.notification_icon);
            titleTextView = itemView.findViewById(R.id.notification_title);
            messageTextView = itemView.findViewById(R.id.notification_message);
            timeTextView = itemView.findViewById(R.id.notification_time);
            unreadIndicator = itemView.findViewById(R.id.unread_indicator);
            deleteButton = itemView.findViewById(R.id.delete_notification);
        }
    }
}
