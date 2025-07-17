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

public class GroupsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_groups);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.groupsMain), (v, insets) -> {
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
            Intent intent = new Intent(GroupsActivity.this, HomepageActivity.class);
            startActivity(intent);
        });

        navGroups.setOnClickListener(v -> {
            Toast.makeText(GroupsActivity.this, "Already on Groups", Toast.LENGTH_SHORT).show();
        });

        navDashboard.setOnClickListener(v -> {
            Intent intent = new Intent(GroupsActivity.this, DashboardActivity.class);
            startActivity(intent);
        });

        navNotifications.setOnClickListener(v -> {
            Intent intent = new Intent(GroupsActivity.this, NotificationsActivity.class);
            startActivity(intent);
        });

        navChats.setOnClickListener(v -> {
            Intent intent = new Intent(GroupsActivity.this, ChatsActivity.class);
            startActivity(intent);
        });
    }
}
