package com.example.vemorize.domain.chat.voice

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
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

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "VoiceControlService onCreate")
        createNotificationChannel()
        initializeVoiceInput()
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
                    // Auto-restart listening after recognition
                    if (currentState is VoiceControlState.ActiveListening) {
                        voiceInputManager?.startListening()
                    }
                }
            }
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
                stopListening()
            }
            is VoiceControlState.ActiveListening -> {
                startListening()
            }
            is VoiceControlState.WakeWordMode -> {
                // Will be implemented in next step
                Log.d(TAG, "WakeWordMode not yet implemented")
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

    private fun stopVoiceControl() {
        Log.d(TAG, "Stopping voice control service")

        stopListening()
        voiceInputManager?.destroy()
        voiceInputManager = null

        currentState = VoiceControlState.Stopped
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()

        Log.d(TAG, "Voice control service stopped")
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
