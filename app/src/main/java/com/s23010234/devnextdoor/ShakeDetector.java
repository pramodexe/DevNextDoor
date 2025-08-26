package com.s23010234.devnextdoor;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * Shake Detector - Detects when the user shakes their phone
 * 
 * This class uses the phone's accelerometer (motion sensor) to detect
 * when someone shakes their device. Think of it like a motion detector
 * that can tell when the phone is being moved around vigorously.
 * 
 * The shake detection works by:
 * 1. Continuously monitoring the phone's movement in 3D space (X, Y, Z axes)
 * 2. Calculating the total force being applied to the device
 * 3. Triggering a "shake detected" event when the force exceeds a threshold
 * 
 * This is commonly used for features like "shake to refresh" or
 * "shake to open settings" that many apps have.
 */
public class ShakeDetector implements SensorEventListener {
    
    // Sensitivity settings for shake detection
    private static final float SHAKE_THRESHOLD_GRAVITY = 2.7F;        // How hard you need to shake
    private static final int SHAKE_SLOP_TIME_MS = 500;                // Minimum time between shakes (milliseconds)
    private static final int SHAKE_COUNT_RESET_TIME_MS = 3000;        // Reset shake count after this time
    
    // Variables to track shake state
    private OnShakeListener shakeListener;    // Object that gets notified when shake happens
    private long shakeTimestamp;              // When the last shake occurred
    private int shakeCount;                   // How many times user has shaken recently

    /**
     * Interface for Shake Events
     * 
     * This interface defines what happens when a shake is detected.
     * Other classes can implement this to respond to shake gestures.
     */
    public interface OnShakeListener {
        /**
         * Called when a shake is detected
         * @param count How many times the user has shaken recently
         */
        void onShake(int count);
    }

    /**
     * Set Shake Listener - Register an object to be notified of shakes
     * 
     * This method allows other parts of the app to "listen" for shake events.
     * When a shake is detected, the listener's onShake method will be called.
     * 
     * @param listener The object that wants to know about shake events
     */
    public void setOnShakeListener(OnShakeListener listener) {
        this.shakeListener = listener;
    }

    /**
     * onAccuracyChanged - Called when sensor accuracy changes
     * 
     * This method is required by the SensorEventListener interface, but we
     * don't need to do anything when the accelerometer's accuracy changes.
     * The shake detection works fine regardless of accuracy level.
     * 
     * @param sensor The sensor whose accuracy changed
     * @param accuracy The new accuracy level
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // We don't need to respond to accuracy changes for shake detection
    }

    /**
     * onSensorChanged - The main shake detection logic
     * 
     * This method runs every time the accelerometer reports new movement data.
     * It analyzes the movement to determine if it qualifies as a "shake".
     * Think of this like a security guard who watches for suspicious movement
     * and sounds an alarm when they detect something unusual.
     * 
     * @param event Contains the latest movement data from the accelerometer
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        // Only process shake detection if someone is listening for shakes
        if (shakeListener != null) {
            // Get the raw movement values from the accelerometer
            // These represent how much the phone moved in each direction
            float x = event.values[0];  // Left/right movement
            float y = event.values[1];  // Forward/backward movement
            float z = event.values[2];  // Up/down movement

            // Convert raw values to gravity units (g-force)
            // 1g = normal gravity when phone is sitting still
            // Higher values = more violent movement
            float gX = x / SensorManager.GRAVITY_EARTH;
            float gY = y / SensorManager.GRAVITY_EARTH;
            float gZ = z / SensorManager.GRAVITY_EARTH;

            // Calculate total movement force using 3D math
            // This combines all three directions into one number
            float gForce = (float) Math.sqrt(gX * gX + gY * gY + gZ * gZ);

            // Check if the movement is strong enough to be considered a "shake"
            if (gForce > SHAKE_THRESHOLD_GRAVITY) {
                final long now = System.currentTimeMillis();
                
                // Ignore shakes that happen too quickly after each other
                // This prevents one physical shake from being counted multiple times
                if (shakeTimestamp + SHAKE_SLOP_TIME_MS > now) {
                    return;
                }

                // Reset the shake count if it's been a while since the last shake
                // This prevents old shakes from being counted with new ones
                if (shakeTimestamp + SHAKE_COUNT_RESET_TIME_MS < now) {
                    shakeCount = 0;
                }

                // Record this shake and increment the count
                shakeTimestamp = now;
                shakeCount++;

                // Notify whoever is listening that a shake occurred
                shakeListener.onShake(shakeCount);
            }
        }
    }
}
