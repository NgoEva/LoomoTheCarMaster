package com.segway.loomo;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import java.util.Arrays;

import com.segway.loomo.objects.Spot;
import com.segway.robot.algo.PoseVLS;
import com.segway.robot.algo.minicontroller.CheckPointStateListener;
import com.segway.robot.algo.minicontroller.CheckPoint;
import com.segway.robot.algo.Pose2D;
import com.segway.robot.sdk.base.bind.ServiceBinder;
import com.segway.robot.sdk.locomotion.sbv.Base;
import com.segway.robot.sdk.locomotion.sbv.StartVLSListener;
import com.segway.robot.sdk.voice.Recognizer;
import com.segway.robot.sdk.voice.Speaker;
import com.segway.robot.sdk.voice.VoiceException;
import com.segway.robot.sdk.voice.grammar.GrammarConstraint;
import com.segway.robot.sdk.voice.grammar.Slot;
import com.segway.robot.sdk.voice.recognition.RecognitionListener;
import com.segway.robot.sdk.voice.recognition.RecognitionResult;
//import com.segway.robot.sdk.voice.recognition.WakeupListener;
//import com.segway.robot.sdk.voice.recognition.WakeupResult;
import com.segway.robot.sdk.voice.tts.TtsListener;

public class MainActivityOld extends Activity implements View.OnClickListener {
    private static final String TAG = "MainActivityOld";

    private Recognizer mRecognizer;
    private Speaker mSpeaker;
    private Base mBase;

//    private WakeupListener mWakeupListener;
    private RecognitionListener mRecognitionListener;
    private TtsListener mTtsListener;
    private GrammarConstraint mThreeSlotGrammar;
    private GrammarConstraint mInterestSlotGrammar;

//    private VLSPoseListener vlsPoseListener;
    private StartVLSListener mStartVLSListener;
    private CheckPointStateListener mCheckpointListener;

    private Button mStart;

    private Spot spot1 = new Spot(-1.0f, 1.0f);
    private Spot spot2 = new Spot(0f, 1.0f);
    private Spot spot3 = new Spot(1.0f, 1.0f);

