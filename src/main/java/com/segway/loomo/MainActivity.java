package com.segway.loomo;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.Volley;
import com.segway.loomo.services.BaseService;
import com.segway.loomo.services.RecognitionService;
import com.segway.loomo.services.Service;
import com.segway.loomo.services.SpeakService;

public class MainActivity extends Activity {
    private static String TAG = "MainActivity";
    private static MainActivity instance;
    private static RequestQueue requestQueue;


    private BaseService loomoBaseService;
    private RecognitionService loomoRegocnitionService;
    private SpeakService loomoSpeakService;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "initialize main activity");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        instance = this;
        //initServices();
    }

    public static RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(instance.getApplicationContext());
        }
        return requestQueue;
    }

    public static void addToRequestQueue(JsonRequest request) {
        VolleyLog.d("Adding request to queue: %s", request.getUrl());
        request.setTag(TAG);
        getRequestQueue().add(request);
    }

    public static void cancelPendingRequests() {
        if(requestQueue != null) {
            requestQueue.cancelAll(TAG);
        }
    }

    private void initServices(){
        this.loomoBaseService = new BaseService(getRequestQueue());
        this.loomoRegocnitionService = new RecognitionService(getApplicationContext());
        this.loomoSpeakService = new SpeakService(getApplicationContext());

    }

    protected void onDestroy(){
        super.onDestroy();
        this.loomoBaseService.disconnect();
        this.loomoRegocnitionService.disconnect();
        this.loomoSpeakService.disconnect();

    }



}
