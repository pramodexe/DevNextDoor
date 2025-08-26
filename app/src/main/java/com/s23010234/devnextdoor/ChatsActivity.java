package com.s23010234.devnextdoor;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * Chats Activity - The chat conversations list screen
 * 
 * This activity shows all the chat conversations that the current user
 * is involved in. Think of it like the main chat screen in WhatsApp
 * where you see all your different conversations with different people.
 * 
 * Users can:
 * - See all their chat conversations
 * - View the last message from each conversation
 * - See when the last message was sent
 * - Tap on any conversation to open it and continue chatting
 * - Navigate to other parts of the app using the bottom navigation
 * 
 * The screen extends ShakeBaseActivity, so users can shake their phone
 * for quick actions. It also loads conversations from Firebase and
 * displays them in a scrollable list.
 */
public class ChatsActivity extends ShakeBaseActivity {

    // Visual elements for displaying chat conversations
    private RecyclerView chatsRecyclerView;    // The scrollable list of chat conversations
    private ChatsAdapter chatsAdapter;         // Manages displaying each conversation in the list
    private LinearLayout loadingLayout;        // Shown while loading conversations from Firebase
    private LinearLayout emptyStateLayout;     // Shown when user has no chat conversations yet
    
    // Data management objects
    private ChatManager chatManager;           // Helper for loading chat data from Firebase
    private String currentUsername;            // Username of the person using the app
    private List<Chat> chatList;              // List of all chat conversations for this user

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chats);

        // Apply current theme
        ThemeManager.applyTheme(this);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Get current username
        SharedPreferences sharedPreferences = getSharedPreferences("DevNextDoorPrefs", MODE_PRIVATE);
        currentUsername = sharedPreferences.getString("username", "");

        // Initialize shake detection
        initializeShakeDetection();

        // Initialize views and managers
        initializeViews();
        setupRecyclerView();
        setupNavigationClicks();
        loadChats();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Reapply theme when returning to this activity (e.g., from Settings)
        ThemeManager.applyTheme(this);
        // Reload chats to get latest updates
        android.util.Log.d("ChatsActivity", "onResume - reloading chats");
        loadChats();
    }

    /**
     * Initialize view components
     */
    private void initializeViews() {
        chatsRecyclerView = findViewById(R.id.chatsRecyclerView);
        loadingLayout = findViewById(R.id.loadingLayout);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        
        chatManager = new ChatManager();
        chatList = new ArrayList<>();
    }

    /**
     * Setup RecyclerView with adapter and layout manager
     */
    private void setupRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        chatsRecyclerView.setLayoutManager(layoutManager);
        
        chatsAdapter = new ChatsAdapter(this, chatList, currentUsername);
        chatsRecyclerView.setAdapter(chatsAdapter);
    }

    /**
     * Load chats for current user from Firebase
     */
    private void loadChats() {
        if (currentUsername.isEmpty()) {
            showEmptyState();
            return;
        }

        showLoading();

        chatManager.getChatsForUser(currentUsername, new ChatManager.ChatsCallback() {
            @Override
            public void onSuccess(List<Chat> chats) {
                android.util.Log.d("ChatsActivity", "Loaded " + chats.size() + " chats for user: " + currentUsername);
                
                chatList.clear();
                chatList.addAll(chats);
                
                if (chats.isEmpty()) {
                    showEmptyState();
                } else {
                    showChatList();
                }
                
                chatsAdapter.updateChatList(chatList);
                
                // Force adapter to refresh all views to update badge states
                runOnUiThread(() -> {
                    chatsAdapter.notifyDataSetChanged();
                    android.util.Log.d("ChatsActivity", "Chat list updated and adapter refreshed");
                });
            }

            @Override
            public void onError(String error) {
                android.util.Log.e("ChatsActivity", "Error loading chats: " + error);
                Toast.makeText(ChatsActivity.this, "Error loading chats: " + error, Toast.LENGTH_SHORT).show();
                showEmptyState();
            }
        });
    }

    /**
     * Show loading state
     */
    private void showLoading() {
        loadingLayout.setVisibility(View.VISIBLE);
        chatsRecyclerView.setVisibility(View.GONE);
        emptyStateLayout.setVisibility(View.GONE);
    }

    /**
     * Show chat list
     */
    private void showChatList() {
        loadingLayout.setVisibility(View.GONE);
        chatsRecyclerView.setVisibility(View.VISIBLE);
        emptyStateLayout.setVisibility(View.GONE);
    }

    /**
     * Show empty state
     */
    private void showEmptyState() {
        loadingLayout.setVisibility(View.GONE);
        chatsRecyclerView.setVisibility(View.GONE);
        emptyStateLayout.setVisibility(View.VISIBLE);
    }

    private void setupNavigationClicks() {
        LinearLayout navHomepage = findViewById(R.id.navHomepage);
        LinearLayout navSearch = findViewById(R.id.navSearch);
        LinearLayout navDashboard = findViewById(R.id.navDashboard);
        LinearLayout navNotifications = findViewById(R.id.navNotifications);
        LinearLayout navChats = findViewById(R.id.navChats);

        navHomepage.setOnClickListener(v -> {
            Intent intent = new Intent(ChatsActivity.this, HomepageActivity.class);
            startActivity(intent);
        });

        navSearch.setOnClickListener(v -> {
            Intent intent = new Intent(ChatsActivity.this, SearchActivity.class);
            startActivity(intent);
        });

        navDashboard.setOnClickListener(v -> {
            Intent intent = new Intent(ChatsActivity.this, DashboardActivity.class);
            startActivity(intent);
        });

        navNotifications.setOnClickListener(v -> {
            Intent intent = new Intent(ChatsActivity.this, NotificationsActivity.class);
            startActivity(intent);
        });

        navChats.setOnClickListener(v -> {
            Toast.makeText(ChatsActivity.this, "Already on Chats", Toast.LENGTH_SHORT).show();
        });
    }
}
