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

public class NotificationsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_notifications);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupNavigationClicks();
    }

    private void setupNavigationClicks() {
        LinearLayout navHomepage = findViewById(R.id.navHomepage);
        LinearLayout navGroups = findViewById(R.id.navGroups);
        LinearLayout navDashboard = findViewById(R.id.navDashboard);
        LinearLayout navNotifications = findViewById(R.id.navNotifications);
        LinearLayout navChats = findViewById(R.id.navChats);

        navHomepage.setOnClickListener(v -> {
            Intent intent = new Intent(NotificationsActivity.this, HomepageActivity.class);
            startActivity(intent);
        });

        navGroups.setOnClickListener(v -> {
            Intent intent = new Intent(NotificationsActivity.this, GroupsActivity.class);
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
