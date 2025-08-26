package com.s23010234.devnextdoor;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatDelegate;
import android.widget.ScrollView;
import androidx.cardview.widget.CardView;
import android.widget.TextView;
import android.widget.ImageView;
import android.view.View;
import android.view.ViewGroup;
import android.graphics.Color;
import androidx.core.widget.ImageViewCompat;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import androidx.appcompat.app.AppCompatDelegate;

/**
 * This class manages the app's theme, especially dark mode and light mode.
 * It handles changing colors, backgrounds, and text colors throughout the app.
 * Think of it as the app's "paint job" manager - it decides what colors to use where.
 * 
 * The app has specific requirements for dark mode:
 * - Change main background to a dark theme
 * - Some text becomes black, some becomes white depending on the screen
 * - Navigation bar icons and text become black
 * - Scrollable areas get dark backgrounds
 * - Dashboard boxes keep their special color
 * - Settings explanation text becomes black
 */
public class ThemeManager {
    
    // The name of the file where we save user preferences on the device
    private static final String PREFS_NAME = "DevNextDoorPrefs";
    
    // The key we use to save whether dark mode is turned on or off
    private static final String DARK_MODE_KEY = "isDarkModeLocal";
    
    /**
     * Applies the correct theme colors to a screen based on user's preference.
     * This method figures out if the user wants dark mode or light mode,
     * then changes the screen colors accordingly.
     */
    public static void applyTheme(Activity activity) {
        // Check if the user has dark mode turned on
        boolean isDarkMode = isDarkModeEnabled(activity);
        
        // Tell Android to use dark or light theme resources (like different colored icons)
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        
        // Apply the specific color changes to this screen
        applyThemeToActivity(activity, isDarkMode);
    }
    
    /**
     * Applies specific color changes to different types of screens in the app.
     * Different screens need different color treatments in dark mode.
     * This method figures out what type of screen it is and applies the right colors.
     */
    public static void applyThemeToActivity(Activity activity, boolean isDarkMode) {
        // Tell Android to use dark or light theme resources
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            // In light mode, we don't need to change anything - Android handles it
            return;
        }
        
        // Find out which screen this is by looking at its name
        String activityName = activity.getClass().getSimpleName();
        
