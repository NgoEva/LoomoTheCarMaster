package com.segway.loomo;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.segway.robot.algo.PoseVLS;
import com.segway.robot.algo.VLSPoseListener;
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
import com.segway.robot.sdk.voice.recognition.WakeupListener;
import com.segway.robot.sdk.voice.recognition.WakeupResult;
import com.segway.robot.sdk.voice.tts.TtsListener;

import java.util.LinkedList;
import java.util.List;

public class MainActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "MainActivity";
    private ServiceBinder.BindStateListener mRecognitionBindStateListener;
    private ServiceBinder.BindStateListener mSpeakerBindStateListener;
    private ServiceBinder.BindStateListener mBaseBindStateListener;

    private Recognizer mRecognizer;
    private Speaker mSpeaker;
    private Base mBase;

    private WakeupListener mWakeupListener;
    private RecognitionListener mRecognitionListener;
    private TtsListener mTtsListener;
    private GrammarConstraint mThreeSlotGrammar;

    private VLSPoseListener vlsPoseListener;
    private StartVLSListener mStartVLSListener;
    private CheckPointStateListener mCheckpointListener;

    private Button mStart;

    private Spot spot1 = new Spot(-1f, 1f);
    private Spot spot2 = new Spot(0f, 1f);
    private Spot spot3 = new Spot(1f, 1f);

    private Spot spots[] = new Spot[3];

    private boolean wakeUp = false;
    private boolean interested = false;
    private int selectedCar = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecognizer = Recognizer.getInstance();
        mSpeaker = Speaker.getInstance();
        mBase = Base.getInstance();

        initButtons();
        initListeners();

        //bind the recognition service.
        mRecognizer.bindService(MainActivity.this, mRecognitionBindStateListener);

        //bind the speaker service.
        mSpeaker.bindService(MainActivity.this, mSpeakerBindStateListener);

        //bind the base service
        mBase.bindService(MainActivity.this, mBaseBindStateListener);

        spots[0] = spot1;
        spots[1] = spot2;
        spots[2]= spot3;
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    // init UI.
    private void initButtons() {
        mStart = (Button) findViewById(R.id.start);
        mStart.setOnClickListener(this);
    }

    //init listeners.
    private void initListeners() {

        mRecognitionBindStateListener = new ServiceBinder.BindStateListener() {
            @Override
            public void onBind() {
                initControlGrammar();
                startListening();
            }

            @Override
            public void onUnbind(String s) {
                //speaker service or recognition service unbind, disable function buttons.
                Log.d(TAG, "recognition service onUnbind");
            }
        };

        mSpeakerBindStateListener = new ServiceBinder.BindStateListener() {
            @Override
            public void onBind() {
                Log.d(TAG, "speaker service onBind");
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
        };

        mBaseBindStateListener = new ServiceBinder.BindStateListener() {
            @Override
            public void onBind() {
                Log.d(TAG, "onBind() called");
                // set base control mode
                mBase.setControlMode(Base.CONTROL_MODE_NAVIGATION);
                // start VLS
            }

            @Override
            public void onUnbind(String reason) {
                Log.d(TAG, "onUnbind() called with: reason = [" + reason + "]");
            }
        };

        mWakeupListener = new WakeupListener() {
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
        };

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

                if (result.contains("hello") || result.contains("hi")) {
                    try {
                        wakeUp = true;
                        mSpeaker.speak("hello, are you interested in getting some information about the cars?", mTtsListener);
                        mRecognizer.addGrammarConstraint(mThreeSlotGrammar);
                    } catch (VoiceException e) {
                        Log.e(TAG, "Exception: ", e);
                    }
                    //true means continuing to recognition, false means wakeup.
                    return true;
                }

                if (wakeUp && !interested) {
                    if (result.contains("yes") || result.contains("yeah") || result.contains("of course") || result.contains("sure")) {
                        try {
                            interested = true;
                            mSpeaker.speak("Which car should I show you?", mTtsListener);
                        }
                        catch (VoiceException e){
                            Log.e(TAG, "Exception: ", e);
                        }
                        return true;
                    }
                }

                if (wakeUp && interested) {
                    if (result.contains("car 1") || result.contains("first car")) {
                        try {
                            selectedCar = 1;
                            mSpeaker.speak("Alright. Follow me. I will guide you to car 1.", mTtsListener);
                            startNavigation(spot1);
                        }
                        catch (VoiceException e){
                            Log.e(TAG, "Exception: ", e);
                        }
                        return true;
                    }
                    else if (result.contains("car 2") || result.contains("second car")) {
                        try {
                            selectedCar = 2;
                            mSpeaker.speak("Alright. Follow me. I will guide you to car 2.", mTtsListener);
                            startNavigation(spot2);
                        }
                        catch (VoiceException e){
                            Log.e(TAG, "Exception: ", e);
                        }
                        return true;
                    }
                    else if (result.contains("car 3") || result.contains("third car")) {
                        try {
                            selectedCar = 3;
                            mSpeaker.speak("Alright. Follow me. I will guide you to car 3.", mTtsListener);
                            startNavigation(spot3);
                        }
                        catch (VoiceException e){
                            Log.e(TAG, "Exception: ", e);
                        }
                        return true;
                    }
                }
                return false;
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
                mBase.setVLSPoseListener(vlsPoseListener);
            }

            @Override
            public void onError(String errorMessage) {
                Log.d(TAG, "onError() called with: errorMessage = [" + errorMessage + "]");
            }
        };


        vlsPoseListener = new VLSPoseListener() {
            @Override
            public void onVLSPoseUpdate(long timestamp, float pose_x, float pose_y, float pose_theta, float v, float w) {
                Log.d(TAG, "onVLSPoseUpdate() called with: timestamp = [" + timestamp + "], pose_x = [" + pose_x + "], pose_y = [" + pose_y + "], pose_theta = [" + pose_theta + "], v = [" + v + "], w = [" + w + "]");
            }
        };

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
        Log.d(TAG, "startListening");
        try {
            mRecognizer.startWakeupAndRecognition(mWakeupListener, mRecognitionListener);
        } catch (VoiceException e) {
            Log.e(TAG, "Got VoiceException", e);
        }
    }

    private void startNavigation(Spot spot) {
        mBase.startVLS(true, true, mStartVLSListener);
        mBase.cleanOriginalPoint();
        PoseVLS poseVLS = mBase.getVLSPose(-1);
        mBase.setOriginalPoint(poseVLS);
        mBase.addCheckPoint(spot.getX_coordinate(), spot.getY_coordinate());
    }

    // init control grammar
    private void initControlGrammar() {

        Slot guidanceSlot = new Slot("guidance");
        Slot toSlot = new Slot("to");
        Slot carSlot = new Slot("car");
        List<Slot> controlSlotList = new LinkedList<>();
        guidanceSlot.setOptional(false);
        guidanceSlot.addWord("bring me");
        guidanceSlot.addWord("guide me");
        guidanceSlot.addWord("show me");
        guidanceSlot.addWord("take me");
        controlSlotList.add(guidanceSlot);

        toSlot.setOptional(true);
        toSlot.addWord("to");
        controlSlotList.add(toSlot);

        carSlot.setOptional(false);
        carSlot.addWord("car 1");
        carSlot.addWord("car 2");
        carSlot.addWord("car 3");
        controlSlotList.add(carSlot);

        mThreeSlotGrammar = new GrammarConstraint("three slots grammar", controlSlotList);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.start:
                Log.d(TAG, "start-button clicked");
                mStart.setEnabled(false);
                try {
                    Log.d(TAG, "start speak");
                    wakeUp = true;
                    mSpeaker.speak("hello, are you interested in getting some information about the cars?", mTtsListener);
                    mRecognizer.addGrammarConstraint(mThreeSlotGrammar);
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
