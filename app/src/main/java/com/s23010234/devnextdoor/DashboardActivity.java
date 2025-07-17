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
}
