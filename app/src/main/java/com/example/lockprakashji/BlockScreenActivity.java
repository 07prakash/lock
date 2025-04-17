package com.example.lockprakashji;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import com.example.lockprakashji.R;

public class BlockScreenActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_block_screen);

        Button btnClose = findViewById(R.id.btnExit);
        btnClose.setOnClickListener(v -> {
            // You can add logic to prompt for payment here.
            finish();
        });
    }

    @Override
    public void onBackPressed() {
        // Prevent back button exit
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish(); // Close the activity when it loses focus
    }
}