        // Some screens need BLACK headings in dark mode (like Homepage, Search, etc.)
        if (activityName.equals("HomepageActivity") || 
            activityName.equals("SearchActivity") || 
            activityName.equals("NotificationsActivity") || 
            activityName.equals("ChatsActivity")) {
            // Apply dark mode theme with black headings
            applyContentPageTheme(activity, true);
        } 
        // Dashboard screen needs special handling
        else if (activityName.equals("DashboardActivity")) {
            applyDashboardTheme(activity);
        }
        // Settings screen needs special handling
        else if (activityName.equals("SettingsActivity")) {
            applySettingsTheme(activity);
        } 
        // Profile screen needs very special handling
        else if (activityName.equals("ProfileActivity")) {
            applyProfileThemeSpecial(activity);
        } 
        // Some screens need WHITE headings in dark mode (like Favorites, Meet Friends)
        else if (activityName.equals("FavoritesActivity") || 
                   activityName.equals("MeetFriendsActivity")) {
            // Apply dark mode theme with white headings
            applyContentPageTheme(activity, false);
        }
    }
    
    /**
     * Apply dark mode colors to content pages like Homepage, Search, Favorites, etc.
     * This method can make headings either black or white depending on what looks best.
     * It also changes backgrounds to dark and makes scrollable text white.
     */
    private static void applyContentPageTheme(Activity activity, boolean blackHeadings) {
        // Change background to bg_main_dark
        View rootView = activity.findViewById(android.R.id.content);
        LinearLayout rootLayout = findRootLinearLayout(rootView);
        if (rootLayout != null) {
            rootLayout.setBackgroundResource(R.drawable.main_bg_dark);
        }
        
        // Special handling for all content pages - apply dark background to the main ConstraintLayout
        String activityName = activity.getClass().getSimpleName();
        if (activityName.equals("HomepageActivity") || activityName.equals("NotificationsActivity") || 
            activityName.equals("ChatsActivity") || activityName.equals("FavoritesActivity") || 
            activityName.equals("SearchActivity") || activityName.equals("MeetFriendsActivity")) {
            View mainConstraintLayout = activity.findViewById(R.id.main);
            if (mainConstraintLayout != null) {
                mainConstraintLayout.setBackgroundResource(R.drawable.main_bg_dark);
            }
        }
        
        // Apply specific text theming
        applyContentPageTextTheme(rootView, blackHeadings);
        
        // Apply navbar theming - icons/names to black
        applyNavbarTheme(activity);
        
        // Apply scrollable area backgrounds
        applyScrollableAreaBackgrounds(rootView);
        
        // Apply back arrow theming
        applyBackArrowTheme(activity);
    }
    
    /**
     * Apply theme to Dashboard:
     * - Change background to bg_main_dark
     * - Change 5 boxes background color to main theme color (#F4F0D5)
     * - Change dashboard title color to black in dark mode
     */
    private static void applyDashboardTheme(Activity activity) {
        // Change background to bg_main_dark
        View rootView = activity.findViewById(android.R.id.content);
        LinearLayout rootLayout = findRootLinearLayout(rootView);
        if (rootLayout != null) {
            rootLayout.setBackgroundResource(R.drawable.main_bg_dark);
        }
        
        // Change dashboard boxes background color to main theme color
        applyDashboardBoxBackgroundTheme(activity);
        
        // Change dashboard title color to black in dark mode
        applyDashboardTitleTheme(activity);
        
        // Apply navbar theming - icons/names to black
        applyNavbarTheme(activity);
    }
    
    /**
     * Apply theme to Settings:
     * - Background to bg_main_dark
     * - Settings cards themed
     * - Description texts to black (handled in applySettingsCardTheme)
     * - Heading and footer texts to white
     */
    private static void applySettingsTheme(Activity activity) {
        // Change background
        View rootView = activity.findViewById(android.R.id.content);
        LinearLayout rootLayout = findRootLinearLayout(rootView);
        if (rootLayout != null) {
            rootLayout.setBackgroundResource(R.drawable.main_bg_dark);
        }
        
        // Apply settings-specific theming
        // Note: We don't call applyContentPageTextTheme here because it would override
        // the black text colors we set for the description texts
        applySettingsCardTheme(activity);
        
        // Apply white color to heading and footer texts
        applySettingsHeadingAndFooterWhite(activity);
        
        // Apply back arrow theming
        applyBackArrowTheme(activity);
    }
    
    /**
     * Apply special theme to Profile page in dark mode:
     * - Change main background to bg_main_dark
     * - Change username text color to white
     * - Change information boxes background color to #dcd8c0
     * - Only change back arrow to white
     * - Do NOT change other text colors (keep them black as in light mode)
     */
    private static void applyProfileThemeSpecial(Activity activity) {
        // Change main background to bg_main_dark
        View rootView = activity.findViewById(android.R.id.content);
        LinearLayout rootLayout = findRootLinearLayout(rootView);
        if (rootLayout != null) {
            rootLayout.setBackgroundResource(R.drawable.main_bg_dark);
        }
        
        // Change username text color to white
        TextView usernameText = activity.findViewById(R.id.usernameText);
        if (usernameText != null) {
            usernameText.setTextColor(Color.WHITE);
        }
        
        // Apply custom background to profile information boxes
        applyProfileInformationBoxTheme(activity);
        
        // Apply back arrow theming (white in dark mode for visibility)
        applyBackArrowTheme(activity);
        
        // NOTE: We deliberately do NOT call:
        // - applyContentPageTextTheme() - to keep other text colors as black
        // - applyNavbarTheme() - profile page doesn't have navbar
        // - applyScrollableAreaBackgrounds() - to keep content boxes unchanged for most elements
        // This preserves the light mode appearance of text while changing box backgrounds
    }
    
    /**
     * Apply custom background theme to profile information boxes in dark mode
     * Changes each profile information box individually to use #dcd8c0 background color
     */
    private static void applyProfileInformationBoxTheme(Activity activity) {
        // 1. Apply to Bio box (TextView with id bioText)
        applyProfileBioBoxBackground(activity);
        
        // 2. Apply to Education Level box ("I'm" section)
        applyProfileEducationLevelBoxBackground(activity);
        
        // 3. Apply to Location box ("From" section)
        applyProfileLocationBoxBackground(activity);
        
        // 4. Apply to Availability box ("Available in" section)
        applyProfileAvailabilityBoxBackground(activity);
        
        // 5. Apply to Tech Stack box
        applyProfileTechStackBoxBackground(activity);
        
        // 6. Apply to Want to Learn box
        applyProfileWantToLearnBoxBackground(activity);
        
        // 7. Apply to Project Goals box
        applyProfileProjectGoalsBoxBackground(activity);
    }
    
    /**
     * Apply background to the Bio box (TextView with id bioText)
     */
    private static void applyProfileBioBoxBackground(Activity activity) {
        TextView bioText = activity.findViewById(R.id.bioText);
        if (bioText != null) {
            bioText.setBackgroundResource(R.drawable.profile_box_bg_dark);
        }
    }
    
    /**
     * Apply background to the Education Level box ("I'm" section)
     * This box contains the statusText TextView
     */
    private static void applyProfileEducationLevelBoxBackground(Activity activity) {
        TextView statusText = activity.findViewById(R.id.statusText);
        if (statusText != null) {
            // Find the direct parent LinearLayout of statusText (this is the "I'm" box)
            ViewGroup parent = (ViewGroup) statusText.getParent();
            if (parent instanceof LinearLayout) {
                parent.setBackgroundResource(R.drawable.profile_box_bg_dark);
            }
        }
    }
    
    /**
     * Apply background to the Location box ("From" section)
     * This box contains the locationText TextView
     */
    private static void applyProfileLocationBoxBackground(Activity activity) {
        TextView locationText = activity.findViewById(R.id.locationText);
        if (locationText != null) {
            // Find the direct parent LinearLayout of locationText (this is the "From" box)
            ViewGroup parent = (ViewGroup) locationText.getParent();
            if (parent instanceof LinearLayout) {
                parent.setBackgroundResource(R.drawable.profile_box_bg_dark);
            }
        }
    }
    
    /**
     * Apply background to the Availability box ("Available in" section)
     * This box contains the weekdayIndicator and other availability indicators
     */
    private static void applyProfileAvailabilityBoxBackground(Activity activity) {
        TextView weekdayIndicator = activity.findViewById(R.id.weekdayIndicator);
        if (weekdayIndicator != null) {
            // Navigate up to find the main availability container LinearLayout
            // weekdayIndicator -> horizontal LinearLayout -> vertical LinearLayout (the main availability box)
            ViewGroup horizontalLayout = (ViewGroup) weekdayIndicator.getParent(); // Horizontal LinearLayout containing WD/WE
            if (horizontalLayout != null) {
                ViewGroup availabilityBox = (ViewGroup) horizontalLayout.getParent(); // Main availability box LinearLayout
                if (availabilityBox instanceof LinearLayout) {
                    availabilityBox.setBackgroundResource(R.drawable.profile_box_bg_dark);
                }
            }
        }
    }
    
    /**
     * Apply background to the Tech Stack box
     * This box contains the techStackText TextView
     */
    private static void applyProfileTechStackBoxBackground(Activity activity) {
        TextView techStackText = activity.findViewById(R.id.techStackText);
        if (techStackText != null) {
            // Find the parent LinearLayout of techStackText (this is the Tech Stack box)
            ViewGroup parent = (ViewGroup) techStackText.getParent();
            if (parent instanceof LinearLayout) {
                parent.setBackgroundResource(R.drawable.profile_box_bg_dark);
            }
        }
    }
    
    /**
     * Apply background to the Want to Learn box
     * This box contains the wantToLearnText TextView
     */
    private static void applyProfileWantToLearnBoxBackground(Activity activity) {
        TextView wantToLearnText = activity.findViewById(R.id.wantToLearnText);
        if (wantToLearnText != null) {
            // Find the parent LinearLayout of wantToLearnText (this is the Want to Learn box)
            ViewGroup parent = (ViewGroup) wantToLearnText.getParent();
            if (parent instanceof LinearLayout) {
                parent.setBackgroundResource(R.drawable.profile_box_bg_dark);
            }
        }
    }
    
    /**
     * Apply background to the Project Goals box
     * This box contains the projectGoalsText TextView
     */
    private static void applyProfileProjectGoalsBoxBackground(Activity activity) {
        TextView projectGoalsText = activity.findViewById(R.id.projectGoalsText);
        if (projectGoalsText != null) {
            // Find the parent LinearLayout of projectGoalsText (this is the Project Goals box)
            ViewGroup parent = (ViewGroup) projectGoalsText.getParent();
            if (parent instanceof LinearLayout) {
                parent.setBackgroundResource(R.drawable.profile_box_bg_dark);
            }
        }
    }
    
    /**
     * Apply theme to Profile (now handled in applyThemeToActivity):
     * This method is kept for backward compatibility but not used
     */
    @Deprecated
    private static void applyProfileTheme(Activity activity) {
        // This method is deprecated - Profile now uses applyProfileThemeSpecial
    }
    
    /**
     * Apply text theming for content pages:
     * - Heading text to black OR white (depending on blackHeadings parameter)
     * - Scrollable area text to white
     */
    private static void applyContentPageTextTheme(View view, boolean blackHeadings) {
        applyContentPageTextThemeRecursive(view, blackHeadings, null);
    }
    
    /**
     * Recursively apply text theming with activity context for special handling
     */
    private static void applyContentPageTextThemeRecursive(View view, boolean blackHeadings, String activityName) {
        if (view instanceof TextView && !(view instanceof android.widget.EditText) && 
            !(view instanceof com.google.android.material.textfield.TextInputEditText)) {
            TextView textView = (TextView) view;
            float textSize = textView.getTextSize();
            boolean isBold = textView.getTypeface() != null && textView.getTypeface().isBold();
            
            // Check if it's in a ScrollView (scrollable area)
            boolean inScrollView = isInScrollView(view);
            
            // Check if it's in a user profile card
            boolean inUserProfileCard = isInUserProfileCard(view);
            
            // Check if it's in a favorite user item
            boolean inFavoriteUserItem = isInFavoriteUserItem(view);
            
            // Get activity name if not provided
            if (activityName == null) {
                try {
                    Activity activity = (Activity) view.getContext();
                    activityName = activity.getClass().getSimpleName();
                } catch (Exception e) {
                    activityName = "";
                }
            }
            
            // Special handling for specific activities: force content text to white
            boolean forceContentWhite = activityName.equals("HomepageActivity") || 
                                       activityName.equals("SearchActivity") || 
                                       activityName.equals("NotificationsActivity") || 
                                       activityName.equals("ChatsActivity");
            
            // Special case: User profile cards should maintain black text color in dark mode
            if (inUserProfileCard && (activityName.equals("HomepageActivity") || 
                                     activityName.equals("SearchActivity") || 
                                     activityName.equals("FavoritesActivity"))) {
                // Keep user profile card text black as defined in the XML resources
                return; // Don't override the color - let XML @color/card_text_color handle it
            }
            // Special case: Favorite user items should maintain black text color in dark mode
            else if (inFavoriteUserItem && activityName.equals("FavoritesActivity")) {
                // Keep favorite user item text black as defined in the XML resources
                return; // Don't override the color - let XML @color/card_text_color handle it
            }
            // Special case: Meet Friends activity - only the main title (26sp) should be white
            else if (activityName.equals("MeetFriendsActivity")) {
                // Check if this is the main page title (exactly 26sp text size)
                float textSizeSp = textSize / view.getContext().getResources().getDisplayMetrics().scaledDensity;
                if (Math.abs(textSizeSp - 26.0f) < 1.0f) {
                    // Main title "Meet Friends" should be white in dark mode
                    textView.setTextColor(Color.WHITE);
                } else {
                    // All other text should remain black - don't override
                    return;
                }
            } else if (inScrollView && forceContentWhite) {
                // Force scrollable content text to white for these specific pages
                textView.setTextColor(Color.WHITE);
            } else if (textSize > 60 || (isBold && !inScrollView)) {
                // Heading text (large or bold text outside scroll areas) - black or white depending on the page
                if (blackHeadings) {
                    textView.setTextColor(Color.BLACK);
                } else {
                    textView.setTextColor(Color.WHITE);
                }
            } else if (inScrollView) {
                // Scrollable area text - white in dark mode
                textView.setTextColor(Color.WHITE);
            } else {
                // Other text - white in dark mode
                textView.setTextColor(Color.WHITE);
            }
        }
        
        // Recursively apply to child views
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                applyContentPageTextThemeRecursive(group.getChildAt(i), blackHeadings, activityName);
            }
        }
    }
    
    /**
     * Apply theme to settings cards
     */
    private static void applySettingsCardTheme(Activity activity) {
        int[] cardIds = {
            R.id.dark_mode_card, R.id.change_username_card, R.id.change_password_card,
            R.id.logout_card, R.id.delete_account_card
        };
        
        for (int cardId : cardIds) {
            View view = activity.findViewById(cardId);
            if (view instanceof CardView) {
                ((CardView) view).setCardBackgroundColor(Color.parseColor("#2C2C2C"));
                // Apply black color to explanation texts (smaller text)
                applySettingsCardTextColor((CardView) view);
            }
        }
        
        // Directly target description texts by ID to ensure they are black
        applySettingsDescriptionTextColors(activity);
    }
    
    /**
     * Apply black color to settings explanation texts
     * Recursively searches for all TextViews in the card and applies black color to explanation text
     */
    private static void applySettingsCardTextColor(CardView cardView) {
        applySettingsCardTextColorRecursive(cardView);
    }
    
    /**
     * Apply black color to specific settings description texts by ID
     */
    private static void applySettingsDescriptionTextColors(Activity activity) {
        int[] descriptionIds = {
            R.id.dark_mode_description, R.id.change_username_description, 
            R.id.change_password_description, R.id.logout_description, 
            R.id.delete_account_description
        };
        
        for (int textId : descriptionIds) {
            TextView textView = activity.findViewById(textId);
            if (textView != null) {
                textView.setTextColor(Color.BLACK);
            }
        }
    }
    
    /**
     * Recursively apply black color to settings explanation texts
     */
    private static void applySettingsCardTextColorRecursive(View view) {
        if (view instanceof TextView) {
            TextView textView = (TextView) view;
            float textSize = textView.getTextSize();
            boolean isBold = textView.getTypeface() != null && textView.getTypeface().isBold();
            
            // Apply black color to smaller, non-bold text (explanation text)
            // Also check text content to identify explanation text more accurately
            String text = textView.getText().toString().toLowerCase();
            boolean isExplanationText = text.contains("toggle") || text.contains("change") || 
                                       text.contains("update") || text.contains("delete") ||
                                       text.contains("logout") || (!isBold && textSize <= 48);
            
            if (isExplanationText) {
                textView.setTextColor(Color.BLACK);
            }
        } else if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                applySettingsCardTextColorRecursive(group.getChildAt(i));
            }
        }
    }
    
    /**
     * Apply theme to dashboard boxes - use box_bg_dark.xml drawable
     * This preserves rounded corners and black borders exactly like light mode
     * but with main theme color (#F4F0D5) background
     */
    private static void applyDashboardBoxBackgroundTheme(Activity activity) {
        int[] boxIds = {
            R.id.boxProfile, R.id.boxSettings, R.id.boxFavorites, 
            R.id.boxMeetFriends
        };
        
        for (int boxId : boxIds) {
            View view = activity.findViewById(boxId);
            if (view != null) {
                // Use the dark mode drawable that has the same styling as light mode
                // but with main theme color background
                view.setBackgroundResource(R.drawable.box_bg_dark);
            }
        }
    }
    
    /**
     * Apply theme to dashboard title - change color to black in dark mode
     */
    private static void applyDashboardTitleTheme(Activity activity) {
        TextView dashboardTitle = activity.findViewById(R.id.textView);
        if (dashboardTitle != null) {
            dashboardTitle.setTextColor(Color.BLACK);
        }
    }
    
    
    
    /**
     * Apply navbar theming - change icon names/text to black in dark mode
     * Special focus on search icon and text
     */
    private static void applyNavbarTheme(Activity activity) {
        // Bottom navigation bar icons/text to black
        int[] navIds = {R.id.navHomepage, R.id.navGroups, R.id.navDashboard, 
                       R.id.navNotifications, R.id.navChats, R.id.navSearch};
        
        for (int navId : navIds) {
            LinearLayout navItem = activity.findViewById(navId);
            if (navItem != null) {
                // Apply black color to all TextView children in navbar
                applyNavbarTextColor(navItem, Color.BLACK);
                
                // Special handling for search nav item to ensure search text is black
                if (navId == R.id.navSearch) {
                    applySearchNavItemTheme(navItem);
                }
            }
        }
    }
    
    /**
     * Apply black color to navbar text recursively
     */
    private static void applyNavbarTextColor(ViewGroup viewGroup, int color) {
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View child = viewGroup.getChildAt(i);
            if (child instanceof TextView) {
                ((TextView) child).setTextColor(color);
            } else if (child instanceof ViewGroup) {
                applyNavbarTextColor((ViewGroup) child, color);
            }
        }
    }
    
    /**
     * Apply specific theme to search navigation item - ensure search text is black
     */
    private static void applySearchNavItemTheme(ViewGroup searchNavItem) {
        for (int i = 0; i < searchNavItem.getChildCount(); i++) {
            View child = searchNavItem.getChildAt(i);
            if (child instanceof TextView) {
                TextView textView = (TextView) child;
                String text = textView.getText().toString();
                if ("Search".equals(text)) {
                    textView.setTextColor(Color.BLACK);
                }
            } else if (child instanceof ViewGroup) {
                applySearchNavItemTheme((ViewGroup) child);
            }
        }
    }
    
    /**
     * Apply white color to settings heading and footer texts in dark mode
     */
    private static void applySettingsHeadingAndFooterWhite(Activity activity) {
        // Settings heading
        TextView settingsHeading = activity.findViewById(R.id.textView);
        if (settingsHeading != null) {
            settingsHeading.setTextColor(Color.WHITE);
        }
        
        // Footer texts
        TextView footerDesignedText = activity.findViewById(R.id.footer_designed_text);
        if (footerDesignedText != null) {
            footerDesignedText.setTextColor(Color.WHITE);
        }
        
        TextView footerDeveloperText = activity.findViewById(R.id.footer_developer_text);
        if (footerDeveloperText != null) {
            footerDeveloperText.setTextColor(Color.WHITE);
        }
    }
    
    /**
     * Apply theme to back arrow in dark mode
     * Sets the back arrow color to white in dark mode for better visibility
     */
    private static void applyBackArrowTheme(Activity activity) {
        ImageView backArrow = activity.findViewById(R.id.backArrow);
        if (backArrow != null) {
            // Set the tint to white in dark mode
            ImageViewCompat.setImageTintList(backArrow, ColorStateList.valueOf(Color.WHITE));
        }
    }
    
    /**
     * Apply bg_main_dark background to scrollable areas
     */
    private static void applyScrollableAreaBackgrounds(View view) {
        if (view instanceof ScrollView) {
            view.setBackgroundResource(R.drawable.main_bg_dark);
        }
        
        // Recursively apply to child views
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                applyScrollableAreaBackgrounds(group.getChildAt(i));
            }
        }
    }
    
    /**
     * Check if a view is inside a ScrollView
     */
    private static boolean isInScrollView(View view) {
        ViewGroup parent = (ViewGroup) view.getParent();
        while (parent != null) {
            if (parent instanceof ScrollView) {
                return true;
            }
            if (parent.getParent() instanceof ViewGroup) {
                parent = (ViewGroup) parent.getParent();
            } else {
                break;
            }
        }
        return false;
    }
    
    /**
     * Check if a view is inside a user profile card
     * @param view The view to check
     * @return True if the view is inside a CardView (user profile card)
     */
    private static boolean isInUserProfileCard(View view) {
        ViewGroup parent = (ViewGroup) view.getParent();
        while (parent != null) {
            if (parent instanceof androidx.cardview.widget.CardView) {
                return true;
            }
            if (parent.getParent() instanceof ViewGroup) {
                parent = (ViewGroup) parent.getParent();
            } else {
                break;
            }
        }
        return false;
    }
    
    /**
     * Check if a view is inside a favorite user item
     * @param view The view to check
     * @return True if the view is inside a favorite user item layout
     */
    private static boolean isInFavoriteUserItem(View view) {
        ViewGroup parent = (ViewGroup) view.getParent();
        while (parent != null) {
            // Check if parent has the box_bg background which indicates favorite user item
            if (parent instanceof LinearLayout) {
                try {
                    // Check if this LinearLayout has the specific IDs from favorite_user_item.xml
                    View usernameView = parent.findViewById(R.id.favoriteUsername);
                    View bioView = parent.findViewById(R.id.favoriteBio);
                    if (usernameView != null || bioView != null) {
                        return true;
                    }
                } catch (Exception e) {
                    // Continue searching if findViewById fails
                }
            }
            if (parent.getParent() instanceof ViewGroup) {
                parent = (ViewGroup) parent.getParent();
            } else {
                break;
            }
        }
        return false;
    }
    
    /**
     * Finds the root LinearLayout in the activity
     * @param view The view to search from
     * @return The root LinearLayout if found
     */
    private static LinearLayout findRootLinearLayout(View view) {
        if (view instanceof LinearLayout) {
            return (LinearLayout) view;
        } else if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                LinearLayout result = findRootLinearLayout(group.getChildAt(i));
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }
    
    /**
     * Finds the root ViewGroup in the activity
     * @param view The view to search from
     * @return The root ViewGroup if found
     */
    private static ViewGroup findRootViewGroup(View view) {
        if (view instanceof ViewGroup && ((ViewGroup) view).getChildCount() > 0) {
            return (ViewGroup) view;
        } else if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                ViewGroup result = findRootViewGroup(group.getChildAt(i));
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }
    
    /**
     * Checks if dark mode is currently enabled
     * @param context Application context
     * @return True if dark mode is enabled
     */
    public static boolean isDarkModeEnabled(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(DARK_MODE_KEY, false); // Default to light mode
    }
    
    /**
     * Saves the dark mode preference locally
     * @param context Application context
     * @param isDarkMode True to enable dark mode, false for light mode
     */
    public static void saveDarkModePreference(Context context, boolean isDarkMode) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(DARK_MODE_KEY, isDarkMode);
        editor.apply();
    }
    
    /**
     * Toggles the current theme and applies it globally
     * @param activity The activity to toggle theme for
     * @return The new theme state (true for dark, false for light)
     */
    public static boolean toggleTheme(Activity activity) {
        boolean currentDarkMode = isDarkModeEnabled(activity);
        boolean newDarkMode = !currentDarkMode;
        
        saveDarkModePreference(activity, newDarkMode);
        applyThemeGlobally(activity, newDarkMode);
        
        return newDarkMode;
    }
    
    /**
     * Applies theme globally by recreating the current activity
     * This ensures the theme change is immediately visible
     * @param activity The current activity
     * @param isDarkMode True for dark mode, false for light mode
     */
    public static void applyThemeGlobally(Activity activity, boolean isDarkMode) {
        // Save the preference first
        saveDarkModePreference(activity, isDarkMode);
        
        // Set the AppCompatDelegate night mode to ensure drawable-night resources are used
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        
        // Recreate the current activity to apply the new theme immediately
        activity.recreate();
    }
}
