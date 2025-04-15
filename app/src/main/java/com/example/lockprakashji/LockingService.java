package com.example.lockprakashji;



import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class LockingService extends Service {

    private Handler handler;
    private Runnable checkTask;
    private static final String CHANNEL_ID = "focus_mode_channel";
    private static final String TAG = "LockingService";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createNotificationChannel();
        Notification notification = new NotificationCompat.Builder(this, "focus_mode_channel")
                .setContentTitle("Focus Mode")
                .setContentText("Focus session is running...")
                .setSmallIcon(R.drawable.ic_launcher_foreground) // replace with your icon
                .build();
        startForeground(1, notification);

        handler = new Handler();
        checkTask = new Runnable() {
            @Override
            public void run() {
                String currentApp = getForegroundApp();
                if (currentApp != null && !MainActivity.allowedPackages.contains(currentApp)) {
                    Log.d(TAG, "Blocking app: " + currentApp);
                    Intent lockIntent = new Intent(LockingService.this, LockScreenActivity.class);
                    lockIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(lockIntent);
                }

                handler.postDelayed(this, 1000); // Repeat every 1 second
            }
        };
        handler.post(checkTask);

        return START_STICKY;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Focus Mode",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private Notification getNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Focus Mode Active")
                .setContentText("Distractions are blocked")
                .setSmallIcon(android.R.drawable.ic_lock_lock)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }

    private String getForegroundApp() {
        UsageStatsManager usm = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
        long time = System.currentTimeMillis();
        List<UsageStats> appList = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 10000, time);
        if (appList != null && !appList.isEmpty()) {
            SortedMap<Long, UsageStats> sortedMap = new TreeMap<>();
            for (UsageStats usageStats : appList) {
                sortedMap.put(usageStats.getLastTimeUsed(), usageStats);
            }
            if (!sortedMap.isEmpty()) {
                return sortedMap.get(sortedMap.lastKey()).getPackageName();
            }
        }
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (handler != null) {
            handler.removeCallbacks(checkTask);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
