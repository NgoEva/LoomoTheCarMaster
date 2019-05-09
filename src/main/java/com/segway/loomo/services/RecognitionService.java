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
import com.segway.robot.sdk.voice.recognition.WakeupListener;
import com.segway.robot.sdk.voice.recognition.WakeupResult;

import java.util.Arrays;

public class RecognitionService {
    private static final String TAG = "RecognitionService";
    private final Context context;

    private Recognizer recognizer;
    public static RecognitionService instance;

    private WakeupListener wakeupListener;
    private RecognitionListener recognitionListener;

    private GrammarConstraint welcomeSlotGrammar;
    private GrammarConstraint interestSlotGrammar;
    private GrammarConstraint guidanceSlotGrammar;

    private Spot spot1 = new Spot(-1.0f, 1.0f);
    private Spot spot2 = new Spot(0f, 1.0f);
    private Spot spot3 = new Spot(1.0f, 1.0f);

    private boolean wakeup = false;
    private boolean interested = false;
    private boolean resetPosition = true;
    private int selectedCar = 0;

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
        init();
        instance = this;
    }

    public void restartService() {
        Log.d(TAG, "restart service");
        init();
    }

    private void init() {
        this.recognizer = Recognizer.getInstance();
        initListeners();
        this.recognizer.bindService(this.context, new ServiceBinder.BindStateListener() {
            @Override
            public void onBind() {
                Log.d(TAG, "recognizer service bound successfully");
                try {
                    initControlGrammer();
                    RecognitionService.getInstance().startListening();
                } catch (VoiceException e) {
                    Log.e(TAG, "Exception: ", e);
                }
            }

            @Override
            public void onUnbind(String reason) {
                Log.d(TAG, "recognizer service unbound");
            }
        });
    }

    private void initListeners(){
        wakeupListener = new WakeupListener() {
            @Override
            public void onStandby() {
                Log.i(TAG, "in Standby");
            }

            @Override
            public void onWakeupResult(WakeupResult wakeupResult) {
                Log.i(TAG, "got wakeup result: " + wakeupResult);
                wakeup = true;
                SpeakService.getInstance().speak("Hello, I am Loomo, the Car Master. Are you interested in getting some information about the cars?");
                boolean timeout = SpeakService.getInstance().waitForSpeakFinish(5000);
                if (timeout) {
                    try {
                        recognizer.addGrammarConstraint(interestSlotGrammar);
                        recognizer.startRecognitionMode(recognitionListener);
                    }
                    catch (VoiceException e) {
                        Log.w(TAG, "Exception: ", e);
                    }
                }
            }

            @Override
            public void onWakeupError(String error) {
                Log.i(TAG, "got wakeup error: " + error);
            }
        };

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

                if (wakeup && !interested) {
                    if (result.contains("yes") || result.contains("yeah") || result.contains("of course") || result.contains("sure")) {
                        Log.d(TAG, "customer is interested");
                        try {
                            interested = true;
                            SpeakService.getInstance().speak("Which car should I show you?");
                            recognizer.removeGrammarConstraint(interestSlotGrammar);
                            recognizer.addGrammarConstraint(guidanceSlotGrammar);
                        } catch (VoiceException e) {
                            Log.e(TAG, "Exception: ", e);
                        }
                    }
                }

                if (wakeup && interested) {
                    if (resetPosition) BaseService.getInstance().resetPosition();
                    if (result.contains("car one") || result.contains("first car")) {
                        Log.d(TAG, "selected car: 1");
                        try {
                            selectedCar = 1;
                            SpeakService.getInstance().speak("Alright. Follow me. I will guide you to car one.");
                            boolean timeout = SpeakService.getInstance().waitForSpeakFinish(3000);
                            if (timeout) {
                                BaseService.getInstance().startNavigation(spot1);
                                resetPosition = false;
                            }
                        }
                        catch (VoiceException e){
                            Log.e(TAG, "Exception: ", e);
                        }
                        return true;
                    }
                    else if (result.contains("car two") || result.contains("second car")) {
                        Log.d(TAG, "selected car: 2");
                        try {
                            selectedCar = 2;
                            SpeakService.getInstance().speak("Alright. Follow me. I will guide you to car two.");
                            boolean timeout = SpeakService.getInstance().waitForSpeakFinish(3000);
                            if (timeout) {
                                BaseService.getInstance().startNavigation(spot2);
                                resetPosition = false;
                            }
                        }
                        catch (VoiceException e){
                            Log.e(TAG, "Exception: ", e);
                        }
                        return true;
                    }
                    else if (result.contains("car three") || result.contains("third car")) {
                        Log.d(TAG, "selected car: 3");
                        try {
                            selectedCar = 3;
                            SpeakService.getInstance().speak("Alright. Follow me. I will guide you to car three.");
                            boolean timeout = SpeakService.getInstance().waitForSpeakFinish(3000);
                            if (timeout) {
                                BaseService.getInstance().startNavigation(spot3);
                                resetPosition = false;
                            }
                        }
                        catch (VoiceException e){
                            Log.e(TAG, "Exception: ", e);
                        }
                        return true;
                    }
                    return true;
                }
                return true;
            }

            @Override
            public boolean onRecognitionError(String error) {
                Log.i(TAG, "recognition error: " + error);
                return false;
            }
        };
    }

    private void initControlGrammer() {
        Log.d(TAG, "init control grammar");

        welcomeSlotGrammar = new GrammarConstraint();
        welcomeSlotGrammar.setName("welcome");
        welcomeSlotGrammar.addSlot(new Slot("greeting", false, Arrays.asList("hello", "hey", "hi", "okay")));
        welcomeSlotGrammar.addSlot(new Slot("loomo", false, Arrays.asList("loomo", "robot")));

        interestSlotGrammar = new GrammarConstraint();
        interestSlotGrammar.setName("interest");
        interestSlotGrammar.addSlot(new Slot("positive", false, Arrays.asList("yes", "yeah", "sure", "of course")));
        interestSlotGrammar.addSlot(new Slot("negative", false, Arrays.asList("no", "nah")));

        guidanceSlotGrammar = new GrammarConstraint();
        guidanceSlotGrammar.setName("guidance");
        guidanceSlotGrammar.addSlot(new Slot("command", false, Arrays.asList("bring me", "guide me", "show me", "take me")));
        guidanceSlotGrammar.addSlot(new Slot("to", true, Arrays.asList("to")));
        guidanceSlotGrammar.addSlot(new Slot("car", false, Arrays.asList("car one", "car two", "car three")));
    }

    public void startListening() {
        Log.d(TAG, "start listening");
        try {
            recognizer.startWakeupMode(wakeupListener);
        } catch (VoiceException e) {
            Log.e(TAG, "got VoiceException", e);
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
        this.recognizer.unbindService();
    }
}
