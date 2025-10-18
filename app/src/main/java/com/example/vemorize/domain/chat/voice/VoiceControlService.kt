package com.example.vemorize.domain.chat.voice

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.vemorize.R

/**
 * Foreground service for voice control with screen off
 * Manages voice recognition and wake word detection states
 */
class VoiceControlService : Service() {

    private var currentState: VoiceControlState = VoiceControlState.Stopped

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "VoiceControlService onCreate")
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "VoiceControlService onStartCommand")

        when (intent?.action) {
            ACTION_START -> startVoiceControl()
            ACTION_STOP -> stopVoiceControl()
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        // This is a started service, not a bound service
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "VoiceControlService onDestroy")
        stopVoiceControl()
    }

    private fun startVoiceControl() {
        Log.d(TAG, "Starting voice control service")

        // Start foreground with notification
        val notification = createNotification(currentState)
        startForeground(NOTIFICATION_ID, notification)

        Log.d(TAG, "Voice control service started in STOPPED state")
    }

    private fun stopVoiceControl() {
        Log.d(TAG, "Stopping voice control service")

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
