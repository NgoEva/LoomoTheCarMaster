package com.segway.loomo.services;

import com.segway.loomo.MainActivity;
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

/**
 * class to provide the base service and handle checkpoint navigation and obstacle avoidance
 */
public class BaseService extends Service {
    private static final String TAG = "BaseService";

    /**
     * the application context
     */
    private Context context;

    /**
     * base instance
     */
    private Base base;

    /**
     * base service instance
     */
    private static BaseService instance;

    /**
     * listeners for the base service: vls listener, checkpoint listener, obstacle listener
     */
    private StartVLSListener startVlsListener;
    private CheckPointStateListener checkpointListener;
    private ObstacleStateChangedListener obstacleStateChangedListener;

    private boolean firstNavigation;


    /**
     * returns the base instance
     * @return BaseService
     */
    public static BaseService getInstance() {
        Log.d(TAG, "get base instance");
        if (instance == null) {
            throw new IllegalStateException("BaseService instance not initialized yet");
        }
        return instance;
    }

    /**
     * constructor to initialize the base service
     * @param context
     */
    public BaseService(Context context) {
        Log.d(TAG, "base service initiated");
        this.context = context;
        instance = this;
        this.init();
        this.initListeners();
    }

    /**
     * initialize the base instance, set control mode to navigation and start VLS
     */
    @Override
    public void init() {
        this.base = Base.getInstance();
        this.base.bindService(this.context, new ServiceBinder.BindStateListener() {
            @Override
            public void onBind() {
                Log.d(TAG, "base service bound successfully");
                base.setControlMode(Base.CONTROL_MODE_NAVIGATION);
                base.startVLS(true, true, startVlsListener);

                firstNavigation = true;
            }

            @Override
            public void onUnbind(String reason) {
                Log.d(TAG, "Base bind failed");
            }
        });
    }

    /**
     * initialize the VLS listener, the checkpoint listener and the obstacle listener
     */
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
                String text = "Okay, here we are. I can start with general information about the car or you can ask me a particular question.";
                //MainActivity.getInstance().changeInfoText(text);
                SpeakService.getInstance().speak(text);
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

    /**
     * disconnect the base service
     */
    @Override
    public void disconnect() {
        Log.d(TAG, "unbind base service");
        this.base.stop();
        this.base.stopVLS();
        this.base.unbindService();
    }

    /**
     * reset the original point
     */
    public void resetPosition() {
        Log.d(TAG, "reset original point");
        this.base.cleanOriginalPoint();
        PoseVLS pose2D = this.base.getVLSPose(-1);
        this.base.setOriginalPoint(pose2D);
    }

    /**
     * start navigation to a spot and reset original point if necessary
     * @param spot
     */
    public void startNavigation(Spot spot) {
        Log.i(TAG, "start navigation");
        this.setupNavigationVLS();
        Log.i(TAG, "Moving to: " + spot.getX_coordinate() + spot.getY_coordinate());
        this.base.addCheckPoint(spot.getX_coordinate(), spot.getY_coordinate());
    }

    /**
     * set On-Checkpoint-Arrived-Listener, set up Obstacle Avoidance
     */
    private void setupNavigationVLS() {
        Log.d(TAG, "setup navigation VLS");
        Log.d(TAG, Boolean.toString(this.base.isVLSStarted()));

        if (this.firstNavigation) {
            Log.d(TAG, "first navigation setup");
            resetPosition();
            firstNavigation = false;

            Log.d(TAG, "set on checkpoint arrived listener");
            this.base.setOnCheckPointArrivedListener(this.checkpointListener);

            // setting up Obstacle Avoidance
            Log.d(TAG, "Obstacle Avoidance is enabled:  " + this.base.isUltrasonicObstacleAvoidanceEnabled() +
                    ". Distance: " + this.base.getUltrasonicObstacleAvoidanceDistance());
            this.base.setUltrasonicObstacleAvoidanceEnabled(true);
            this.base.setUltrasonicObstacleAvoidanceDistance(0.5f);
            this.base.setObstacleStateChangeListener(obstacleStateChangedListener);

            Log.d(TAG, "Setting up Obstacle Avoidance:  " + this.base.isUltrasonicObstacleAvoidanceEnabled() +
                    ". Distance: " + this.base.getUltrasonicObstacleAvoidanceDistance());
        }
    }
}
