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

public class RecognitionService extends Service {
    private static final String TAG = "RecognitionService";
    private final Context context;

    private Recognizer recognizer;
    private static RecognitionService instance;

    private WakeupListener wakeupListener;
    private RecognitionListener recognitionListener;

    private GrammarConstraint welcomeSlotGrammar;
    private GrammarConstraint interestSlotGrammar;
    private GrammarConstraint guidanceSlotGrammar;
    private GrammarConstraint categorySlotGrammar;
    private GrammarConstraint modelSlotGrammar;
    private GrammarConstraint generalInformationSlotGrammar;
    private GrammarConstraint questionInformationSlotGrammar;
    private GrammarConstraint noMoreInformationSlotGrammar;
    private GrammarConstraint humanRequestSlotGrammar;
    private GrammarConstraint additionalConsultationSlotGrammar;
    private GrammarConstraint collectInformationSlotGrammar;





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

    public void init() {
        this.recognizer = Recognizer.getInstance();
        initListeners();
        this.recognizer.bindService(this.context, new ServiceBinder.BindStateListener() {
            @Override
            public void onBind() {
                Log.d(TAG, "recognizer service bound successfully");
                initControlGrammer();
                RecognitionService.getInstance().startListening();
            }

            @Override
            public void onUnbind(String reason) {
                Log.d(TAG, "recognizer service unbound");
            }
        });
    }

    public void initListeners(){
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
                try {
                    recognizer.addGrammarConstraint(interestSlotGrammar);
                    recognizer.startRecognitionMode(recognitionListener);
                }
                catch (VoiceException e) {
                    Log.w(TAG, "Exception: ", e);
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
                        selectedCar = 1;
                        SpeakService.getInstance().speak("Alright. Follow me. I will guide you to car one.");
                        BaseService.getInstance().startNavigation(spot1);
                        resetPosition = false;
                        return true;
                    }
                    else if (result.contains("car two") || result.contains("second car")) {
                        Log.d(TAG, "selected car: 2");
                        selectedCar = 2;
                        SpeakService.getInstance().speak("Alright. Follow me. I will guide you to car two.");
                        BaseService.getInstance().startNavigation(spot2);
                        resetPosition = false;
                        return true;
                    }
                    else if (result.contains("car three") || result.contains("third car")) {
                        Log.d(TAG, "selected car: 3");
                        selectedCar = 3;
                        SpeakService.getInstance().speak("Alright. Follow me. I will guide you to car three.");
                        BaseService.getInstance().startNavigation(spot3);
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
                return false;
            }
        };
    }

    private void initControlGrammer() {
        Log.d(TAG, "init control grammar");

        Slot interest = new Slot( "interest ", false, Arrays.asList("Show me", "I would like to see", "Take me", "Guide me", "Can you show me"));
        Slot preposition = new Slot("preposition", true, Arrays.asList("to", "a"));
        Slot answerPos = new Slot("answer positive", false, Arrays.asList("yes", "yeah", "sure", "of course", "yes please"));
        Slot answerNeg = new Slot("answer negative", false, Arrays.asList("no", "nah", "nope", "no thanks"));
        Slot article = new Slot("article", true, Arrays.asList("the", "this", "that"));
        Slot modelName = new Slot("model name", false, Arrays.asList("car", "model", "A-Class", "B-Class", "C-Class", "CLA", "CLS", "S-Class", "E-Class", "G-Class", "GLA", "GLC","GLE","V-Class"));


        welcomeSlotGrammar = new GrammarConstraint();
        welcomeSlotGrammar.setName("welcome");
        welcomeSlotGrammar.addSlot(new Slot("greeting", false, Arrays.asList("hello", "hey", "hi", "okay")));
        welcomeSlotGrammar.addSlot(new Slot("loomo", false, Arrays.asList("loomo", "robot")));

        interestSlotGrammar = new GrammarConstraint();
        interestSlotGrammar.setName("interest");
        interestSlotGrammar.addSlot(answerPos);
        interestSlotGrammar.addSlot(answerNeg);

       /* guidanceSlotGrammar = new GrammarConstraint();
        *guidanceSlotGrammar.setName("guidance");
       * guidanceSlotGrammar.addSlot(new Slot("command", false, Arrays.asList("bring me", "guide me", "show me", "take me")));
        *guidanceSlotGrammar.addSlot(new Slot("to", true, Arrays.asList("to")));
        *guidanceSlotGrammar.addSlot(new Slot("car", false, Arrays.asList("car one", "car two", "car three")));
        */

        categorySlotGrammar = new GrammarConstraint();
        categorySlotGrammar.setName("category");
        categorySlotGrammar.addSlot(interest);
        categorySlotGrammar.addSlot(preposition);
        categorySlotGrammar.addSlot(new Slot("category", false, Arrays.asList("Hatchback", "Coupé", "Saloon", "Cabriolet", "SUV", "MPV" )));

        modelSlotGrammar = new GrammarConstraint();
        modelSlotGrammar.setName("model");
        modelSlotGrammar.addSlot(interest);
        modelSlotGrammar.addSlot(preposition);
        modelSlotGrammar.addSlot(new Slot("model", false, Arrays.asList("A-Class", "B-Class", "C-Class", "CLA", "CLS", "S-Class", "E-Class", "G-Class", "GLA", "GLC","GLE","V-Class" )));

        generalInformationSlotGrammar = new GrammarConstraint();
        generalInformationSlotGrammar.setName("general information");
        generalInformationSlotGrammar.addSlot(new Slot("command", false, Arrays.asList("Tell me general information about", "Tell me something about")));
        generalInformationSlotGrammar.addSlot(article);
        generalInformationSlotGrammar.addSlot(modelName);

        questionInformationSlotGrammar = new GrammarConstraint();
        questionInformationSlotGrammar.setName("question information");
        questionInformationSlotGrammar.addSlot(new Slot("question", false, Arrays.asList("What is the")));
        questionInformationSlotGrammar.addSlot(new Slot("information type", false, Arrays.asList("name of", "colour of", "seat number of", "power of", "maximum speed of", "transmission of", "fuel type of", "maximum fuel consupmtion of", "price of" ));
        questionInformationSlotGrammar.addSlot(new Slot("car", false, Arrays.asList("this car", "that car")));

        noMoreInformationSlotGrammar = new GrammarConstraint();
        noMoreInformationSlotGrammar.setName("more questions");
        noMoreInformationSlotGrammar.addSlot(answerNeg);

        humanRequestSlotGrammar = new GrammarConstraint();
        humanRequestSlotGrammar.setName("human request");
        humanRequestSlotGrammar.addSlot(answerPos);
        humanRequestSlotGrammar.addSlot(answerNeg);

        additionalConsultationSlotGrammar = new GrammarConstraint();
        additionalConsultationSlotGrammar.setName("additional consultation");
        additionalConsultationSlotGrammar.addSlot(new Slot("wanting", false, Arrays.asList("I want to", "I would like to")));
        additionalConsultationSlotGrammar.addSlot(new Slot("verb", false, Arrays.asList("receive", "do", "have", "make")));
        additionalConsultationSlotGrammar.addSlot(new Slot("consultation", false, Arrays.asList("an offer", "a phone call", "a test drive")));

        collectInformationSlotGrammar = new GrammarConstraint();
        collectInformationSlotGrammar.setName("collect customer information");
        collectInformationSlotGrammar.addSlot(answerPos);
        collectInformationSlotGrammar.addSlot(answerNeg);











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
