package com.sage.bridge;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;

public class SAGEGodModeService extends AccessibilityService {
    private static SAGEGodModeService instance;
    private static volatile boolean isTaskAborted = false;

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        instance = this;
    }

    public static void abortTask() { isTaskAborted = true; }

    public static void performSystemAction(int action) {
        if (instance != null) instance.performGlobalAction(action);
    }

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {}
    @Override public void onInterrupt() { isTaskAborted = true; }
}