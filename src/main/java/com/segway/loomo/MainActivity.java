package com.segway.loomo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.segway.loomo.objects.Category;
import com.segway.loomo.objects.Customer;
import com.segway.loomo.objects.MapObject;
import com.segway.loomo.services.BaseService;
import com.segway.loomo.services.RecognitionService;
import com.segway.loomo.services.SpeakService;;

import java.util.ArrayList;

/**
 * main activity class to provide the start screen and initialize the services
 */
public class MainActivity extends Activity implements View.OnClickListener {
    private static String TAG = "MainActivity";

    /**
     * the application context
     */
    private static Context context;

    /**
     * main activity instance
     */
    private static MainActivity instance;

    /**
     * loomo services
     */
    private BaseService baseService;
    private RecognitionService recognitionService;
    private SpeakService speakService;

    /**
     * request handler
     */
    private RequestHandler requestHandler;

    /**
     * layout elements of the start screen
     */
    private Button start;
    private Button stop;
    public static TextView info;

    /**
     * available categories requested from the database
     */
    public ArrayList<Category> categories;

    /**
     * available map objects in the car showroom including the cars and their respective spot positions
     */
    public ArrayList<MapObject> cars;

    /**
     * the customer
     */
    public Customer customer;

    /**
     * initialize main activity
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "initialize main activity");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        instance = this;
        this.context = getApplicationContext();
        this.initServices();
        this.initLayoutElements();
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

    /**
     * initialize base, recognition, speak service and request handler
     */
    private void initServices(){
        Log.d(TAG, "init services");
        this.baseService = new BaseService(MainActivity.getInstance().getApplicationContext());
        this.recognitionService = new RecognitionService(MainActivity.getInstance().getApplicationContext());
        this.speakService = new SpeakService(MainActivity.getInstance().getApplicationContext());
        this.requestHandler = new RequestHandler(MainActivity.getInstance().getApplicationContext());
    }

    /**
     * initialize layout elements like buttons and text views
     */
    private void initLayoutElements() {
        Log.d(TAG, "init buttons");
        this.start = findViewById(R.id.startButton);
        this.stop = findViewById(R.id.stopButton);
        this.info = findViewById(R.id.infoText);

        this.start.setOnClickListener(this);
        this.stop.setOnClickListener(this);

        this.start.setEnabled(true);
        this.stop.setEnabled(false);
    }

    /**
     * switching the screen from start screen to contact form screen
     */
    public void switchScreen() {
        Intent nextScreen = new Intent(MainActivity.getInstance().getApplicationContext(), ContactFormActivity.class);
        startActivity(nextScreen);
    }

    /**
     * disconnect base, recognition, speak service and request handler and destroy application
     */
    protected void onDestroy() {
        Log.d(TAG, "destroy");
        super.onDestroy();
        this.baseService.disconnect();
        this.recognitionService.disconnect();
        this.speakService.disconnect();
        this.requestHandler.cancelPendingRequests();
    }

    /**
     * on-click function for start- and stop-button
     * @param view
     */
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.startButton:
                Log.d(TAG, "start-button clicked");
                this.start.setEnabled(false);
                this.stop.setEnabled(true);
                this.getData();
                this.speakService.speak("Hello, I am Loomo, the Car Master. Do you want to know something about our cars?");
                this.recognitionService.startListening();

                break;
            case R.id.stopButton:
                Log.d(TAG, "start-button clicked");
                this.restart();

                break;
        }
    }

    /**
     * function to change the info text on the start screen to the string s
     * @param s
     */
    public void changeInfoText(String s) {
        Log.d(TAG, "change info text");
        TextView info = findViewById(R.id.infoText);
        info.setText(s);
    }

    /**
     * get data (categories and showroom map) of the cms database
     */
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

    /**
     * send email to salesman
     */
    public void sendMail(){
        Log.d(TAG, "starting mail method");
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    GMailSender sender = new GMailSender("loomo.email@gmail.com",
                            "loomo@MBC");
                    sender.sendMail("Customer Request", "Hello, a customer has told Loomo, the Car Master, that he/sha wants to receive " +
                                    "more information from a personal salesman. Please go to Loomo to serve the corresponding custome.",
                            "loomo.email@gmail.com", "thomas.lehenberger@gmail.com");
                } catch (Exception e) {
                    Log.e("SendMail", e.getMessage(), e);
                }
            }

        }).start();
    }

    /**
     * restart the service by disconnecting from services, stop listening, cancel requests and reset everything else to null
     */
    public void restart() {
        this.stop.setEnabled(false);
        this.start.setEnabled(true);
        this.baseService.disconnect();
        this.speakService.disconnect();
        this.recognitionService.stopListening();
        this.recognitionService.disconnect();
        this.requestHandler.cancelPendingRequests();

        this.baseService = null;
        this.recognitionService = null;
        this.speakService = null;
        this.requestHandler = null;

        this.categories = null;
        this.cars = null;
        this.customer = null;
    }
}
















