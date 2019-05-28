package com.segway.loomo.services;

import android.util.Log;

/**
 * abstract class which lists the common functions for the robotic services
 */
public abstract class Service {
    public void init() {};

    public void initListeners() {};

    public void restartService() {
        Log.d(null, "restart service");
        this.init();
    };

    public void disconnect() {};

}
