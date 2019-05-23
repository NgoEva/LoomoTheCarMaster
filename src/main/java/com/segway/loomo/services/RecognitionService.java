package com.segway.loomo.services;

import android.content.Context;
import android.util.Log;

import com.segway.loomo.objects.Spot;
import com.segway.robot.sdk.base.bind.ServiceBinder;
import com.segway.robot.sdk.voice.Recognizer;
import com.segway.robot.sdk.voice.VoiceException;
import com.segway.robot.sdk.voice.grammar.GrammarConstraint;
import com.segway.robot.sdk.voice.grammar.Slot;
import com.segway.robot.sdk.voice.recognition.RecognitionListener;
import com.segway.robot.sdk.voice.recognition.RecognitionResult;

import java.util.Arrays;

public class RecognitionService extends Service {
    private static final String TAG = "RecognitionService";
    private final Context context;

    private Recognizer recognizer;
    private static RecognitionService instance;

    private RecognitionListener recognitionListener;

    private GrammarConstraint interestSlotGrammar;
    private GrammarConstraint guidanceSlotGrammar;

    private Spot spot1 = new Spot(-1.0f, 1.0f);
    private Spot spot2 = new Spot(0f, 1.0f);
    private Spot spot3 = new Spot(1.0f, 1.0f);

    private boolean resetPosition;

    private boolean interested = false;

    public static RecognitionService getInstance() {
        Log.d(TAG, "get recognizer instance");
        if (instance == null) {
            throw new IllegalStateException("RecognitionService instance not initialized yet");
        }
        return instance;
    }

    public RecognitionService(Context context) {
        Log.d(TAG, "recognition service initiated");
        this.context = context;
        this.init();
        this.initListeners();
        instance = this;
    }

    @Override
    public void init() {
        recognizer = Recognizer.getInstance();
        recognizer.bindService(this.context, new ServiceBinder.BindStateListener() {
            @Override
            public void onBind() {
                Log.d(TAG, "recognizer service bound successfully");
                initControlGrammer();
            }

            @Override
            public void onUnbind(String reason) {
                Log.d(TAG, "recognizer service unbound");
            }
        });
    }

    public void initListeners(){

        recognitionListener = new RecognitionListener() {
            @Override
            public void onRecognitionStart() {
                Log.i(TAG, "recognition started");
            }

            @Override
            public boolean onRecognitionResult(RecognitionResult recognitionResult) {
                //show the recognition result and recognition result confidence.
                Log.d(TAG, "recognition result: " + recognitionResult.getRecognitionResult() +
                        ", confidence:" + recognitionResult.getConfidence());
                String result = recognitionResult.getRecognitionResult();

                if(!interested) {
                    if (result.contains("yes") || result.contains("yeah") || result.contains("of course") || result.contains("sure")) {
                        Log.d(TAG, "customer is interested");
                        interested = true;
                        try {
                            SpeakService.getInstance().speak("Which car should I show you?");
                            recognizer.removeGrammarConstraint(interestSlotGrammar);
                            recognizer.addGrammarConstraint(guidanceSlotGrammar);
                            return true;
                        } catch (VoiceException e) {
                            Log.e(TAG, "Exception: ", e);
                        }
                        return true;
                    }
                    return true;
                }

                if(interested) {
                    if (result.contains("bring me to car one") || result.contains("guide me to car one") || result.contains("show me car one")) {
                        BaseService.getInstance().resetPosition();
                        Log.d(TAG, "selected car: 1");
                        SpeakService.getInstance().speak("Alright. Follow me. I will guide you to car one.");
                        BaseService.getInstance().startNavigation(resetPosition, spot1);
                        resetPosition = false;
                        return true;
                    } else if (result.contains("bring me to car two")) {
                        Log.d(TAG, "selected car: 2");
                        SpeakService.getInstance().speak("Alright. Follow me. I will guide you to car two.");
                        BaseService.getInstance().startNavigation(resetPosition, spot2);
                        resetPosition = false;
                        return true;
                    } else if (result.contains("bring me to car three")) {
                        Log.d(TAG, "selected car: 3");
                        SpeakService.getInstance().speak("Alright. Follow me. I will guide you to car three.");
                        BaseService.getInstance().startNavigation(resetPosition, spot3);
                        resetPosition = false;
                        return true;
                    }
                    return true;
                }
                return true;
            }

            @Override
            public boolean onRecognitionError(String error) {
                Log.i(TAG, "recognition error: " + error);
                return true;
            }
        };
    }

    private void initControlGrammer() {
        Log.d(TAG, "init control grammar");

        interestSlotGrammar = new GrammarConstraint();
        interestSlotGrammar.setName("interest");
        interestSlotGrammar.addSlot(new Slot("positive", false, Arrays.asList("yes", "yeah", "sure", "of course")));

        guidanceSlotGrammar = new GrammarConstraint();
        guidanceSlotGrammar.setName("guidance");
        guidanceSlotGrammar.addSlot(new Slot("command", false, Arrays.asList("bring me", "guide me", "show me", "take me")));
        guidanceSlotGrammar.addSlot(new Slot("to", true, Arrays.asList("to")));
        guidanceSlotGrammar.addSlot(new Slot("car", false, Arrays.asList("car one", "car two", "car three")));
    }

    public void startListening() {
        Log.d(TAG, "start listening");
        SpeakService.getInstance().speak("Hello, I am Loomo, the Car Master. Do you want to know something about our cars?");
        try {
            recognizer.addGrammarConstraint(interestSlotGrammar);
            recognizer.startRecognitionMode(recognitionListener);
        }
        catch (VoiceException e) {
            Log.w(TAG, "Exception: ", e);
        }
    }

    public void stopListening() {
        Log.d(TAG, "stop listening");
        try {
            recognizer.stopRecognition();
        } catch (VoiceException e) {
            Log.e(TAG, "got VoiceException", e);
        }

    }

    public void disconnect() {
        Log.d(TAG, "unbind recognizer service");
        recognizer.unbindService();
    }
}
