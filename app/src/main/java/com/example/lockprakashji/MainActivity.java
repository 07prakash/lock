package com.example.lockprakashji;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AppOpsManager;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

// Implement the listener interface from the adapter

import android.widget.TimePicker;


public class MainActivity extends AppCompatActivity implements AppListAdapter.OnAppSelectedListener {
    public static List<String> allowedPackages = new ArrayList<>();


    private static final String TAG = "MainActivity";

    private RecyclerView recyclerViewApps;
    private TextView textViewDuration;
    private Button buttonStartFocus;
    private AppListAdapter appListAdapter; // Use the adapter
    private List<AppInfo> installedApps; // Use the AppInfo model
    private List<String> selectedAppPackages = new ArrayList<>(); // Keep track of selected package names

    private CheckBox checkBoxStrictMode;

    public long focusDurationMillis = 0;
    private CountDownTimer focusTimer;
    private boolean isFocusSessionActive = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        recyclerViewApps = findViewById(R.id.recyclerViewApps);
        textViewDuration = findViewById(R.id.textViewDuration);
        buttonStartFocus = findViewById(R.id.buttonStartFocus);
        checkBoxStrictMode = findViewById(R.id.checkBoxStrictMode);

        requestUsageStatsPermission();
        if (!checkUsageStatsPermission()) {
            startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
            Toast.makeText(this, "Please grant Usage Access permission", Toast.LENGTH_LONG).show();
        }

        // Initialize the list
        installedApps = new ArrayList<>();

        setupTimePicker();
        setupRecyclerView(appListAdapter); // Setup adapter here
        loadInstalledApps(installedApps); // Load data

        buttonStartFocus.setOnClickListener(v -> {
            if (!isFocusSessionActive) {
                startFocusSession();
            } else {
                Toast.makeText(this, "Focus session already active!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupTimePicker() {
        textViewDuration.setOnClickListener(v -> {
            // Get current time
            Calendar currentTime = Calendar.getInstance();
            int hour = currentTime.get(Calendar.HOUR_OF_DAY);
            int minute = currentTime.get(Calendar.MINUTE);

            // Create a TimePickerDialog
            TimePickerDialog timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker view, int selectedHour, int selectedMinute) {
                    // Calculate duration in milliseconds from the selected time
                    Calendar selectedTime = Calendar.getInstance();
                    selectedTime.set(Calendar.HOUR_OF_DAY, selectedHour);
                    selectedTime.set(Calendar.MINUTE, selectedMinute);
                    selectedTime.set(Calendar.SECOND, 0); // Ensure seconds are set to 0
                    long selectedMillis = selectedTime.getTimeInMillis();
                    long currentMillis = Calendar.getInstance().getTimeInMillis();
                    focusDurationMillis = selectedMillis - currentMillis;
                    if (focusDurationMillis <= 0) {
                        focusDurationMillis += TimeUnit.DAYS.toMillis(1); // Add 24 hours if the time is in the past
                    }
                    // Update the TextView with the selected time
                    textViewDuration.setText(String.format("%02d:%02d", selectedHour, selectedMinute));
                }
            }, hour, minute, false); // 'false' for 12-hour format, 'true' for 24-hour
            timePickerDialog.show();
        });
    }

    private void setupRecyclerView(AppListAdapter appListAdapter) {
        recyclerViewApps.setLayoutManager(new LinearLayoutManager(this));
        // Pass the list and the listener (this activity)
        this.appListAdapter = new AppListAdapter(installedApps, this);
        recyclerViewApps.setAdapter(this.appListAdapter);
         // Toast.makeText(this, "RecyclerView setup (Adapter pending)", Toast.LENGTH_SHORT).show(); // Remove placeholder toast
    }

    @SuppressLint("NotifyDataSetChanged") // Suppress warning for now, refine later if needed
    private void loadInstalledApps(List<AppInfo> installedApps) {
        Log.d(TAG, "Loading installed apps...");
        PackageManager pm = getPackageManager();
        this.installedApps.clear(); // Clear previous list

        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> apps = pm.queryIntentActivities(intent, 0);

        if (apps != null) {
            for (ResolveInfo resolveInfo : apps) {
                String appName = resolveInfo.loadLabel(pm).toString();
                String packageName = resolveInfo.activityInfo.packageName;
                // Exclude self
                if (packageName.equals(getPackageName())) {
                    continue;
                }
                try {
                    AppInfo appInfo = new AppInfo(
                            appName,
                            packageName,
                            resolveInfo.loadIcon(pm)
                    );
                    this.installedApps.add(appInfo);
                } catch (Exception e) {
                    Log.e(TAG, "Error loading info for package: " + packageName, e);
                }
            }
            // Sort apps alphabetically by name
            Collections.sort(this.installedApps, Comparator.comparing(AppInfo::getName, String.CASE_INSENSITIVE_ORDER));

        } else {
            Log.e(TAG, "Could not retrieve installed apps.");
            Toast.makeText(this, "Error loading apps", Toast.LENGTH_SHORT).show();
        }

        // Notify the adapter that the data has changed
        appListAdapter.notifyDataSetChanged();
        Log.d(TAG, "App loading complete. Found " + this.installedApps.size() + " apps.");
       // Toast.makeText(this, "App loading logic pending", Toast.LENGTH_SHORT).show(); // Remove placeholder toast
    }

