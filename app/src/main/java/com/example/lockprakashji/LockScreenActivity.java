package com.example.lockprakashji;

import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.TextView;


import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class LockScreenActivity extends AppCompatActivity {

    private TextView textViewTimer;
    private TextView lockedAppTextView;

    private CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_lock_screen);

        initializeViews();

        // Handle window insets for a full-screen experience
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Prevent dismissing the activity by tapping outside
        setFinishOnTouchOutside(false);

        processIntentData();
    }

    private void initializeViews() {
        textViewTimer = findViewById(R.id.textViewTimer);
        lockedAppTextView = findViewById(R.id.lockedAppTextView);
    }

    private void processIntentData() {
        String blockedAppPackage = getIntent().getStringExtra("blocked_app");
        long durationMillis = getIntent().getLongExtra("duration", 0);

        if (blockedAppPackage != null && durationMillis > 0) {
            displayLockedAppMessage(blockedAppPackage);
            startCountdown(durationMillis);
        } else {
            handleInvalidData();
        }
    }

    private void displayLockedAppMessage(String blockedAppPackage) {
        try {
            PackageManager packageManager = getPackageManager();
            String appName = packageManager.getApplicationLabel(packageManager.getApplicationInfo(blockedAppPackage, PackageManager.GET_META_DATA)).toString();
            String message = String.format(Locale.getDefault(), "⚠️ %s is locked during focus mode.", appName);
            lockedAppTextView.setText(message);
        } catch (PackageManager.NameNotFoundException e) {
            lockedAppTextView.setText(String.format(Locale.getDefault(), "⚠️ %s is locked during focus mode.", blockedAppPackage));
        }
    }

    private void handleInvalidData() {
        lockedAppTextView.setText("Invalid session data.");
        textViewTimer.setText("--:--");
    }

    private void startCountdown(final long durationMillis) {
        updateTimerText(durationMillis); // Initial display

        countDownTimer = new CountDownTimer(durationMillis, 1000) { // Tick every 1 second
        @Override
        public void onTick(long millisUntilFinished) {
            updateTimerText(millisUntilFinished);
        }

        @Override
        public void onFinish() {
            endSession();
        }
    }.start();
    }

    private void updateTimerText(long millisUntilFinished) {
        if (millisUntilFinished <= 0) {
            textViewTimer.setText("00:00");
        } else {
            long minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished);
            long seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60;
            String timeLeft = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
            textViewTimer.setText(timeLeft);
        }
    }

    private void endSession() {
        textViewTimer.setText("Session Over");
        finish(); // Close the activity when the timer finishes
    }

    @Override
    public void onBackPressed() {  // Disable back button
    }
}