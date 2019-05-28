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
        start = (Button) findViewById(R.id.start);
        start.setOnClickListener(this);
        /*send = (Button) findViewById(R.id.send);
        send.setOnClickListener(this);*/
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
                start.setEnabled(false);
                this.getData();
                speakService.speak("Hello, I am Loomo, the Car Master. Do you want to know something about our cars?");
                recognitionService.startListening();
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
                RequestHandler.getInstance().makeRequest(RequestHandler.Collection.CATEGORIES);

                // get map objects from database which include the car and its respective spot
                RequestHandler.getInstance().makeRequest(RequestHandler.Collection.SHOWROOM_MAP);
            }
        }.start();
    }
}
