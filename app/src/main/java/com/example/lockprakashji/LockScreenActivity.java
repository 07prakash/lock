package com.example.lockprakashji;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.TextView;


import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.concurrent.TimeUnit;

public class LockScreenActivity extends AppCompatActivity {

    private TextView textViewTimer;
    private CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_lock_screen);

        textViewTimer = findViewById(R.id.textViewTimer);

        // Handle window insets for a full-screen experience
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Prevent dismissing the activity by tapping outside
        setFinishOnTouchOutside(false);

        // Retrieve the duration from the intent, defaulting to 0 if not provided
        long durationMillis = getIntent().getLongExtra("duration", 0);

        // Start the countdown timer if a valid duration is provided
        if (durationMillis > 0) {
            startCountdown(durationMillis);
        } else {
            // Handle case where duration is not provided (e.g., display a default message)
            textViewTimer.setText("No session duration provided.");
        }
    }

    private void startCountdown(long durationMillis) {
        countDownTimer = new CountDownTimer(durationMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                String timeLeft = String.format("%02d:%02d",
                        TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished),
                        TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60);
                textViewTimer.setText(timeLeft);
            }

            @Override
            public void onFinish() {
                textViewTimer.setText("Session Over");
            }
        }.start();
    }

    @Override
    public void onBackPressed() {  // Disable back button
    }
}