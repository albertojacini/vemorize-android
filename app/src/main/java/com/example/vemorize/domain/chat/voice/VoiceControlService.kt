package com.example.vemorize.domain.chat.voice

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.example.vemorize.R
import kotlinx.coroutines.launch

/**
 * Foreground service for voice control with screen off
 * Manages voice recognition and wake word detection states
 */
class VoiceControlService : LifecycleService() {

    private var currentState: VoiceControlState = VoiceControlState.Stopped
    private var voiceInputManager: VoiceInputManager? = null
    private var wakeWordDetector: PorcupineWakeWordDetector? = null

    // Inactivity timer
    private val handler = Handler(Looper.getMainLooper())
    private var inactivityRunnable: Runnable? = null

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "VoiceControlService onCreate")
        createNotificationChannel()
        initializeVoiceInput()
        initializeWakeWordDetector()
    }

    private fun initializeVoiceInput() {
        voiceInputManager = VoiceInputManager(applicationContext).apply {
            initialize()
        }

        // Observe recognized text and auto-restart listening
        lifecycleScope.launch {
            voiceInputManager?.recognizedText?.collect { text ->
                if (!text.isNullOrBlank()) {
                    Log.d(TAG, "Recognized text: $text")
                    resetInactivityTimer() // Reset timer on voice activity
                    // Auto-restart listening after recognition
                    if (currentState is VoiceControlState.ActiveListening) {
                        voiceInputManager?.startListening()
                    }
                }
            }
        }

        // Observe partial results to reset inactivity timer
        lifecycleScope.launch {
            voiceInputManager?.partialText?.collect { text ->
                if (!text.isNullOrBlank()) {
                    Log.d(TAG, "Partial text: $text")
                    resetInactivityTimer()
                }
            }
        }

        // Observe listening state to reset inactivity timer
        lifecycleScope.launch {
            voiceInputManager?.isListening?.collect { isListening ->
                if (isListening) {
                    Log.d(TAG, "Voice recognition started")
                    resetInactivityTimer()
                }
            }
        }
    }

    private fun initializeWakeWordDetector() {
        // TODO: Replace with actual Porcupine access key from configuration
        // Get a free access key from https://console.picovoice.ai/
        val accessKey = "YOUR_PORCUPINE_ACCESS_KEY_HERE"

        try {
            wakeWordDetector = PorcupineWakeWordDetector(applicationContext, accessKey)
            // Note: We don't initialize yet - will do that when transitioning to WakeWordMode
            Log.d(TAG, "PorcupineWakeWordDetector created")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create PorcupineWakeWordDetector. Please configure a valid Porcupine access key.", e)
            wakeWordDetector = null
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "VoiceControlService onStartCommand")

        when (intent?.action) {
            ACTION_START -> startVoiceControl()
            ACTION_STOP -> stopVoiceControl()
        }

        return START_STICKY
    }


    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "VoiceControlService onDestroy")
        cancelInactivityTimer()
        stopVoiceControl()
    }

    private fun startVoiceControl() {
        Log.d(TAG, "Starting voice control service")

        // Transition to ActiveListening state
        transitionToState(VoiceControlState.ActiveListening)

        Log.d(TAG, "Voice control service started in ACTIVE_LISTENING state")
    }

    private fun transitionToState(newState: VoiceControlState) {
        if (!currentState.canTransitionTo(newState)) {
            Log.w(TAG, "Invalid state transition from ${currentState::class.simpleName} to ${newState::class.simpleName}")
            return
        }

        Log.d(TAG, "State transition: ${currentState::class.simpleName} -> ${newState::class.simpleName}")
        currentState = newState

        // Update notification
        val notification = createNotification(currentState)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)

        // Handle state-specific behavior
        when (newState) {
            is VoiceControlState.Stopped -> {
                cancelInactivityTimer()
                stopListening()
                stopWakeWordDetection()
            }
            is VoiceControlState.ActiveListening -> {
                stopWakeWordDetection()
                startListening()
                startInactivityTimer() // Start 60-second timer
            }
            is VoiceControlState.WakeWordMode -> {
                cancelInactivityTimer()
                stopListening()
                startWakeWordDetection()
            }
        }
    }

    private fun startListening() {
        Log.d(TAG, "Starting voice recognition")
        // Start foreground if not already
        val notification = createNotification(currentState)
        startForeground(NOTIFICATION_ID, notification)

        voiceInputManager?.startListening()
    }

    private fun stopListening() {
        Log.d(TAG, "Stopping voice recognition")
        voiceInputManager?.stopListening()
    }

    private fun startWakeWordDetection() {
        Log.d(TAG, "Starting wake word detection")

        val detector = wakeWordDetector
        if (detector == null) {
            Log.w(TAG, "Wake word detector not initialized - cannot start detection")
            return
        }

        try {
            // Initialize with callback to transition back to ActiveListening
            detector.initialize(wakeWord = "porcupine") {
                Log.d(TAG, "Wake word detected! Transitioning back to ACTIVE_LISTENING")
                onWakeWordDetected()
            }

            // Start detection
            detector.start()
            Log.d(TAG, "Wake word detection started")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start wake word detection", e)
        }
    }

    /**
     * Called when wake word is detected
     * Transitions back to ActiveListening state
     */
    private fun onWakeWordDetected() {
        Log.d(TAG, "Processing wake word detection")

        // Transition back to active listening
        transitionToState(VoiceControlState.ActiveListening)
    }

    private fun stopWakeWordDetection() {
        Log.d(TAG, "Stopping wake word detection")
        wakeWordDetector?.stop()
    }

    private fun stopVoiceControl() {
        Log.d(TAG, "Stopping voice control service")

        cancelInactivityTimer()
        stopListening()
        stopWakeWordDetection()

        voiceInputManager?.destroy()
        voiceInputManager = null

        wakeWordDetector?.destroy()
        wakeWordDetector = null

        currentState = VoiceControlState.Stopped
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()

        Log.d(TAG, "Voice control service stopped")
    }

    /**
     * Start the 60-second inactivity timer
     * When it expires, transition to WakeWordMode
     */
    private fun startInactivityTimer() {
        cancelInactivityTimer() // Cancel any existing timer

        inactivityRunnable = Runnable {
            Log.d(TAG, "Inactivity timeout reached - transitioning to WakeWordMode")
            transitionToState(VoiceControlState.WakeWordMode)
        }

        handler.postDelayed(inactivityRunnable!!, INACTIVITY_TIMEOUT_MS)
        Log.d(TAG, "Inactivity timer started (${INACTIVITY_TIMEOUT_MS / 1000}s)")
    }

    /**
     * Reset the inactivity timer (cancel and restart)
     * Call this on any voice activity
     */
    private fun resetInactivityTimer() {
        if (currentState !is VoiceControlState.ActiveListening) {
            return // Only reset timer in ACTIVE_LISTENING state
        }

        Log.d(TAG, "Resetting inactivity timer")
        startInactivityTimer()
    }

    /**
     * Cancel the inactivity timer
     */
    private fun cancelInactivityTimer() {
        inactivityRunnable?.let {
            handler.removeCallbacks(it)
            inactivityRunnable = null
            Log.d(TAG, "Inactivity timer cancelled")
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Voice Control",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Voice control service status"
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            Log.d(TAG, "Notification channel created")
        }
    }

    private fun createNotification(state: VoiceControlState): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Vemorize Voice Control")
            .setContentText(state.displayName())
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }

    companion object {
        private const val TAG = "VoiceControlService"
        private const val CHANNEL_ID = "voice_control_channel"
        private const val NOTIFICATION_ID = 1001

        // 60-second inactivity timeout before transitioning to WakeWordMode
        private const val INACTIVITY_TIMEOUT_MS = 60_000L

        const val ACTION_START = "com.example.vemorize.ACTION_START_VOICE_CONTROL"
        const val ACTION_STOP = "com.example.vemorize.ACTION_STOP_VOICE_CONTROL"

        /**
         * Start the voice control service
         */
        fun start(context: Context) {
            val intent = Intent(context, VoiceControlService::class.java).apply {
                action = ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        /**
         * Stop the voice control service
         */
        fun stop(context: Context) {
            val intent = Intent(context, VoiceControlService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }
}
