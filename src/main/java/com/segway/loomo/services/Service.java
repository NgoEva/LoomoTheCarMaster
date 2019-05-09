package com.segway.loomo.services;

interface Service {

    void init();

    void initListeners();

    void restartService();

    void disconnect();

}
