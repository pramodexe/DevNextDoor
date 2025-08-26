package com.s23010234.devnextdoor;

/**
 * This class represents a single message in a chat conversation.
 * It holds all the information about one message, like who sent it,
 * what they said, when they sent it, and whether it's been read.
 * Each message is like a single bubble in a chat conversation.
 */
public class Message {
    
    // A unique ID that identifies this specific message
    private String messageId;
    
    // The ID of the chat conversation this message belongs to
    private String chatId;
    
    // The username of the person who sent this message
    private String senderId;
    
    // The username of the person who should receive this message
    private String receiverId;
    
    // The actual text content of the message (what the person wrote)
    private String content;
    
    // When this message was sent (as a timestamp number)
    private long timestamp;
    
    // Whether the receiver has read this message yet (true = read, false = unread)
    private boolean isRead;
    
    // What type of message this is (like "text" for text messages, could be "image" in future)
    private String messageType;

    /**
     * Empty constructor that Firebase needs to create Message objects.
     * Firebase uses this when loading message data from the database.
     */
    public Message() {
    }

    /**
     * Constructor to create a new Message with all information.
     * This is used when creating a message with specific type.
     */
    public Message(String messageId, String chatId, String senderId, String receiverId, 
                   String content, long timestamp, boolean isRead, String messageType) {
        this.messageId = messageId;
        this.chatId = chatId;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.content = content;
        this.timestamp = timestamp;
        this.isRead = isRead;
        this.messageType = messageType;
    }

    /**
     * Simplified constructor to create a text message.
     * This is used when creating regular text messages (the most common type).
     */
    public Message(String messageId, String chatId, String senderId, String receiverId, 
                   String content, long timestamp, boolean isRead) {
        // Call the main constructor and set message type to "text"
        this(messageId, chatId, senderId, receiverId, content, timestamp, isRead, "text");
    }

    // These are getter methods - they let other parts of the app read the message information

    /**
     * Get the message's unique ID.
     * Returns the ID that identifies this specific message.
     */
    public String getMessageId() {
        return messageId;
    }

    /**
     * Get the chat ID this message belongs to.
     * Returns which chat conversation this message is part of.
     */
    public String getChatId() {
        return chatId;
    }

    /**
     * Get who sent this message.
     * Returns the username of the person who wrote this message.
     */
    public String getSenderId() {
        return senderId;
    }

    /**
     * Get who should receive this message.
     * Returns the username of the person this message is for.
     */
    public String getReceiverId() {
        return receiverId;
    }

    /**
     * Get the message content.
     * Returns the actual text that the person wrote.
     */
    public String getContent() {
        return content;
    }

    /**
     * Get when this message was sent.
     * Returns the timestamp showing exactly when this message was created.
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Check if this message has been read.
     * Returns true if the receiver has seen this message, false if it's still unread.
     */
    public boolean isRead() {
        return isRead;
    }

    /**
     * Get the type of this message.
     * Returns what kind of message this is (like "text" for normal messages).
     */
    public String getMessageType() {
        return messageType;
    }

    // These are setter methods - they let other parts of the app change the message information

    /**
     * Set the message's unique ID.
     * Changes the ID that identifies this specific message.
     */
    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    /**
     * Set which chat this message belongs to.
     * Changes which chat conversation this message is part of.
     */
    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    /**
     * Set who sent this message.
     * Changes the username of the person who wrote this message.
     */
    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    /**
     * Set who should receive this message.
     * Changes the username of the person this message is for.
     */
    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    /**
     * Set the message content.
     * Changes the actual text that the person wrote.
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * Set when this message was sent.
     * Changes the timestamp showing when this message was created.
     */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Set whether this message has been read.
     * Changes if the receiver has seen this message (true = read, false = unread).
     */
    public void setRead(boolean read) {
        isRead = read;
    }

    /**
     * Set the type of this message.
     * Changes what kind of message this is (like "text" for normal messages).
     */
    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }
}
