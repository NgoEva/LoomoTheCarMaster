package com.segway.loomo.services;

import com.segway.loomo.objects.Spot;

import android.content.Context;
import android.util.Log;

import com.segway.robot.algo.Pose2D;
import com.segway.robot.algo.PoseVLS;
import com.segway.robot.algo.minicontroller.CheckPoint;
import com.segway.robot.algo.minicontroller.CheckPointStateListener;
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
        init();
        instance = this;
    }

    @Override
    public void init() {
        base = Base.getInstance();
        base.bindService(context, new ServiceBinder.BindStateListener() {
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
        startVlsListener = new StartVLSListener() {
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
        checkpointListener = new CheckPointStateListener() {
            public void onCheckPointArrived(CheckPoint checkPoint, final Pose2D realPose, boolean isLast) {
                Log.i(TAG, "Arrived to checkpoint: " + checkPoint);
            }

            @Override
            public void onCheckPointMiss(CheckPoint checkPoint, Pose2D realPose, boolean isLast, int reason) {
                Log.i(TAG, "Missed checkpoint: " + checkPoint);
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
        this.base.cleanOriginalPoint();
        PoseVLS pose2D = base.getVLSPose(-1);
        this.base.setOriginalPoint(pose2D);
    }

    public void startNavigation(Spot spot) {
        Log.i(TAG, "start navigation");
        setupNavigationVLS();
        Log.i(TAG, "Moving to: " + spot.toString());
        this.base.addCheckPoint(spot.getX_coordinate(), spot.getY_coordinate());
    }

    private void setupNavigationVLS() {
        base.startVLS(true, true, startVlsListener);
        base.setOnCheckPointArrivedListener(checkpointListener);
    }
}
