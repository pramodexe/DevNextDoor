package com.s23010234.devnextdoor;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class DashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.dashboardContent), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupNavigationClicks();
        setupDashboardBoxClicks();
    } // Added missing closing brace

    private void setupNavigationClicks() {
        LinearLayout navHomepage = findViewById(R.id.navHomepage);
        LinearLayout navGroups = findViewById(R.id.navGroups);
        LinearLayout navDashboard = findViewById(R.id.navDashboard);
        LinearLayout navNotifications = findViewById(R.id.navNotifications);
        LinearLayout navChats = findViewById(R.id.navChats);

        navHomepage.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, HomepageActivity.class);
            startActivity(intent);
        });

        navGroups.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, GroupsActivity.class);
            startActivity(intent);
        });

        navDashboard.setOnClickListener(v -> {
            Toast.makeText(DashboardActivity.this, "Already on Dashboard", Toast.LENGTH_SHORT).show();
        });

        navNotifications.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, NotificationsActivity.class);
            startActivity(intent);
        });

        navChats.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, ChatsActivity.class);
            startActivity(intent);
        });
    }

    private void setupDashboardBoxClicks() {
        findViewById(R.id.boxProfile).setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.boxSettings).setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, SettingsActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.boxFavourites).setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, FavouritesActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.boxSearch).setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, SearchActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.boxMeetFriends).setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, MeetFriendsActivity.class);
            startActivity(intent);
        });
    }

}
