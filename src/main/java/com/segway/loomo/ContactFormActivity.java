package com.segway.loomo;


import android.app.Activity;
import android.nfc.Tag;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;


public class ContactFormActivity extends Activity {

    private Button send;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setContentView(R.layout.contact_form);
        
       this.initButtons();




}

    private void initButtons() {

        this.send = (Button) findViewById(R.id.send);
        this.send.setOnClickListener((View.OnClickListener) this);
        this.send = (Button) findViewById(R.id.send);
        this.send.setOnClickListener((View.OnClickListener) this);
    }



    
    }


