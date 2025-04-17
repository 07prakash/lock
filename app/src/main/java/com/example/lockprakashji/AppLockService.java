package com.example.lockprakashji;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;
import android.content.Intent;
import android.util.Log;

import java.util.List;

public class AppLockService extends AccessibilityService {

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null || event.getPackageName() == null) return;

        String foregroundApp = event.getPackageName().toString();

        if (!MainActivity.allowedPackages.contains(foregroundApp)) {
            Intent intent = new Intent(this, LockScreenActivity.class);
            intent.putExtra("blocked_app", foregroundApp);
            intent.putExtra("duration", MainActivity.focusDurationMillis); // Assuming this is where the duration is stored
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    }

    @Override
    public void onInterrupt() {
        // Required override but not needed here
    }
}
