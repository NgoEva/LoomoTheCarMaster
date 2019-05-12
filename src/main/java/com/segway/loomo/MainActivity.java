package com.segway.loomo;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.Volley;

public class MainActivity extends Activity {
    private static String TAG = "MainActivity";
    private static MainActivity instance;
    private static RequestQueue requestQueue;

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
}
