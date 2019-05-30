package com.segway.loomo.services;

import com.segway.loomo.objects.Spot;

import android.content.Context;
import android.util.Log;

import com.segway.robot.algo.Pose2D;
import com.segway.robot.algo.PoseVLS;
import com.segway.robot.algo.minicontroller.CheckPoint;
import com.segway.robot.algo.minicontroller.CheckPointStateListener;
import com.segway.robot.algo.minicontroller.ObstacleStateChangedListener;
import com.segway.robot.sdk.base.bind.ServiceBinder;
import com.segway.robot.sdk.locomotion.sbv.Base;
import com.segway.robot.sdk.locomotion.sbv.StartVLSListener;

public class BaseService extends Service {
    private static final String TAG = "BaseService";
    private final Context context;

    private Base base;
    private static BaseService instance;

    private StartVLSListener startVlsListener;
    private CheckPointStateListener checkpointListener;
    private ObstacleStateChangedListener obstacleStateChangedListener;

    public static BaseService getInstance() {
        Log.d(TAG, "get base instance");
        if (instance == null) {
            throw new IllegalStateException("BaseService instance not initialized yet");
        }
        return instance;
    }

    public BaseService(Context context) {
        Log.d(TAG, "base service initiated");
        this.context = context;
        this.init();
        this.initListeners();
        instance = this;
    }

    @Override
    public void init() {
        this.base = Base.getInstance();
        this.base.bindService(this.context, new ServiceBinder.BindStateListener() {
            @Override
            public void onBind() {
                Log.d(TAG, "base service bound successfully");
                base.setControlMode(Base.CONTROL_MODE_NAVIGATION);
            }

            @Override
            public void onUnbind(String reason) {
                Log.d(TAG, "Base bind failed");
            }
        });
    }

    @Override
    public void initListeners() {
        this.startVlsListener = new StartVLSListener() {
            @Override
            public void onOpened() {
                Log.i(TAG, "VLSListener started");
                base.setNavigationDataSource(Base.NAVIGATION_SOURCE_TYPE_VLS);
            }

            @Override
            public void onError(String errorMessage) {
                Log.i(TAG, "VLSListener error: " + errorMessage);

            }
        };

        this.checkpointListener = new CheckPointStateListener() {
            public void onCheckPointArrived(CheckPoint checkPoint, final Pose2D realPose, boolean isLast) {
                Log.i(TAG, "Arrived to checkpoint: " + checkPoint);
                SpeakService.getInstance().speak("Okay, here we are. I can start with general information about the car or you can ask me a particular question.");
                RecognitionService.getInstance().startRecognition();
            }

            @Override
            public void onCheckPointMiss(CheckPoint checkPoint, Pose2D realPose, boolean isLast, int reason) {
                Log.i(TAG, "Missed checkpoint: " + checkPoint);
            }
        };

        this.obstacleStateChangedListener  = new ObstacleStateChangedListener() {
            @Override
            public void onObstacleStateChanged(int ObstacleAppearance) {
                Log.i(TAG, "ObstacleStateChanged " + ObstacleAppearance);
            }

        };
    }

    @Override
    public void disconnect() {
        Log.d(TAG, "unbind base service");
        this.base.unbindService();
    }


    public void resetPosition() {
        Log.d(TAG, "reset original point");
        this.setupNavigationVLS();
        this.base.cleanOriginalPoint();
        PoseVLS pose2D = this.base.getVLSPose(-1);
        this.base.setOriginalPoint(pose2D);
    }

    public void startNavigation(boolean resetPosition, Spot spot) {
        Log.i(TAG, "start navigation");
        if (resetPosition) this.resetPosition();
        this.setupNavigationVLS();
        Log.i(TAG, "Moving to: " + spot.toString());
        this.base.addCheckPoint(spot.getX_coordinate(), spot.getY_coordinate());
    }

    private void setupNavigationVLS() {
        if (!this.base.isVLSStarted()) {
            this.base.startVLS(true, true, this.startVlsListener);
            this.base.setOnCheckPointArrivedListener(this.checkpointListener);

            // setting up Obstacle Avoidance
            Log.d(TAG, "Obstacle Avoidance is enabled:  " + this.base.isUltrasonicObstacleAvoidanceEnabled() +
                    ". Distance: " + this.base.getUltrasonicObstacleAvoidanceDistance());
            this.base.setUltrasonicObstacleAvoidanceEnabled(true);
            this.base.setUltrasonicObstacleAvoidanceDistance(0.5f);
            this.base.setObstacleStateChangeListener(this.obstacleStateChangedListener);

            Log.d(TAG, "Setting up Obstacle Avoidance:  " + base.isUltrasonicObstacleAvoidanceEnabled() +
                    ". Distance: " + base.getUltrasonicObstacleAvoidanceDistance());
        }
    }
}
