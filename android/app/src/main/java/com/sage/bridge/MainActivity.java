package com.sage.bridge;

import android.os.Bundle;
import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        registerPlugin(SAGEPlugin.class);
        super.onCreate(savedInstanceState);
    }
}