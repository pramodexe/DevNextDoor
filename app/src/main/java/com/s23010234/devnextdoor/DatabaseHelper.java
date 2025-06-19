package com.s23010234.devnextdoor;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

// DatabaseHelper manages the SQLite database for user login and signup.
// It provides methods to add new users and validate login credentials.
public class DatabaseHelper extends SQLiteOpenHelper {

    // Database name and version constants
    private static final String DATABASE_NAME = "user_database.db";
    private static final int DATABASE_VERSION = 1;

    // Table and columns constants
    private static final String TABLE_USER = "users";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_PASSWORD = "password";

    // Constructor for DatabaseHelper.
    // @param context The application context.
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Called when the database is created for the first time.
    // Creates the users table.
    // @param db The SQLite database.
    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableQuery = "CREATE TABLE " + TABLE_USER + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_NAME + " TEXT NOT NULL, " +
                COLUMN_USERNAME + " TEXT UNIQUE NOT NULL, " +
                COLUMN_PASSWORD + " TEXT NOT NULL);";
        db.execSQL(createTableQuery);
    }

    // Called when the database needs to be upgraded.
    // Currently drops the existing table and recreates it.
    // @param db The SQLite database.
    // @param oldVersion The old database version.
    // @param newVersion The new database version.
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String dropTableQuery = "DROP TABLE IF EXISTS " + TABLE_USER;
        db.execSQL(dropTableQuery);
        onCreate(db);
    }

    // Adds a new user to the database.
    // @param name The full name of the user.
    // @param username The unique username.
    // @param password The password (should be hashed in production).
    // @return true if user was added successfully, false otherwise.
    public boolean addUser(String name, String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(COLUMN_NAME, name);
        contentValues.put(COLUMN_USERNAME, username);
        contentValues.put(COLUMN_PASSWORD, password);

        try {
            long result = db.insertOrThrow(TABLE_USER, null, contentValues);
            return result != -1;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        // Do NOT close db here
    }

    // Checks if the username exists in the database.
    // @param username The username to check.
    // @return true if username exists, false otherwise.
    public boolean isUsernameExists(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            String query = "SELECT " + COLUMN_ID + " FROM " + TABLE_USER + " WHERE " + COLUMN_USERNAME + " = ?";
            cursor = db.rawQuery(query, new String[]{username});
            return cursor.moveToFirst();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (cursor != null) cursor.close();
            // Do NOT close db here
        }
    }

    // Validates user login credentials.
    // @param username The username entered.
    // @param password The password entered.
    // @return true if credentials are valid, false otherwise.
    public boolean validateUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            String query = "SELECT " + COLUMN_ID + " FROM " + TABLE_USER + " WHERE " +
                    COLUMN_USERNAME + " = ? AND " + COLUMN_PASSWORD + " = ?";
            cursor = db.rawQuery(query, new String[]{username, password});
            return cursor.moveToFirst();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (cursor != null) cursor.close();
            // Do NOT close db here
        }
    }
}
