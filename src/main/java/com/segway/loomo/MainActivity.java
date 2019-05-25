package com.segway.loomo;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.segway.loomo.services.BaseService;
import com.segway.loomo.services.RecognitionService;
import com.segway.loomo.services.SpeakService;

public class MainActivity extends Activity implements View.OnClickListener {
    private static String TAG = "MainActivity";

    private BaseService baseService;
    private RecognitionService recognitionService;
    private SpeakService speakService;
    private RequestHandler requestHandler;

    private Button start;

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
                SpeakService.getInstance().speak("Hello, I am Loomo, the Car Master. Do you want to know something about our cars?");
                RecognitionService.getInstance().startListening();
        }
    }
}