    private boolean interested = false;
    private boolean resetPosition = true;
    private int selectedCar = 0;
    private boolean timeout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "get instances");
        mRecognizer = Recognizer.getInstance();
        mSpeaker = Speaker.getInstance();
        mBase = Base.getInstance();

        initButtons();
        init();
    }

    @Override
    protected void onRestart() {
        Log.d(TAG, "restart");
        super.onRestart();
    }

    // init UI.
    private void initButtons() {
        Log.d(TAG, "init buttons");
        mStart = (Button) findViewById(R.id.start);
        mStart.setOnClickListener(this);
    }

    //init listeners.
    private void init() {

        Log.d(TAG, "init listeners");

        Log.d(TAG, "bind recognizer service");
        mRecognizer.bindService(MainActivityOld.this, new ServiceBinder.BindStateListener() {
            @Override
            public void onBind() {
                Log.d(TAG, "recognizer service bound");
                try {
                    initControlGrammar();
                    mRecognizer.addGrammarConstraint(mInterestSlotGrammar);
                    startListening();
                } catch (VoiceException e) {
                    Log.w(TAG, "Exception: ", e);
                }
            }

            @Override
            public void onUnbind(String s) {
                //speaker service or recognition service unbind, disable function buttons.
                Log.d(TAG, "recognition service onUnbind");
            }
        });

        Log.d(TAG, "bind speaker service");
        mSpeaker.bindService(MainActivityOld.this, new ServiceBinder.BindStateListener() {
            @Override
            public void onBind() {
                Log.d(TAG, "speaker service bound");
                // set the volume of TTS
                try {
                    mSpeaker.setVolume(50);
                } catch (VoiceException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onUnbind(String s) {
                Log.d(TAG, "speaker service onUnbind");
            }
        });

        Log.d(TAG, "bind base service");
        mBase.bindService(MainActivityOld.this, new ServiceBinder.BindStateListener() {
            @Override
            public void onBind() {
                Log.d(TAG, "base service bound");
                // set base control mode
                mBase.setControlMode(Base.CONTROL_MODE_NAVIGATION);
            }

            @Override
            public void onUnbind(String reason) {
                Log.d(TAG, "onUnbind() called with: reason = [" + reason + "]");
            }
        });

   /*     mWakeupListener = new WakeupListener() {
            @Override
            public void onStandby() {
                Log.d(TAG, "onStandby");
            }

            @Override
            public void onWakeupResult(WakeupResult wakeupResult) {
                //show the wakeup result and wakeup angle.
                Log.d(TAG, "wakeup word:" + wakeupResult.getResult() + ", angle " + wakeupResult.getAngle());
            }

            @Override
            public void onWakeupError(String s) {
                //show the wakeup error reason.
                Log.d(TAG, "onWakeupError");
            }
        };*/

        mRecognitionListener = new RecognitionListener() {
            @Override
            public void onRecognitionStart() {
                Log.d(TAG, "onRecognitionStart");
            }

            @Override
            public boolean onRecognitionResult(RecognitionResult recognitionResult) {
                //show the recognition result and recognition result confidence.
                Log.d(TAG, "recognition phase: " + recognitionResult.getRecognitionResult() +
                        ", confidence:" + recognitionResult.getConfidence());
                String result = recognitionResult.getRecognitionResult();

                if (result.contains("yes") || result.contains("yeah") || result.contains("of course") || result.contains("sure")) {
                    Log.d(TAG, "customer is interested");
                    try {
                        interested = true;
                        mSpeaker.speak("Which car should I show you?", mTtsListener);
                        mRecognizer.removeGrammarConstraint(mInterestSlotGrammar);
                        mRecognizer.addGrammarConstraint(mThreeSlotGrammar);
                    } catch (VoiceException e) {
                        Log.e(TAG, "Exception: ", e);
                    }
                }

                if (interested) {
                    if (resetPosition) resetPosition();
                    if (result.contains("car one") || result.contains("first car")) {
                        Log.d(TAG, "selected car: 1");
                        try {
                            selectedCar = 1;
                            mSpeaker.speak("Alright. Follow me. I will guide you to car one.", mTtsListener);
                            timeout = mSpeaker.waitForSpeakFinish(3000);
                            if (timeout) {
                                startNavigation(spot1);
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
                            mSpeaker.speak("Alright. Follow me. I will guide you to car two.", mTtsListener);
                            timeout = mSpeaker.waitForSpeakFinish(3000);
                            if (timeout) {
                                startNavigation(spot2);
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
                            mSpeaker.speak("Alright. Follow me. I will guide you to car three.", mTtsListener);
                            timeout = mSpeaker.waitForSpeakFinish(3000);
                            if (timeout) {
                                startNavigation(spot3);
                                resetPosition = false;
                            }
                        }
                        catch (VoiceException e){
                            Log.e(TAG, "Exception: ", e);
                        }
                        return true;
                    }
                }
                return true;
            }

            @Override
            public boolean onRecognitionError(String s) {
                //show the recognition error reason.
                Log.d(TAG, "onRecognitionError: " + s);
                return false; //to wakeup
            }
        };


        mTtsListener = new TtsListener() {
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

        mStartVLSListener = new StartVLSListener() {
            @Override
            public void onOpened() {
                Log.d(TAG, "onOpened() called");
                // set navigation data source
                mBase.setNavigationDataSource(Base.NAVIGATION_SOURCE_TYPE_VLS);
                //mBase.setVLSPoseListener(vlsPoseListener);
            }

            @Override
            public void onError(String errorMessage) {
                Log.d(TAG, "onError() called with: errorMessage = [" + errorMessage + "]");
            }
        };


       /* vlsPoseListener = new VLSPoseListener() {
            @Override
            public void onVLSPoseUpdate(long timestamp, float pose_x, float pose_y, float pose_theta, float v, float w) {
                Log.d(TAG, "onVLSPoseUpdate() called with: timestamp = [" + timestamp + "], pose_x = [" + pose_x + "], pose_y = [" + pose_y + "], pose_theta = [" + pose_theta + "], v = [" + v + "], w = [" + w + "]");
            }
        };*/

        mCheckpointListener = new CheckPointStateListener() {
            @Override
            public void onCheckPointArrived(CheckPoint checkPoint, final Pose2D realPose, boolean isLast) {
                Log.i(TAG, "Arrived to checkpoint: " + checkPoint);
                try {
                    mSpeaker.speak("This is car" + selectedCar + "It is red, has 5 seats and can reach up to 200 kilometers per hour.", mTtsListener);
                }
                catch (VoiceException e){
                    Log.e(TAG, "Exception: ", e);
                }
            }

            @Override
            public void onCheckPointMiss(CheckPoint checkPoint, Pose2D realPose, boolean isLast, int reason) {
                Log.i(TAG, "Missed checkpoint: " + checkPoint);
            }
        };
    }

    private void startListening() {
        Log.d(TAG, "start listening");
        try {
            mRecognizer.startRecognitionMode(mRecognitionListener);
        } catch (VoiceException e) {
            Log.e(TAG, "Got VoiceException", e);
        }
    }

    private void resetPosition() {
        Log.d(TAG, "reset original point");
        mBase.cleanOriginalPoint();
        PoseVLS poseVLS = mBase.getVLSPose(-1);
        mBase.setOriginalPoint(poseVLS);
    }
    private void startNavigation(Spot spot) {
        Log.d(TAG, "start navigation to" + spot.toString());
        mBase.startVLS(true, true, mStartVLSListener);
        mBase.addCheckPoint(spot.getX_coordinate(), spot.getY_coordinate());
        mBase.setOnCheckPointArrivedListener(mCheckpointListener);
    }

    // init control grammar
    private void initControlGrammar() {
        Log.d(TAG, "init control grammar");

        mInterestSlotGrammar = new GrammarConstraint();
        mInterestSlotGrammar.setName("interest");
        mInterestSlotGrammar.addSlot(new Slot("positive", false, Arrays.asList("yes", "yeah", "sure", "of course")));

        mThreeSlotGrammar = new GrammarConstraint();
        mThreeSlotGrammar.setName("movement");
        mThreeSlotGrammar.addSlot(new Slot("guidance", false, Arrays.asList("bring me", "guide me", "show me", "take me")));
        mThreeSlotGrammar.addSlot(new Slot("to", true, Arrays.asList("to")));
        mThreeSlotGrammar.addSlot(new Slot("car", false, Arrays.asList("car one", "car two", "car three")));

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.start:
                Log.d(TAG, "start-button clicked");
                mStart.setEnabled(false);
                try {
                    Log.d(TAG, "start speak");
                    mSpeaker.speak("hello, are you interested in getting some information about the cars?", mTtsListener);
                    boolean timeout = mSpeaker.waitForSpeakFinish(3000);
                    if (timeout) {
                        Log.d(TAG, "start recognition");
                        mRecognizer.addGrammarConstraint(mInterestSlotGrammar);
                        startListening();
                    }
                } catch (VoiceException e) {
                    Log.w(TAG, "Exception: ", e);
                }
                break;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mRecognizer.unbindService();
        mSpeaker.unbindService();
        mBase.unbindService();
        finish();
    }

    @Override
    protected void onDestroy() {
        if (mRecognizer != null) {
            mRecognizer = null;
        }
        if (mSpeaker != null) {
            mSpeaker = null;
        }
        super.onDestroy();
    }
}
