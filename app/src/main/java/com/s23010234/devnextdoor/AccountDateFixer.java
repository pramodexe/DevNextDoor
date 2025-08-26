package com.s23010234.devnextdoor;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;
import java.util.Map;
import java.util.Calendar;

/**
 * Utility class to fix accountCreationDate for existing users
 */
public class AccountDateFixer {
    
    private DatabaseReference databaseReference;
    
    public interface FixCallback {
        void onComplete(String message);
        void onError(String error);
    }
    
    public AccountDateFixer() {
        databaseReference = FirebaseDatabase.getInstance().getReference("users");
    }
    
    /**
     * Set proper account creation dates AND profile timestamps for existing users
     * amila: NEW (5 days ago)
     * banadara: NEW (3 days ago)
     * nipun: NOT NEW (30 days ago)
     */
    public void fixAccountCreationDates(FixCallback callback) {
        // Set amila's account creation date to 5 days ago (within last 7 days)
        Calendar amilaDate = Calendar.getInstance();
        amilaDate.add(Calendar.DAY_OF_YEAR, -5); // 5 days ago
        long amilaTimestamp = amilaDate.getTimeInMillis();
        
        // Set banadara's account creation date to 3 days ago (within last 7 days)  
        Calendar banadaraDate = Calendar.getInstance();
        banadaraDate.add(Calendar.DAY_OF_YEAR, -3); // 3 days ago
        long banadaraTimestamp = banadaraDate.getTimeInMillis();
        
        // Set nipun's account creation date to 30 days ago (older than 7 days)
        Calendar nipunDate = Calendar.getInstance();
        nipunDate.add(Calendar.DAY_OF_YEAR, -30); // 30 days ago
        long nipunTimestamp = nipunDate.getTimeInMillis();
        
    // Update amila (accountCreationDate + timestamp)
    java.util.Map<String, Object> amilaUpdates = new java.util.HashMap<>();
    amilaUpdates.put("accountCreationDate", amilaTimestamp);
    amilaUpdates.put("timestamp", amilaTimestamp);
    databaseReference.child("amila").updateChildren(amilaUpdates)
            .addOnSuccessListener(aVoid -> {
        // Update banadara (accountCreationDate + timestamp)
        java.util.Map<String, Object> banadaraUpdates = new java.util.HashMap<>();
        banadaraUpdates.put("accountCreationDate", banadaraTimestamp);
        banadaraUpdates.put("timestamp", banadaraTimestamp);
        databaseReference.child("banadara").updateChildren(banadaraUpdates)
                    .addOnSuccessListener(aVoid2 -> {
            // Update nipun (accountCreationDate + timestamp)
            java.util.Map<String, Object> nipunUpdates = new java.util.HashMap<>();
            nipunUpdates.put("accountCreationDate", nipunTimestamp);
            nipunUpdates.put("timestamp", nipunTimestamp);
            databaseReference.child("nipun").updateChildren(nipunUpdates)
                            .addOnSuccessListener(aVoid3 -> {
                callback.onComplete("Successfully fixed dates for amila, banadara, and nipun.");
                            })
                            .addOnFailureListener(e -> {
                callback.onError("Failed to update nipun: " + e.getMessage());
                            });
                    })
                    .addOnFailureListener(e -> {
            callback.onError("Failed to update banadara: " + e.getMessage());
                    });
            })
            .addOnFailureListener(e -> {
        callback.onError("Failed to update amila: " + e.getMessage());
            });
    }
}
