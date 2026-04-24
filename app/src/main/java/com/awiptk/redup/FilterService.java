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
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;

public class FilterService extends Service {

    public static boolean isRunning = false;
    public static int opacity = 50;
    public static int colorMode = 0;

    private WindowManager wm;
    private View filterView;
    private View navView;
    private View statusView;
    private BroadcastReceiver updateReceiver;

    static final String CHANNEL_ID = "redup_channel";
    static final String ACTION_STOP = "com.awiptk.redup.STOP";
    static final String ACTION_UPDATE = "com.awiptk.redup.UPDATE";
    static final String ACTION_BRIGHTER = "com.awiptk.redup.BRIGHTER";
    static final String ACTION_DARKER = "com.awiptk.redup.DARKER";

    @Override
    public void onCreate() {
        super.onCreate();
        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && ACTION_STOP.equals(intent.getAction())) {
            stopSelf();
            return START_NOT_STICKY;
        }
        if (intent != null && ACTION_BRIGHTER.equals(intent.getAction())) {
            opacity = Math.max(0, opacity - 10);
            updateFilter();
            updateNotification();
            return START_STICKY;
        }
        if (intent != null && ACTION_DARKER.equals(intent.getAction())) {
            opacity = Math.min(90, opacity + 10);
            updateFilter();
            updateNotification();
            return START_STICKY;
        }

        isRunning = true;
        startForeground(1, buildNotification());
        addFilterView();
        registerUpdateReceiver();
        return START_STICKY;
    }

    private int getType() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                : WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY;
    }

    private void addFilterView() {
        removeAllViews();

        int color = getFilterColor();

        // Main filter — layar utama
        filterView = new View(this);
        filterView.setBackgroundColor(color);
        WindowManager.LayoutParams mainParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                getType(),
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT
        );
        wm.addView(filterView, mainParams);

        // Nav bar filter — overlay di bawah
        navView = new View(this);
        navView.setBackgroundColor(color);
        WindowManager.LayoutParams navParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                getNavBarHeight(),
                getType(),
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT
        );
        navParams.gravity = Gravity.BOTTOM;
        wm.addView(navView, navParams);

        // Status bar filter — overlay di atas
        statusView = new View(this);
        statusView.setBackgroundColor(color);
        WindowManager.LayoutParams statusParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                getStatusBarHeight(),
                getType(),
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT
        );
        navParams.gravity = Gravity.TOP;
        wm.addView(statusView, statusParams);
    }

    private int getNavBarHeight() {
        int resourceId = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        return resourceId > 0 ? getResources().getDimensionPixelSize(resourceId) : 0;
    }

    private int getStatusBarHeight() {
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        return resourceId > 0 ? getResources().getDimensionPixelSize(resourceId) : 0;
    }

    private void removeAllViews() {
        if (filterView != null) { try { wm.removeView(filterView); } catch (Exception ignored) {} filterView = null; }
        if (navView != null) { try { wm.removeView(navView); } catch (Exception ignored) {} navView = null; }
        if (statusView != null) { try { wm.removeView(statusView); } catch (Exception ignored) {} statusView = null; }
    }

    private void updateFilter() {
        int color = getFilterColor();
        if (filterView != null) filterView.setBackgroundColor(color);
        if (navView != null) navView.setBackgroundColor(color);
        if (statusView != null) statusView.setBackgroundColor(color);
    }

    private int getFilterColor() {
        int alpha = (int) (opacity / 100.0f * 255);
        switch (colorMode) {
            case 1: return Color.argb(alpha, 255, 140, 0);
            case 2: return Color.argb(alpha, 0, 100, 255);
            default: return Color.argb(alpha, 0, 0, 0);
        }
    }

    private void registerUpdateReceiver() {
        updateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updateFilter();
                updateNotification();
            }
        };
        IntentFilter filter = new IntentFilter(ACTION_UPDATE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(updateReceiver, filter, RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(updateReceiver, filter);
        }
    }

    private void updateNotification() {
        NotificationManager nm = getSystemService(NotificationManager.class);
        nm.notify(1, buildNotification());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning = false;
        removeAllViews();
        if (updateReceiver != null) {
            try { unregisterReceiver(updateReceiver); } catch (Exception ignored) {}
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "Redup Filter", NotificationManager.IMPORTANCE_LOW);
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }
    }

    private Notification buildNotification() {
        PendingIntent stopPending = PendingIntent.getService(
                this, 0,
                new Intent(this, FilterService.class).setAction(ACTION_STOP),
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        PendingIntent brighterPending = PendingIntent.getService(
                this, 1,
                new Intent(this, FilterService.class).setAction(ACTION_BRIGHTER),
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        PendingIntent darkerPending = PendingIntent.getService(
                this, 2,
                new Intent(this, FilterService.class).setAction(ACTION_DARKER),
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        PendingIntent openPending = PendingIntent.getActivity(
                this, 0,
                new Intent(this, MainActivity.class),
                PendingIntent.FLAG_IMMUTABLE);

        return new Notification.Builder(this, CHANNEL_ID)
                .setContentTitle("Redup — " + opacity + "% gelap")
                .setContentText("Ketuk untuk buka pengaturan")
                .setSmallIcon(android.R.drawable.ic_menu_view)
                .setContentIntent(openPending)
                .addAction(android.R.drawable.arrow_up_float, "Terang", brighterPending)
                .addAction(android.R.drawable.arrow_down_float, "Gelap", darkerPending)
                .addAction(android.R.drawable.ic_delete, "Matikan", stopPending)
                .setOngoing(true)
                .build();
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }
}
