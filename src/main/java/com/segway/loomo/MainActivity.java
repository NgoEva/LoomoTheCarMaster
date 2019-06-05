package com.segway.loomo;

import android.app.Activity;
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

    private BaseService baseService;
    private RecognitionService recognitionService;
    private SpeakService speakService;
    private RequestHandler requestHandler;

    private Button start;
    //private Button send;

    public static ArrayList<Category> categories;
    public static ArrayList<MapObject> cars;
    public static Customer customer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "initialize main activity");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.initServices();
        this.initButtons();

        sendMail();
    }

    private void initServices(){
        Log.d(TAG, "init services");
        this.baseService = new BaseService(getApplicationContext());
        this.recognitionService = new RecognitionService(getApplicationContext());
        this.speakService = new SpeakService(getApplicationContext());
        this.requestHandler = new RequestHandler(getApplicationContext());
    }

    private void initButtons() {
        Log.d(TAG, "init buttons");
        this.start = (Button) findViewById(R.id.start);
        this.start.setOnClickListener(this);
        /*this.send = (Button) findViewById(R.id.send);
        this.send.setOnClickListener(this);*/
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


        sendMail();
        switch (view.getId()) {


            case R.id.start:
                Log.d(TAG, "start-button clicked");
                this.start.setEnabled(false);
                this.getData();
                this.speakService.speak("Hello, I am Loomo, the Car Master. Do you want to know something about our cars?");
                this.recognitionService.startListening();
                break;
            /*case R.id.send:
                Log.d(TAG, "send-button clicked");
                send.setEnabled(false);
                speakService.speak("Thank you! We will contact you as soon as possible. Goodbye and have a nice day!");
                mapCustomer();
                requestHandler.sendCustomerData(customer);*/
        }
    }

    //contact_form R.setString(R.string.interest, customer.getInterest()); needs to be prefilled with customers interest

    /*public void mapCustomer() {
        customer.setFirstName(getString(R.string.first_name));
        customer.setLastName(getString(R.string.last_name));
        customer.setPhoneNumber(getString(R.string.phone_number));
        customer.setInterest(getString(R.string.interest));
    }*/

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

    public static void sendMail(){
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
