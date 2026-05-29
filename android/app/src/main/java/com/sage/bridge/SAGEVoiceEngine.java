package com.sage.bridge;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import java.util.ArrayList;

public class SAGEVoiceEngine implements RecognitionListener {
    private SpeechRecognizer speechRecognizer;
    private Intent speechIntent;
    private Context context;
    private boolean isListening = false;
    private Handler mainHandler;

    public SAGEVoiceEngine(Context context) {
        this.context = context;
        this.mainHandler = new Handler(Looper.getMainLooper());
        initRecognizer();
    }

    private void initRecognizer() {
        if (speechRecognizer != null) speechRecognizer.destroy();
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
        speechRecognizer.setRecognitionListener(this);

        speechIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechIntent.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true);
    }

    public void start() {
        isListening = true;
        mainHandler.post(() -> {
            try { speechRecognizer.startListening(speechIntent); }
            catch (Exception e) { initRecognizer(); restartSafe(); }
        });
    }

    public void stop() {
        isListening = false;
        mainHandler.post(() -> { if (speechRecognizer != null) speechRecognizer.stopListening(); });
    }

    private void restartSafe() {
        if (!isListening) return;
        mainHandler.postDelayed(this::start, 250); // Breathing delay prevents crashes
    }

    @Override
    public void onResults(Bundle results) {
        ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        if (matches != null && !matches.isEmpty()) {
            SAGEPlugin.onVoiceRecognized(matches.get(0));
        }
        restartSafe();
    }

    @Override
    public void onError(int error) {
        if (error == SpeechRecognizer.ERROR_CLIENT || error == SpeechRecognizer.ERROR_RECOGNIZER_BUSY) {
            initRecognizer();
        }
        restartSafe();
    }

    @Override public void onReadyForSpeech(Bundle p) {}
    @Override public void onBeginningOfSpeech() {}
    @Override public void onRmsChanged(float r) {}
    @Override public void onBufferReceived(byte[] b) {}
    @Override public void onEndOfSpeech() {}
    @Override public void onPartialResults(Bundle p) {}
    @Override public void onEvent(int e, Bundle p) {}
}