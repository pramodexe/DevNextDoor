package com.s23010234.devnextdoor;

/**
 * This class represents a chat conversation between two users.
 * It keeps track of who is chatting, what the latest message was,
 * and when it was sent. Think of it as the header information for a chat.
 */
public class Chat {
    
    // A unique ID that identifies this chat conversation
    private String chatId;
    
    // The username of the first person in this chat
    private String participant1;
    
    // The username of the second person in this chat
    private String participant2;
    
    // The text of the most recent message sent in this chat
    private String lastMessage;
    
    // When the most recent message was sent (as a timestamp number)
    private long lastMessageTimestamp;
    
    // Who sent the most recent message (their username)
    private String lastMessageSender;

    /**
     * Empty constructor that Firebase needs to create Chat objects.
     * Firebase uses this when loading chat data from the database.
     */
    public Chat() {
    }

    /**
     * Constructor to create a new Chat with all information.
     * This is used when setting up a new chat conversation.
     */
    public Chat(String chatId, String participant1, String participant2, String lastMessage, 
                long lastMessageTimestamp, String lastMessageSender) {
        this.chatId = chatId;
        this.participant1 = participant1;
        this.participant2 = participant2;
        this.lastMessage = lastMessage;
        this.lastMessageTimestamp = lastMessageTimestamp;
        this.lastMessageSender = lastMessageSender;
    }

    // These are getter methods - they let other parts of the app read the chat information

    /**
     * Get the chat's unique ID.
     * Returns the ID that identifies this specific chat conversation.
     */
    public String getChatId() {
        return chatId;
    }

    /**
     * Get the first participant's username.
     * Returns the username of the first person in this chat.
     */
    public String getParticipant1() {
        return participant1;
    }

    /**
     * Get the second participant's username.
     * Returns the username of the second person in this chat.
     */
    public String getParticipant2() {
        return participant2;
    }

    /**
     * Get the most recent message.
     * Returns the text of the last message sent in this chat.
     */
    public String getLastMessage() {
        return lastMessage;
    }

    /**
     * Get when the last message was sent.
     * Returns the timestamp of the most recent message.
     */
    public long getLastMessageTimestamp() {
        return lastMessageTimestamp;
    }

    /**
     * Get who sent the last message.
     * Returns the username of whoever sent the most recent message.
     */
    public String getLastMessageSender() {
        return lastMessageSender;
    }

    // These are setter methods - they let other parts of the app change the chat information

    /**
     * Set the chat's unique ID.
     * Changes the ID that identifies this specific chat conversation.
     */
    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    /**
     * Set the first participant's username.
     * Changes the username of the first person in this chat.
     */
    public void setParticipant1(String participant1) {
        this.participant1 = participant1;
    }

    /**
     * Set the second participant's username.
     * Changes the username of the second person in this chat.
     */
    public void setParticipant2(String participant2) {
        this.participant2 = participant2;
    }

    /**
     * Set the most recent message.
     * Changes the text of the last message sent in this chat.
     */
    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    /**
     * Set when the last message was sent.
     * Changes the timestamp of the most recent message.
     */
    public void setLastMessageTimestamp(long lastMessageTimestamp) {
        this.lastMessageTimestamp = lastMessageTimestamp;
    }

    /**
     * Set who sent the last message.
     * Changes the username of whoever sent the most recent message.
     */
    public void setLastMessageSender(String lastMessageSender) {
        this.lastMessageSender = lastMessageSender;
    }

    /**
     * Find out who the other person in this chat is.
     * If you give it your username, it returns the other person's username.
     * This is useful for showing "Chat with [other person's name]".
     */
    public String getOtherParticipant(String currentUser) {
        // If the current user is participant1, return participant2
        if (currentUser.equals(participant1)) {
            return participant2;
        } 
        // If the current user is participant2, return participant1
        else if (currentUser.equals(participant2)) {
            return participant1;
        }
        // If current user is neither participant, something went wrong
        return null;
    }

    /**
     * Create a unique chat ID from two usernames.
     * This method ensures that the same two people always get the same chat ID,
     * no matter which order their names are given.
     */
    public static String generateChatId(String user1, String user2) {
        // Always put usernames in alphabetical order to ensure consistent chat IDs
        // This way "alice" and "bob" always creates "alice_bob", never "bob_alice"
        if (user1.compareTo(user2) < 0) {
            return user1 + "_" + user2;
        } else {
            return user2 + "_" + user1;
        }
    }
}
