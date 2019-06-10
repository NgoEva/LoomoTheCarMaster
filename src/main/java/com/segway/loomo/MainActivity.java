package com.segway.loomo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.segway.loomo.objects.Category;
import com.segway.loomo.objects.Customer;
import com.segway.loomo.objects.MapObject;
import com.segway.loomo.services.BaseService;
import com.segway.loomo.services.RecognitionService;
import com.segway.loomo.services.SpeakService;

import java.util.ArrayList;


public class MainActivity extends Activity implements View.OnClickListener {
    private static String TAG = "MainActivity";

    private static Context context;
    private static MainActivity instance;

    private BaseService baseService;
    private RecognitionService recognitionService;
    private SpeakService speakService;
    private RequestHandler requestHandler;

    private Button start;

    public ArrayList<Category> categories;
    public ArrayList<MapObject> cars;
    public Customer customer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "initialize main activity");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        instance = this;
        this.context = getApplicationContext();
        this.initServices();
        this.initButtons();
    }

    /**
     * returns the main activity instance
     * @return MainActivity
     */
    public static MainActivity getInstance() {
        Log.d(TAG, "get main activity instance");
        if (instance == null) {
            throw new IllegalStateException("MainActivity instance not initialized yet");
        }
        return instance;
    }

    private void initServices(){
        Log.d(TAG, "init services");
        this.baseService = new BaseService(MainActivity.getInstance().getApplicationContext());
        this.recognitionService = new RecognitionService(MainActivity.getInstance().getApplicationContext());
        this.speakService = new SpeakService(MainActivity.getInstance().getApplicationContext());
        this.requestHandler = new RequestHandler(MainActivity.getInstance().getApplicationContext());
    }

    private void initButtons() {
        Log.d(TAG, "init buttons");
        this.start = (Button) findViewById(R.id.start);
        this.start.setOnClickListener(this);
    }

    public void switchScreen() {
        Intent nextScreen = new Intent(MainActivity.getInstance().getApplicationContext(), ContactFormActivity.class);
        startActivity(nextScreen);
    }

    protected void onDestroy() {
        Log.d(TAG, "destroy");
        super.onDestroy();
        this.baseService.disconnect();
        this.recognitionService.disconnect();
        this.speakService.disconnect();
        this.requestHandler.cancelPendingRequests();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.start:
                Log.d(TAG, "start-button clicked");
                this.start.setEnabled(false);
                this.getData();
                this.speakService.speak("Hello, I am Loomo, the Car Master. Do you want to know something about our cars?");
                this.recognitionService.startListening();
                break;
        }
    }

    private void getData() {
        new Thread() {
            @Override
            public void run() {
                // get categories from database
                requestHandler.makeRequest(RequestHandler.Collection.CATEGORIES);

                // get map objects from database which include the car and its respective spot
                requestHandler.makeRequest(RequestHandler.Collection.SHOWROOM_MAP);
            }
        }.start();
    }

    public void sendMail(){
        Log.d(TAG, "starting mail method");
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    GMailSender sender = new GMailSender("loomo.email@gmail.com",
                            "loomo@MBC");
                    sender.sendMail("Customer Request", "Hello, a customer has told Loomo, the Car Master, that he/sha wants to receive " +
                                    "more information from a personal salesman. Please go to Loomo to serve the corresponding customer: " + customer.getFirstName() +
                            customer.getLastName() + ".",
                            "loomo.email@gmail.com", "thomas.lehenberger@gmail.com");
                } catch (Exception e) {
                    Log.e("SendMail", e.getMessage(), e);
                }
            }

        }).start();
    }


}
















