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
    private Context context;

    private Speaker speaker;
    private static SpeakService instance;

    private TtsListener ttsListener;


    /**
     * returns the speak instance
     * @return SpeakService
     */
    public static SpeakService getInstance() {
        Log.d(TAG, "get speaker instance");
        if (instance == null) {
            throw new IllegalStateException("SpeakService instance not initialized yet");
        }
        return instance;
    }

    /**
     * constructor to initialize the speak service
     * @param context
     */
    public SpeakService(Context context) {
        Log.d(TAG, "speaker service initiated");
        this.context = context;
        instance = this;
        this.init();
        this.initListeners();
    }

    /**
     * initialize speaker service
     */
    @Override
    public void init() {
        this.speaker = Speaker.getInstance();
        this.speaker.bindService(this.context, new ServiceBinder.BindStateListener() {
            @Override
            public void onBind() {
                Log.d(TAG, "speaker service bound successfully");
                try {
                    speaker.setVolume(30);
                }
                catch (VoiceException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onUnbind(String reason) {
                Log.d(TAG, "speaker service unbound");
            }
        });
    }

    /**
     * initialize speaker listener
     */
    public void initListeners() {
        this.ttsListener = new TtsListener() {
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

    /**
     * speak function
     * @param text
     */
    public void speak(String text) {
        try {
            this.speaker.speak(text, this.ttsListener);
            Log.d(TAG, "before speak ");
            this.speaker.waitForSpeakFinish(10000);
            Log.d(TAG, "after speak ");
        }
        catch(VoiceException e) {
            e.printStackTrace();
        }
    }

    /**
     * disconnect the speaker service
     */
    public void disconnect() {
        Log.d(TAG, "unbind speaker service");
        this.speaker.unbindService();
    }
}
