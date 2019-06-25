package com.segway.loomo;


import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.segway.loomo.services.DataMapper;
import com.segway.loomo.services.SpeakService;

/**
 * contact form activity class to provide the contact form screen and functionality
 */
public class ContactFormActivity extends Activity implements View.OnClickListener {
    private static String TAG = "ContactFormActivity";

    /**
     * send-button
     */
    private Button send;

    /**
     * edit texts of the contact form to get the input information of the customer
     */
    private EditText firstName;
    private EditText lastName;
    private EditText email;
    private EditText address;
    private EditText houseNumber;
    private EditText phoneNumber;
    private EditText zipCode;
    private EditText city;

    /**
     * initialize contact form activity
     * @param savedInstanceState
     */
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "initialize contact form activity");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contact_form);
        
        this.initButtons();
    }

    /**
     * initialize send button
     */
    private void initButtons() {
        this.send = findViewById(R.id.send);
        this.send.setOnClickListener(this);
    }

    /**
     * get the customer data from the contact form
     */
    public void getCustomerData () {
        firstName = findViewById(R.id.edit_first_name);
        lastName = findViewById(R.id.edit_last_name);
        email = findViewById(R.id.edit_email);
        address = findViewById(R.id.edit_address);
        houseNumber = findViewById(R.id.edit_house_number);
        phoneNumber = findViewById(R.id.edit_phone_number);
        zipCode = findViewById(R.id.edit_zip_code);
        city = findViewById(R.id.edit_city);
    }

    /**
     * check if all required data has been entered
     * @return
     */
    private boolean checkInput() {
        getCustomerData();
        boolean inputCorrect = true;

        if (firstName.getText().toString().trim().equals("") || lastName.getText().toString().trim().equals("") || phoneNumber.getText().toString().trim().equals("")
                || email.getText().toString().trim().equals("")) {
            send.setEnabled(true);
            inputCorrect = false;
            if (firstName.getText().toString().trim().equals("")) {
                firstName.setError("First name is required!");
                firstName.setHint("Please enter your first name");
            }
            if (lastName.getText().toString().trim().equals("")) {
                lastName.setError("Last name is required!");
                lastName.setHint("Please enter your last name");
            }
            if (email.getText().toString().trim().equals("")) {
                email.setError("E-Mail is required!");
                email.setHint("Please enter your e-mail.");
            }
            if (phoneNumber.getText().toString().trim().equals("")) {
                phoneNumber.setError("Phone Number is required!");
                phoneNumber.setHint("Please enter your phone number");
            }
        }
        return inputCorrect;
    }

    /**
     * sending and saving customer data to the cms when send-button is clicked
     * @param view
     */
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.send:
                Log.d(TAG, "send-button clicked");
                send.setEnabled(false);
                if(checkInput()) {
                    Toast.makeText(this, "Your data has been sent! We will contact you as soon as possible. Goodbye and have a nice day!", Toast.LENGTH_LONG).show();
                    SpeakService.getInstance().speak("Thank you! We will contact you as soon as possible. Goodbye and have a nice day!");
                    DataMapper.mapCustomer(firstName.getText().toString(), lastName.getText().toString(), email.getText().toString(),
                            address.getText().toString(), houseNumber.getText().toString(), phoneNumber.getText().toString(),
                            zipCode.getText().toString(), city.getText().toString());
                    RequestHandler.getInstance().sendCustomerData(MainActivity.getInstance().customer);
                }
        }
    }
}


