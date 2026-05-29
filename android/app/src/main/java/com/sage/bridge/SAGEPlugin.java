package com.sage.bridge;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.speech.tts.TextToSpeech;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import java.util.List;
import java.util.Locale;

@CapacitorPlugin(name = "SAGESystem")
public class SAGEPlugin extends Plugin implements TextToSpeech.OnInitListener {
    private static SAGEPlugin instance;
    private TextToSpeech tts;
    private SAGEVoiceEngine voiceEngine;
    private boolean isTtsReady = false;

    @Override
    public void load() {
        super.load();
        instance = this;
        tts = new TextToSpeech(getContext(), this);
        voiceEngine = new SAGEVoiceEngine(getContext());
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            tts.setLanguage(Locale.US);
            isTtsReady = true;
        }
    }

    @PluginMethod
    public void startSAGEVoice(PluginCall call) {
        if (voiceEngine != null) voiceEngine.start();
        call.resolve();
    }

    public static void onVoiceRecognized(String text) {
        if (instance != null) {
            JSObject ret = new JSObject();
            ret.put("command", text);
            instance.notifyListeners("onVoiceCommand", ret);
        }
    }

    @PluginMethod
    public void sendOfflineCommand(PluginCall call) {
        String type = call.getString("type");
        if ("lock_screen".equals(type)) {
            SAGEGodModeService.performSystemAction(SAGEGodModeService.GLOBAL_ACTION_LOCK_SCREEN);
        }
        call.resolve();
    }

    @PluginMethod
    public void abortCurrentTask(PluginCall call) {
        SAGEGodModeService.abortTask();
        if (tts != null) tts.stop();
        call.resolve();
    }

    @PluginMethod
    public void speak(PluginCall call) {
        String text = call.getString("text");
        if (text != null && isTtsReady) tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "SAGE_TTS");
        call.resolve();
    }

    @PluginMethod
    public void openApplication(PluginCall call) {
        String req = call.getString("appName");
        if (req == null) { call.reject("No app name"); return; }
        PackageManager pm = getContext().getPackageManager();
        for (ApplicationInfo app : pm.getInstalledApplications(PackageManager.GET_META_DATA)) {
            if (pm.getApplicationLabel(app).toString().toLowerCase().contains(req.toLowerCase())) {
                Intent i = pm.getLaunchIntentForPackage(app.packageName);
                if (i != null) {
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    getContext().startActivity(i);
                    call.resolve();
                    return;
                }
            }
        }
        call.reject("Not found");
    }

    @PluginMethod
    public void quitSystem(PluginCall call) {
        if (voiceEngine != null) voiceEngine.stop();
        if (tts != null) tts.shutdown();
        if (getActivity() != null) getActivity().finishAffinity();
        call.resolve();
    }
}