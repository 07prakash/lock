package com.example.lockprakashji;




import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.util.List;

public class LockingService extends Service {

    private static final String TAG = "LockingService";
    private static final String CHANNEL_ID = "lock_service_channel";

    private Handler handler;
    private Runnable runnable;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        startForeground(1, buildNotification());

        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                String currentApp = getForegroundApp();
                Log.d(TAG, "Current foreground app: " + currentApp);

                if (currentApp != null && !MainActivity.allowedPackages.contains(currentApp)) {
                    Log.d(TAG, "Blocked app detected: " + currentApp);

                    Intent lockIntent = new Intent(LockingService.this, LockScreenActivity.class);
                    lockIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    long duration = MainActivity.focusDurationMillis; // Get the duration here
                    lockIntent.putExtra("duration", duration);
                    startActivity(lockIntent);
                }

                handler.postDelayed(this, 1000); // check every second
            }
        };

        handler.post(runnable);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Lock Service Channel",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }

    private Notification buildNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Focus Mode Active")
                .setContentText("Locking unapproved apps...")
                .setSmallIcon(R.drawable.ic_launcher_foreground) // Make sure this icon exists
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }

    private String getForegroundApp() {
        UsageStatsManager usm = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
        long time = System.currentTimeMillis();
        List<UsageStats> appList = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000, time);

        if (appList == null || appList.isEmpty()) {
            return null;
        }

        UsageStats recentStats = null;
        for (UsageStats usageStats : appList) {
            if (recentStats == null || usageStats.getLastTimeUsed() > recentStats.getLastTimeUsed()) {
                recentStats = usageStats;
            }
        }

        return recentStats != null ? recentStats.getPackageName() : null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Nothing additional needed here for now
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (handler != null && runnable != null) {
            handler.removeCallbacks(runnable);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
