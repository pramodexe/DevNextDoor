package com.s23010234.devnextdoor;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Chat Manager - Handles all chat and messaging operations with Firebase
 * 
 * This class is like a messaging system manager that handles all the
 * behind-the-scenes work for chat functionality. Think of it as the
 * postal service for the app - it can:
 * - Load all chat conversations for a user
 * - Load all messages within a specific chat
 * - Send new messages
 * - Create new chat conversations
 * - Update chat information when new messages arrive
 * 
 * It connects to two main areas in Firebase:
 * - "chats" section: stores conversation information
 * - "messages" section: stores individual messages
 * 
 * This separation keeps the app fast because we can load chat summaries
 * without loading every single message until the user opens a specific chat.
 */
public class ChatManager {
    // Database connections for different types of chat data
    private DatabaseReference chatsReference;      // Connection to chat conversation data
    private DatabaseReference messagesReference;   // Connection to individual message data
    private ValueEventListener messagesListener;   // Listens for new messages in real-time

    /**
     * Constructor - Sets up connections to Firebase chat data
     * 
     * This creates a new ChatManager and establishes connections to
     * the Firebase database sections that store chat information.
     */
    public ChatManager() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        chatsReference = database.getReference("chats");
        messagesReference = database.getReference("messages");
    }

    /**
     * Callback Interfaces - Define what happens when operations complete
     * 
     * These interfaces are like contracts that define what to do when
     * chat operations succeed or fail. They allow other parts of the
     * app to respond appropriately to different outcomes.
     */
    
    /**
     * Chats Callback - For operations that return lists of chat conversations
     */
    public interface ChatsCallback {
        void onSuccess(List<Chat> chats);  // Called when chat list is successfully loaded
        void onError(String error);        // Called when something goes wrong
    }

    /**
     * Messages Callback - For operations that return lists of messages
     */
    public interface MessagesCallback {
        void onSuccess(List<Message> messages);  // Called when messages are successfully loaded
        void onError(String error);              // Called when something goes wrong
    }

    public interface DatabaseCallback {
        void onSuccess(boolean result);
        void onError(String error);
    }

    public interface ChatExistsCallback {
        void onResult(boolean exists, String chatId);
        void onError(String error);
    }

    /**
     * Get all chats for a specific user
     */
    public void getChatsForUser(String username, ChatsCallback callback) {
        chatsReference.orderByChild("lastMessageTimestamp")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        List<Chat> userChats = new ArrayList<>();
                        
                        for (DataSnapshot chatSnapshot : dataSnapshot.getChildren()) {
                            Chat chat = chatSnapshot.getValue(Chat.class);
                            if (chat != null && (username.equals(chat.getParticipant1()) || 
                                               username.equals(chat.getParticipant2()))) {
                                chat.setChatId(chatSnapshot.getKey());
                                userChats.add(chat);
                            }
                        }
                        
                        // Sort by timestamp (newest first)
                        Collections.sort(userChats, new Comparator<Chat>() {
                            @Override
                            public int compare(Chat c1, Chat c2) {
                                return Long.compare(c2.getLastMessageTimestamp(), c1.getLastMessageTimestamp());
                            }
                        });
                        
                        callback.onSuccess(userChats);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        callback.onError(databaseError.getMessage());
                    }
                });
    }

    /**
     * Create a new chat between two users
     */
    public void createChat(String user1, String user2, DatabaseCallback callback) {
        String chatId = Chat.generateChatId(user1, user2);
        
        // Check if chat already exists
        chatsReference.child(chatId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    // Create new chat
                    Chat newChat = new Chat(chatId, user1, user2, "", System.currentTimeMillis(), "");
                    
                    chatsReference.child(chatId).setValue(newChat)
                            .addOnSuccessListener(aVoid -> callback.onSuccess(true))
                            .addOnFailureListener(e -> callback.onError(e.getMessage()));
                } else {
                    callback.onSuccess(true); // Chat already exists
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onError(databaseError.getMessage());
            }
        });
    }

    /**
     * Check if a chat exists between two users
     */
    public void checkChatExists(String user1, String user2, ChatExistsCallback callback) {
        String chatId = Chat.generateChatId(user1, user2);
        
        chatsReference.child(chatId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                callback.onResult(dataSnapshot.exists(), chatId);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onError(databaseError.getMessage());
            }
        });
    }

    /**
     * Send a message in a chat with duplicate prevention
     */
    public void sendMessage(String chatId, String senderId, String receiverId, String content, DatabaseCallback callback) {
        // Generate unique message ID with timestamp to ensure uniqueness
        long timestamp = System.currentTimeMillis();
        String messageId = messagesReference.child(chatId).push().getKey();
        
        if (messageId != null) {
            // Add timestamp to message ID to make it even more unique
            String uniqueMessageId = messageId + "_" + timestamp;
            
            Message message = new Message(uniqueMessageId, chatId, senderId, receiverId, content, timestamp, false);
            
            // Check if this exact message was already sent recently (within 3 seconds)
            Query recentMessagesQuery = messagesReference.child(chatId)
                .orderByChild("timestamp")
                .startAt(timestamp - 3000); // Check last 3 seconds
            
            recentMessagesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    boolean isDuplicate = false;
                    
                    // Check for duplicate content from same sender
                    for (DataSnapshot messageSnapshot : dataSnapshot.getChildren()) {
                        Message existingMessage = messageSnapshot.getValue(Message.class);
                        if (existingMessage != null && 
                            existingMessage.getSenderId().equals(senderId) &&
                            existingMessage.getContent().equals(content) &&
                            Math.abs(existingMessage.getTimestamp() - timestamp) < 2000) {
                            isDuplicate = true;
                            break;
                        }
                    }
                    
                    if (isDuplicate) {
                        callback.onError("Duplicate message detected");
                        return;
                    }
                    
                    // Save message if not duplicate
                    messagesReference.child(chatId).child(uniqueMessageId).setValue(message)
                            .addOnSuccessListener(aVoid -> {
                                // Update chat with last message info
                                updateChatLastMessage(chatId, content, timestamp, senderId, callback);
                            })
                            .addOnFailureListener(e -> callback.onError(e.getMessage()));
                }
                
                @Override
                public void onCancelled(DatabaseError databaseError) {
                    callback.onError(databaseError.getMessage());
                }
            });
        } else {
            callback.onError("Failed to generate message ID");
        }
    }

    /**
     * Update chat with last message information
     */
    private void updateChatLastMessage(String chatId, String lastMessage, long timestamp, String senderId, DatabaseCallback callback) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("lastMessage", lastMessage);
        updates.put("lastMessageTimestamp", timestamp);
        updates.put("lastMessageSender", senderId);
        
        // Mark as unread for the receiver
        chatsReference.child(chatId).updateChildren(updates)
                .addOnSuccessListener(aVoid -> callback.onSuccess(true))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    /**
     * Get messages for a specific chat with real-time updates
     */
    public void getMessagesForChat(String chatId, MessagesCallback callback) {
        // Remove any existing listener first
        if (messagesListener != null) {
            messagesReference.removeEventListener(messagesListener);
        }
        
        messagesListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Message> messages = new ArrayList<>();
                
                for (DataSnapshot messageSnapshot : dataSnapshot.getChildren()) {
                    Message message = messageSnapshot.getValue(Message.class);
                    if (message != null) {
                        message.setMessageId(messageSnapshot.getKey());
                        messages.add(message);
                    }
                }
                
                callback.onSuccess(messages);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onError(databaseError.getMessage());
            }
        };
        
        messagesReference.child(chatId).orderByChild("timestamp")
                .addValueEventListener(messagesListener);
    }
    
    /**
     * Stop listening for message updates
     */
    public void stopListeningForMessages() {
        if (messagesListener != null) {
            messagesReference.removeEventListener(messagesListener);
            messagesListener = null;
        }
    }
}
