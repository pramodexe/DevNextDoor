package com.s23010234.devnextdoor;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Vibrator;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Shake Base Activity - Provides shake detection to other activities
 * 
 * This is a special base class that other activities can extend to
 * automatically get shake detection functionality. Think of it like
 * a foundation that gives activities the ability to detect when
 * users shake their phone.
 * 
 * Any activity that extends this class will automatically:
 * - Detect when the user shakes their phone
 * - Open the Settings screen when a shake is detected
 * - Provide haptic feedback (vibration) when shake occurs
 * - Handle enabling/disabling shake detection properly
 * 
 * This creates a consistent "shake to open settings" feature
 * throughout the app, similar to how many social media apps
 * use shake gestures for quick actions.
 * 
 * Activities that extend this class just need to call
 * initializeShakeDetection() in their onCreate method.
 */
public abstract class ShakeBaseActivity extends AppCompatActivity implements ShakeDetector.OnShakeListener {
    
    // Objects needed for detecting phone shakes
    private SensorManager sensorManager;    // Manages access to device sensors
    private Sensor accelerometer;           // The motion sensor that detects movement
    private ShakeDetector shakeDetector;    // Our custom shake detection logic
    private boolean shakeDetectionEnabled = true;  // Whether shake detection is currently active

    /**
     * Initialize Shake Detection - Sets up the shake detection system
     * 
     * This method should be called by activities that want to enable
     * shake detection. It sets up the sensors and shake detector.
     */
    protected void initializeShakeDetection() {
        // Get access to the device's sensor system
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (sensorManager != null) {
            // Get the accelerometer sensor (detects device movement)
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            if (accelerometer != null) {
                // Create our shake detector and set this activity as the listener
                shakeDetector = new ShakeDetector();
                shakeDetector.setOnShakeListener(this);
            }
        }
    }

    /**
     * Enable Shake Detection - Starts listening for shake gestures
     * 
     * This activates the shake detection by registering our detector
     * with the sensor manager to receive accelerometer updates.
     */
    protected void enableShakeDetection() {
        if (sensorManager != null && accelerometer != null && shakeDetector != null && shakeDetectionEnabled) {
            sensorManager.registerListener(shakeDetector, accelerometer, SensorManager.SENSOR_DELAY_UI);
        }
    }

    /**
     * Disable Shake Detection - Stops listening for shake gestures
     * 
     * This deactivates shake detection to save battery and prevent
     * unwanted shake events when the activity is not active.
     */
    protected void disableShakeDetection() {
        if (sensorManager != null && shakeDetector != null) {
            sensorManager.unregisterListener(shakeDetector);
        }
    }

    protected void setShakeDetectionEnabled(boolean enabled) {
        this.shakeDetectionEnabled = enabled;
        if (!enabled) {
            disableShakeDetection();
        } else {
            enableShakeDetection();
        }
    }

    @Override
    public void onShake(int count) {
        // Trigger settings activity on shake (require at least 2 shakes to avoid accidental triggers)
        if (count >= 2 && shakeDetectionEnabled) {
            // Provide haptic feedback
            try {
                Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                if (vibrator != null && vibrator.hasVibrator()) {
                    // Short vibration pattern: vibrate 100ms, pause 50ms, vibrate 100ms
                    long[] pattern = {0, 100, 50, 100};
                    vibrator.vibrate(pattern, -1);
                }
                
                runOnUiThread(() -> {
                    Toast.makeText(this, "Shake detected! Opening settings...", Toast.LENGTH_SHORT).show();
                    
                    // Open settings activity
                    Intent settingsIntent = new Intent(this, SettingsActivity.class);
                    startActivity(settingsIntent);
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        enableShakeDetection();
    }

    @Override
    protected void onPause() {
        super.onPause();
        disableShakeDetection();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disableShakeDetection();
    }
}