    // Implementation of the listener method from AppListAdapter.OnAppSelectedListener
    @Override
    public void onAppSelected(AppInfo appInfo, boolean isSelected) {
        String packageName = appInfo.getPackageName();
        if (isSelected) {
            if (!selectedAppPackages.contains(packageName)) {
                selectedAppPackages.add(packageName);
            }
        } else {
            selectedAppPackages.remove(packageName);
        }
        // We update the AppInfo object directly in the adapter now,
        // but we still need selectedAppPackages for the locking logic.
        Log.d(TAG, "Selection changed: " + packageName + ", selected: " + isSelected);
        Log.d(TAG, "Currently selected packages: " + selectedAppPackages.toString());
    }


    private void startFocusSession() {
        // Refresh the selected packages list just in case (based on AppInfo state)
        selectedAppPackages.clear();
        for (AppInfo app : installedApps) {
            if (app.isSelected()) {
                selectedAppPackages.add(app.getPackageName());
            }
        }

        if (selectedAppPackages.isEmpty()) {
            Toast.makeText(this, "Please select at least one app to allow", Toast.LENGTH_SHORT).show();
            return;
        }

        if (focusDurationMillis <= 0) {
            Toast.makeText(this, "Please select a valid time", Toast.LENGTH_SHORT).show();
            return;
        }

        allowedPackages.clear();
        allowedPackages.addAll(selectedAppPackages);

        isFocusSessionActive = true;

        buttonStartFocus.setText("Focus Session Active");
        buttonStartFocus.setEnabled(false); // Disable start button during session
        numberPickerDuration.setEnabled(false); // Disable picker during session
        recyclerViewApps.setEnabled(false); // Disable RecyclerView interaction
        // Make RecyclerView visually disabled (optional, adjust alpha)
        recyclerViewApps.setAlpha(0.5f);


        Log.d(TAG, "Starting focus session for " + focusDurationMillis + " ms. Allowed apps: " + selectedAppPackages);
        Toast.makeText(this, "Focus session started! Timer running.", Toast.LENGTH_LONG).show();

        // Pass duration and strict mode to the service
        Intent serviceIntent = new Intent(this, LockingService.class);
        serviceIntent.putExtra("duration", focusDurationMillis);
        serviceIntent.putExtra("strictMode", checkBoxStrictMode.isChecked()); // Pass strict mode state
        ContextCompat.startForegroundService(this, serviceIntent);
        focusTimer = new CountDownTimer(focusDurationMillis, 1000) {
             @Override
             public void onTick(long millisUntilFinished) {
                 long minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished);
                 long seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60;
                 Log.d(TAG, "Time remaining: " + minutes + "m " + seconds + "s");
                 // Optional: Update UI with remaining time
             }

             @Override
             public void onFinish() {
                 Log.d(TAG, "Focus session finished!");
                 endFocusSession();
                 Toast.makeText(MainActivity.this, "Focus session finished! Apps unlocked.", Toast.LENGTH_LONG).show();
             }
         }.start();


    }
//
    private void endFocusSession() {
        isFocusSessionActive = false;
        if (focusTimer != null) {
            focusTimer.cancel();
            focusTimer = null;
        }
        buttonStartFocus.setText("Start Focus Session");
        buttonStartFocus.setEnabled(true);
        numberPickerDuration.setEnabled(true);
        recyclerViewApps.setEnabled(true); // Re-enable RecyclerView interaction
        recyclerViewApps.setAlpha(1.0f); // Restore visual appearance

        stopService(new Intent(this, LockingService.class));

        Log.d(TAG, "Placeholder for stopping LockingService");

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Ensure timer is cancelled if activity is destroyed unexpectedly during a session
        if (focusTimer != null) {
            focusTimer.cancel();
        }
        // Consider if the service should continue running even if the activity is destroyed
    }
    private boolean checkUsageStatsPermission() {
        AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }

    private void requestUsageStatsPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!checkUsageStatsPermission()) {
                startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
            }
        }
    }

    // Permissions for Android 13+
    private static final int NOTIFICATION_PERMISSION_CODE = 123; // Unique request code

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Handle permission results if necessary
    }

}