package com.awiptk.redup;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.view.View;
import android.view.WindowManager;

public class FilterService extends Service {

    public static boolean isRunning = false;
    public static int opacity = 50;
    public static int colorMode = 0; // 0=normal, 1=warm, 2=cool

    private WindowManager wm;
    private View filterView;
    private BroadcastReceiver updateReceiver;

    private static final String CHANNEL_ID = "redup_channel";

    @Override
    public void onCreate() {
        super.onCreate();
        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        isRunning = true;
        startForeground(1, buildNotification());
        addFilterView();
        registerUpdateReceiver();
        return START_STICKY;
    }

    private void addFilterView() {
        if (filterView != null) {
            wm.removeView(filterView);
        }
        filterView = new View(this);
        filterView.setBackgroundColor(getFilterColor());

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                        ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                        : WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT
        );
        wm.addView(filterView, params);
    }

    private int getFilterColor() {
        int alpha = (int) (opacity / 100.0f * 255);
        switch (colorMode) {
            case 1: return Color.argb(alpha, 255, 140, 0);   // warm
            case 2: return Color.argb(alpha, 0, 100, 255);   // cool
            default: return Color.argb(alpha, 0, 0, 0);      // normal
        }
    }

    private void registerUpdateReceiver() {
        updateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (filterView != null) {
                    filterView.setBackgroundColor(getFilterColor());
                }
            }
        };
        IntentFilter filter = new IntentFilter("com.awiptk.redup.UPDATE");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(updateReceiver, filter, RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(updateReceiver, filter);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning = false;
        if (filterView != null) wm.removeView(filterView);
        if (updateReceiver != null) unregisterReceiver(updateReceiver);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "Redup Filter", NotificationManager.IMPORTANCE_LOW);
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }
    }

    private Notification buildNotification() {
        Intent stopIntent = new Intent(this, FilterService.class);
        stopIntent.setAction("STOP");
        PendingIntent stopPending = PendingIntent.getService(
                this, 0, stopIntent, PendingIntent.FLAG_IMMUTABLE);

        Intent openIntent = new Intent(this, MainActivity.class);
        PendingIntent openPending = PendingIntent.getActivity(
                this, 0, openIntent, PendingIntent.FLAG_IMMUTABLE);

        return new Notification.Builder(this, CHANNEL_ID)
                .setContentTitle("Redup aktif")
                .setContentText("Filter layar sedang berjalan")
                .setSmallIcon(android.R.drawable.ic_menu_view)
                .setContentIntent(openPending)
                .addAction(android.R.drawable.ic_delete, "Matikan", stopPending)
                .setOngoing(true)
                .build();
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }
}
