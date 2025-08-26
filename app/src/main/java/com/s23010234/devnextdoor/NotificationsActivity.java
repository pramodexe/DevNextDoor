package com.s23010234.devnextdoor;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Notifications Activity - The notifications center screen
 * 
 * This activity displays all notifications that the user has received.
 * Think of it like your phone's notification center where you can see
 * all the alerts, messages, and updates from apps.
 * 
 * Users can:
 * - View all their notifications in chronological order
 * - See which notifications are new (unread) vs already seen (read)
 * - Tap on notifications to mark them as read
 * - Delete notifications they don't want anymore
 * - Pull down to refresh and check for new notifications
 * - Navigate to other parts of the app using bottom navigation
 * 
 * The activity extends ShakeBaseActivity so users can shake their phone
 * for quick actions. It also implements the notification click listener
 * to handle user interactions with individual notifications.
 */
public class NotificationsActivity extends ShakeBaseActivity implements NotificationAdapter.OnNotificationClickListener {

    // Visual elements for displaying notifications
    private RecyclerView notificationsRecyclerView;    // Scrollable list of notifications
    private NotificationAdapter notificationAdapter;   // Manages displaying each notification
    private SwipeRefreshLayout swipeRefreshLayout;      // Allows pull-to-refresh functionality
    private TextView emptyStateTextView;               // Shown when there are no notifications
    
    // Data management objects
    private NotificationManager notificationManager;   // Handles loading/managing notifications from Firebase
    private String currentUsername;                    // Username of the person using the app
    private List<Notification> notifications;          // List of all notifications for this user

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_notifications);

        // Apply current theme
        ThemeManager.applyTheme(this);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize shake detection
        initializeShakeDetection();

        initializeComponents();
        setupNavigationClicks();
        loadNotifications();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Reapply theme when returning to this activity (e.g., from Settings)
        ThemeManager.applyTheme(this);
        // Refresh notifications when returning to this activity
        loadNotifications();
    }

    private void initializeComponents() {
        // Get current username from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("DevNextDoorPrefs", MODE_PRIVATE);
        currentUsername = sharedPreferences.getString("username", "");

        // Initialize notification manager
        notificationManager = new NotificationManager();
        notifications = new ArrayList<>();

        // Initialize views
        notificationsRecyclerView = findViewById(R.id.notifications_recycler_view);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        emptyStateTextView = findViewById(R.id.empty_state_text);

        // Setup RecyclerView
        notificationAdapter = new NotificationAdapter(this, notifications);
        notificationAdapter.setOnNotificationClickListener(this);
        notificationsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        notificationsRecyclerView.setAdapter(notificationAdapter);

        // Setup swipe to refresh
        swipeRefreshLayout.setOnRefreshListener(this::loadNotifications);
        swipeRefreshLayout.setColorSchemeResources(R.color.primary_blue);
    }

    private void loadNotifications() {
        if (currentUsername.isEmpty()) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            swipeRefreshLayout.setRefreshing(false);
            return;
        }

        swipeRefreshLayout.setRefreshing(true);

        notificationManager.getUserNotifications(currentUsername, new NotificationManager.NotificationsListCallback() {
            @Override
            public void onSuccess(List<Notification> loadedNotifications) {
                notifications.clear();
                notifications.addAll(loadedNotifications);
                
                runOnUiThread(() -> {
                    notificationAdapter.updateNotifications(notifications);
                    updateEmptyState();
                    swipeRefreshLayout.setRefreshing(false);
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(NotificationsActivity.this, "Error loading notifications: " + error, Toast.LENGTH_SHORT).show();
                    swipeRefreshLayout.setRefreshing(false);
                    updateEmptyState();
                });
            }
        });
    }

    private void updateEmptyState() {
        if (notifications.isEmpty()) {
            notificationsRecyclerView.setVisibility(View.GONE);
            emptyStateTextView.setVisibility(View.VISIBLE);
            emptyStateTextView.setText("No notifications yet.\nWe'll notify you when something important happens!");
        } else {
            notificationsRecyclerView.setVisibility(View.VISIBLE);
            emptyStateTextView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onNotificationClick(Notification notification) {
        // Mark notification as read if it's unread
        if (!notification.isRead()) {
            notificationManager.markNotificationAsRead(currentUsername, notification.getId(), new NotificationManager.NotificationCallback() {
                @Override
                public void onSuccess(boolean result) {
                    if (result) {
                        // Update local notification
                        notification.setRead(true);
                        runOnUiThread(() -> notificationAdapter.notifyDataSetChanged());
                    }
                }

                @Override
                public void onError(String error) {
                    // Don't show error to user for marking as read
                }
            });
        }

        // Show detailed notification dialog or perform action based on notification type
        showNotificationDetails(notification);
    }

    @Override
    public void onNotificationDelete(Notification notification) {
        notificationManager.deleteNotification(currentUsername, notification.getId(), new NotificationManager.NotificationCallback() {
            @Override
            public void onSuccess(boolean result) {
                if (result) {
                    runOnUiThread(() -> {
                        int position = notifications.indexOf(notification);
                        if (position != -1 && position < notifications.size()) {
                            notifications.remove(position);
                            notificationAdapter.removeNotification(position);
                            updateEmptyState();
                            Toast.makeText(NotificationsActivity.this, "Notification deleted", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(NotificationsActivity.this, "Error deleting notification", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void showNotificationDetails(Notification notification) {
        // Create a simple dialog to show notification details
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(notification.getTitle())
                .setMessage(notification.getMessage() + "\n\n" + notification.getFormattedDate())
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void setupNavigationClicks() {
        LinearLayout navHomepage = findViewById(R.id.navHomepage);
        LinearLayout navSearch = findViewById(R.id.navSearch);
        LinearLayout navDashboard = findViewById(R.id.navDashboard);
        LinearLayout navNotifications = findViewById(R.id.navNotifications);
        LinearLayout navChats = findViewById(R.id.navChats);

        navHomepage.setOnClickListener(v -> {
            Intent intent = new Intent(NotificationsActivity.this, HomepageActivity.class);
            startActivity(intent);
        });

        navSearch.setOnClickListener(v -> {
            Intent intent = new Intent(NotificationsActivity.this, SearchActivity.class);
            startActivity(intent);
        });

        navDashboard.setOnClickListener(v -> {
            Intent intent = new Intent(NotificationsActivity.this, DashboardActivity.class);
            startActivity(intent);
        });

        navNotifications.setOnClickListener(v -> {
            Toast.makeText(NotificationsActivity.this, "Already on Notifications", Toast.LENGTH_SHORT).show();
        });

        navChats.setOnClickListener(v -> {
            Intent intent = new Intent(NotificationsActivity.this, ChatsActivity.class);
            startActivity(intent);
        });
    }
}
