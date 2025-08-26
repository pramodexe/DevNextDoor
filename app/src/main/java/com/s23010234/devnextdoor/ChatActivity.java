package com.s23010234.devnextdoor;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
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

import java.util.ArrayList;
import java.util.List;

/**
 * This is the Chat screen where two users can send messages to each other.
 * It shows all the messages in the conversation in a scrollable list.
 * Users can type new messages and send them by pressing the send button.
 * Think of it like WhatsApp or any messaging app where you chat with one person.
 */
public class ChatActivity extends AppCompatActivity {

    // Unique identifier for this specific chat conversation
    private String chatId;
    
    // Username of the person we're chatting with
    private String otherUser;
    
    // Username of the current logged-in user
    private String currentUsername;
    
    // Helper object that handles sending and receiving messages
    private ChatManager chatManager;
    
    // Adapter that displays all the messages in the chat
    private MessagesAdapter messagesAdapter;
    
    // List that holds all the messages in this conversation
    private List<Message> messageList;
    
    // Flag to prevent sending the same message multiple times by accident
    private boolean isSendingMessage = false;
    
    // Remember the last message sent to prevent duplicates
    private String lastSentMessage = "";
    
    // Remember when the last message was sent
    private long lastSentTime = 0;

    // Visual elements on the screen
    private TextView titleText;              // Shows the other person's name at the top
    private ImageView backArrow;             // Button to go back to chat list
    private RecyclerView messagesRecyclerView; // Scrollable list of messages
    private EditText messageInput;           // Text field where user types messages
    private ImageView sendButton;            // Button to send the typed message
    private LinearLayout loadingLayout;      // Shown while messages are loading

    /**
     * This method runs when the Chat screen is created and shown to the user.
     * It sets up the chat interface and loads all the existing messages.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Call the parent class method to properly create the activity
        super.onCreate(savedInstanceState);
        
        // Make the app use the full screen (edge to edge display)
        EdgeToEdge.enable(this);
        
        // Load and display the chat screen layout from the XML file
        setContentView(R.layout.activity_chat);

        // Apply the user's preferred theme (dark or light mode)
        ThemeManager.applyTheme(this);

        // Handle system bars (like status bar and navigation bar) properly
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Get information passed from the previous screen about which chat to open
        chatId = getIntent().getStringExtra("chatId");
        otherUser = getIntent().getStringExtra("otherUser");

        // Get the username of the currently logged-in user from device storage
        SharedPreferences sharedPreferences = getSharedPreferences("DevNextDoorPrefs", MODE_PRIVATE);
        currentUsername = sharedPreferences.getString("username", "");

        // Set up all the visual elements and functionality
        initializeViews();           // Find and connect to UI elements
        setupRecyclerView();         // Set up the messages list
        setupClickListeners();       // Set up button click actions
        loadMessages();              // Load and display existing messages
    }

    /**
     * This method runs when the user returns to this chat screen.
     * It makes sure all messages are up to date.
     */
    @Override
    protected void onResume() {
        super.onResume();
        ThemeManager.applyTheme(this);
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        // Stop listening for message updates to prevent memory leaks
        if (chatManager != null) {
            chatManager.stopListeningForMessages();
        }
    }

    /**
     * Initialize view components
     */
    private void initializeViews() {
        titleText = findViewById(R.id.titleText);
        backArrow = findViewById(R.id.backArrow);
        messagesRecyclerView = findViewById(R.id.messagesRecyclerView);
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);
        loadingLayout = findViewById(R.id.loadingLayout);

        chatManager = new ChatManager();
        messageList = new ArrayList<>();

        // Set title
        if (otherUser != null) {
            titleText.setText("@" + otherUser);
        }
    }

    /**
     * Setup RecyclerView for messages
     */
    private void setupRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true); // Start from bottom
        messagesRecyclerView.setLayoutManager(layoutManager);
        
        messagesAdapter = new MessagesAdapter(this, messageList, currentUsername);
        messagesRecyclerView.setAdapter(messagesAdapter);
    }

    /**
     * Setup click listeners
     */
    private void setupClickListeners() {
        backArrow.setOnClickListener(v -> finish());

        sendButton.setOnClickListener(v -> sendMessage());
        
        // Make username title clickable to view user's profile
        titleText.setOnClickListener(v -> {
            if (otherUser != null) {
                Intent intent = new Intent(ChatActivity.this, ProfileActivity.class);
                intent.putExtra("username", otherUser);
                startActivity(intent);
            }
        });

        // Also allow sending with enter key (with same duplicate prevention)
        messageInput.setOnEditorActionListener((v, actionId, event) -> {
            if (!isSendingMessage) { // Only send if not already sending
                sendMessage();
            }
            return true;
        });
    }

    /**
     * Load messages for this chat
     */
    private void loadMessages() {
        if (chatId == null) return;

        showLoading();

        chatManager.getMessagesForChat(chatId, new ChatManager.MessagesCallback() {
            @Override
            public void onSuccess(List<Message> messages) {
                runOnUiThread(() -> {
                    messageList.clear();
                    messageList.addAll(messages);
                    messagesAdapter.updateMessages(messageList);
                    
                    // Scroll to bottom
                    if (!messages.isEmpty()) {
                        messagesRecyclerView.scrollToPosition(messages.size() - 1);
                    }
                    
                    hideLoading();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(ChatActivity.this, "Error loading messages: " + error, Toast.LENGTH_SHORT).show();
                    hideLoading();
                });
            }
        });
    }

    /**
     * Send a message with duplicate prevention
     */
    private void sendMessage() {
        String content = messageInput.getText().toString().trim();
        
        if (TextUtils.isEmpty(content)) {
            return;
        }

        if (chatId == null || otherUser == null) {
            Toast.makeText(this, "Error: Chat not properly initialized", Toast.LENGTH_SHORT).show();
            return;
        }

        // Prevent duplicate sends
        if (isSendingMessage) {
            return; // Already sending a message, ignore this request
        }
        
        // Check for duplicate message within 2 seconds
        long currentTime = System.currentTimeMillis();
        if (content.equals(lastSentMessage) && (currentTime - lastSentTime) < 2000) {
            return; // Same message sent within 2 seconds, likely a duplicate
        }

        // Set sending state
        isSendingMessage = true;
        lastSentMessage = content;
        lastSentTime = currentTime;
        
        // Disable send button and show sending state
        sendButton.setEnabled(false);
        sendButton.setAlpha(0.5f); // Visual feedback
        
        chatManager.sendMessage(chatId, currentUsername, otherUser, content, new ChatManager.DatabaseCallback() {
            @Override
            public void onSuccess(boolean result) {
                runOnUiThread(() -> {
                    // Clear input and reset sending state
                    messageInput.setText("");
                    resetSendButton();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    // Don't show duplicate message errors to user - just silently ignore
                    if (!error.equals("Duplicate message detected")) {
                        Toast.makeText(ChatActivity.this, "Failed to send message: " + error, Toast.LENGTH_SHORT).show();
                    }
                    resetSendButton();
                });
            }
        });
    }
    
    /**
     * Reset send button to normal state
     */
    private void resetSendButton() {
        isSendingMessage = false;
        sendButton.setEnabled(true);
        sendButton.setAlpha(1.0f);
    }

    /**
     * Show loading state
     */
    private void showLoading() {
        loadingLayout.setVisibility(View.VISIBLE);
        messagesRecyclerView.setVisibility(View.GONE);
    }

    /**
     * Hide loading state
     */
    private void hideLoading() {
        loadingLayout.setVisibility(View.GONE);
        messagesRecyclerView.setVisibility(View.VISIBLE);
    }
}
