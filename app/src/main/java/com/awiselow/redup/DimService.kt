package com.awiselow.redup

import android.app.*
import android.content.*
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.View
import android.view.WindowManager
import androidx.core.app.NotificationCompat

class DimService : Service() {

    private var overlayView: View? = null
    private lateinit var windowManager: WindowManager
    private val CHANNEL_ID = "redup_channel"
    private val NOTIF_ID = 1

    private val stopReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            saveState(false)
            stopSelf()
        }
    }

    override fun onCreate() {
        super.onCreate()
        registerReceiver(stopReceiver, IntentFilter("com.awiselow.redup.EXIT"))
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val alpha = intent?.getIntExtra("alpha", 80) ?: 80
        saveState(true)
        startForeground(NOTIF_ID, buildNotification(alpha))
        showOverlay(alpha)
        return START_STICKY
    }

    private fun showOverlay(alpha: Int) {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        overlayView?.let { windowManager.removeView(it) }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )

        overlayView = View(this).apply {
            setBackgroundColor(Color.BLACK)
            this.alpha = alpha / 100f
        }

        windowManager.addView(overlayView, params)
    }

    private fun buildNotification(alpha: Int): Notification {
        val exitIntent = PendingIntent.getBroadcast(
            this, 0,
            Intent("com.awiselow.redup.EXIT"),
            PendingIntent.FLAG_IMMUTABLE
        )

        val openIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            },
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Redup aktif")
            .setContentText("Kegelapan: $alpha% — Tap untuk atur")
            .setSmallIcon(android.R.drawable.ic_menu_view)
            .setOngoing(true)
            .setContentIntent(openIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Exit", exitIntent)
            .build()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Redup Service",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Notifikasi kontrol Redup"
            setShowBadge(false)
        }
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(channel)
    }

    private fun saveState(active: Boolean) {
        getSharedPreferences("redup_prefs", MODE_PRIVATE)
            .edit().putBoolean("is_active", active).apply()
    }

    override fun onDestroy() {
        overlayView?.let { windowManager.removeView(it) }
        unregisterReceiver(stopReceiver)
        saveState(false)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
