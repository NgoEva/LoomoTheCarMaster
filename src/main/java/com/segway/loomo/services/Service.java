package com.segway.loomo.services;

import android.util.Log;

/**
 * abstract class which lists the common functions for the robotic services
 */
public abstract class Service {

    /**
     * abstract method to initialize the service instances and bind to the service
     */
    public void init() {};

    /**
     * abstract method to initialize the service listeners
     */
    public void initListeners() {};

    /**
     * method to restart the services
     */
    public void restartService() {
        Log.d(null, "restart service");
        this.init();
    };

    /**
     * abstract method to disconnect from the services
     */
    public void disconnect() {};

}
