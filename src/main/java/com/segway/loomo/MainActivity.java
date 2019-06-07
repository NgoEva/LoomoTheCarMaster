package com.segway.loomo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.segway.loomo.objects.Category;
import com.segway.loomo.objects.Customer;
import com.segway.loomo.objects.MapObject;
import com.segway.loomo.services.BaseService;
import com.segway.loomo.services.RecognitionService;
import com.segway.loomo.services.SpeakService;

import java.util.ArrayList;


public class MainActivity extends Activity implements View.OnClickListener {
    public static final String EXTRA_MESSAGE = "com.loomothecarmaster.MESSAGE";
    private static String TAG = "MainActivity";

    private BaseService baseService;
    private RecognitionService recognitionService;
    private SpeakService speakService;
    private RequestHandler requestHandler;

    private Button start;
    private Button send;

    public static ArrayList<Category> categories;
    public static ArrayList<MapObject> cars;
    public static Customer customer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "initialize main activity");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setContentView(R.layout.contact_form);

        this.initServices();
        this.initButtons();

        Intent intent =getIntent();
        String message = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);

        TextView contact_form = findViewById(R.id.contact_form);
        TextView first_name = findViewById(R.id.first_name);
        TextView last_name = findViewById(R.id.last_name);
        TextView email = findViewById(R.id.email);
        TextView address = findViewById(R.id.address);
        TextView house_number = findViewById(R.id.house_number);
        TextView phone_number = findViewById(R.id.phone_number);
        TextView zip_code = findViewById(R.id.zip_code);
        TextView city_stadt = findViewById(R.id.city_stadt);


        sendMail();
    }



    public void sendCustomerData (View view) {

    Intent intent = new Intent(this, ContactFormActivity.class);
        EditText firstName = (EditText) findViewById(R.id.firstName);
        EditText lastName =(EditText)findViewById(R.id.lastName);
        EditText eMail = (EditText)findViewById(R.id.eMail);
        EditText addresse = (EditText) findViewById(R.id.addresse);
        EditText houseNumber =(EditText)findViewById(R.id.houseNumber);
        EditText zipCode = (EditText) findViewById(R.id.zipCode);
        EditText city = (EditText) findViewById(R.id.city);
        EditText phoneNumber = (EditText) findViewById(R.id.phoneNumber);

        String message1 = firstName.getText().toString();
        String message2 = lastName.getText().toString();
        String message3 = eMail.getText().toString();
        String message4 = addresse.getText().toString();
        String message5 = houseNumber.getText().toString();
        String message6 = zipCode.getText().toString();
        String message7 = city.getText().toString();
        String message8 = phoneNumber.getText().toString();

        intent.putExtra(EXTRA_MESSAGE, message1);
        intent.putExtra(EXTRA_MESSAGE, message2);
        intent.putExtra(EXTRA_MESSAGE, message3);
        intent.putExtra(EXTRA_MESSAGE, message4);
        intent.putExtra(EXTRA_MESSAGE, message5);
        intent.putExtra(EXTRA_MESSAGE, message6);
        intent.putExtra(EXTRA_MESSAGE, message7);
        intent.putExtra(EXTRA_MESSAGE, message8);

        startActivity(intent);

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
        this.send = (Button) findViewById(R.id.send);
        this.send.setOnClickListener(this);
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
            case R.id.send:
                Log.d(TAG, "send-button clicked");
                send.setEnabled(false);
                speakService.speak("Thank you! We will contact you as soon as possible. Goodbye and have a nice day!");
                mapCustomer();
                requestHandler.sendCustomerData(customer);
        }
    }

    //contact_form R.setString(R.string.interest, customer.getInterest()); needs to be prefilled with customers interest

    public void mapCustomer() {
        customer.setFirstName(getString(R.string.first_name));
        customer.setLastName(getString(R.string.last_name));
        customer.setPhoneNumber(getString(R.string.phone_number));
        customer.setInterest(getString(R.string.interest));
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
















