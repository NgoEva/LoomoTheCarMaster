package com.segway.loomo.services;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import com.segway.robot.sdk.base.bind.ServiceBinder;
import com.segway.robot.sdk.voice.Speaker;

public class SpeakService implements Service, TextToSpeech.OnInitListener {

    private TextToSpeech tts;

    @Override
    public void init(int status) {

    }

    private static final String TAG = "SpeakService";
    private Speaker speaker;
    private Context context;

    public static SpeakService instance;

    public static SpeakService getInstance() {
        if (instance == null) {
            throw new IllegalStateException("SpeakService instance not initialized yet");
        }
        return instance;
    }
    public SpeakService(Context context) {
        this.context = context;
        init();
        this.instance = this;

        tts = new TextToSpeech(context,this); }


    public void speak(String sentence) {
        tts.speak(sentence, TextToSpeech.QUEUE_FLUSH, null); }


    // muss hier noch mal das gleiche hin wie bei RegonitionService? Sorry war da ein bisschen lost
    @Override
    public void initListeners() {

    }

    @Override
    public void restartService()  {
        init();
    }


    private void init() {

        speaker = Speaker.getInstance();
        speaker.bindService(context, new ServiceBinder.BindStateListener() {
            @Override
            public void onBind() {
                Log.d(TAG, "Speaker onBind");
            }

            @Override
            public void onUnbind(String reason) {
                Log.d(TAG, "Speaker onUnbind");
            }
        });
    }

    @Override
    public void disconnect() { this.speaker.unbindService();

    }
}
