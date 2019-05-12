package com.segway.loomo.services;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import com.segway.robot.sdk.base.bind.ServiceBinder;
import com.segway.robot.sdk.voice.Speaker;
import com.segway.robot.sdk.voice.VoiceException;
import com.segway.robot.sdk.voice.tts.TtsListener;

public class SpeakService extends Service {

    private static final String TAG = "SpeakService";
    private final Context context;

    private Speaker speaker;
    private static SpeakService instance;

    private TtsListener ttsListener;

    public static SpeakService getInstance() {
        Log.d(TAG, "get speaker instance");
        if (instance == null) {
            throw new IllegalStateException("SpeakService instance not initialized yet");
        }
        return instance;
    }

    public SpeakService(Context context) {
        Log.d(TAG, "speaker service initiated");
        this.context = context;
        init();
        instance = this;
    }

    public void init() {
        this.speaker = Speaker.getInstance();
        speaker.bindService(context, new ServiceBinder.BindStateListener() {
            @Override
            public void onBind() {
                Log.d(TAG, "speaker service bound successfully");
            }

            @Override
            public void onUnbind(String reason) {
                Log.d(TAG, "speaker service unbound");
            }
        });
    }

    public void initListeners() {
        ttsListener = new TtsListener() {
            @Override
            public void onSpeechStarted(String s) {
                //s is speech content, callback this method when speech is starting.
                Log.d(TAG, "onSpeechStarted() called with: s = [" + s + "]");
            }

            @Override
            public void onSpeechFinished(String s) {
                //s is speech content, callback this method when speech is finish.
                Log.d(TAG, "onSpeechFinished() called with: s = [" + s + "]");
            }

            @Override
            public void onSpeechError(String s, String s1) {
                //s is speech content, callback this method when speech occurs error.
                Log.d(TAG, "onSpeechError() called with: s = [" + s + "], s1 = [" + s1 + "]");
            }
        };
    }

    public void speak(String text) {
        try {
            this.speaker.speak(text, ttsListener);
            boolean timeout = this.speaker.waitForSpeakFinish(5000);
        }
        catch(VoiceException e) {
            e.printStackTrace();
        }
    }

    public void disconnect() {
        Log.d(TAG, "unbind speaker service");
        this.speaker.unbindService();
    }
}
