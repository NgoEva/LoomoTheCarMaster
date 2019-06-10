package com.segway.loomo;

import android.content.Context;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.Volley;
import com.segway.loomo.objects.Customer;
import com.segway.loomo.services.DataMapper;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * class which handles requests to the cms database
 */
public class RequestHandler {
    private static String TAG = "RequestHandler";
    private Context context;

    private static RequestQueue requestQueue;
    private static RequestHandler requestHandler;

    private String url = "https://loomo.exocreations.de/api/collections/get/%s?token=account-1d02ac9dab107851012a327336009c";
    private String body = "{\"populate\":1}";
    private JSONObject jsonRequestBody;

    /**
     * returns the request handler instance
     * @return RecognitionService
     */
    public static RequestHandler getInstance() {
        Log.d(TAG, "get request handler instance");
        if (requestHandler == null) {
            throw new IllegalStateException("request handler instance not initialized yet");
        }
        return requestHandler;
    }

    /**
     * constructor to initialize the request handler
     * @param context
     */
    public RequestHandler(Context context) {
        Log.d(TAG, "request handler initiated");
        this.context = context;
        this.init();
        requestHandler = this;
    }

    /**
     * initialize the request body
     */
    public void init() {
        try {
            this.jsonRequestBody = new JSONObject(this.body);
        }
        catch(JSONException e) {
            Log.w( null, "Exception: ", e);
        }
    }

    /**
     * get the request queue
     */
    public RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(context);
            requestQueue.start();
        }
        return requestQueue;
    }

    /**
     * add request to the request queue
     * @param request
     */
    public void addToRequestQueue(JsonRequest request) {
        VolleyLog.d("Adding request to queue: %s", request.getUrl());
        request.setTag(TAG);
        this.getRequestQueue().add(request);
        requestQueue.start();
    }

    /**
     * cancel all pending requests
     */
    public void cancelPendingRequests() {
        if(requestQueue != null) {
            requestQueue.cancelAll(TAG);
        }
    }

    /**
     * make specific async request defined by the given parameter type to the cms database and save result in global variable
     * @param type
     */
    public void makeRequest(final String type) {
        String url = String.format(this.url, type);

        JsonObjectRequest request = new JsonObjectRequest(url, jsonRequestBody, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, response.toString());
                /*if(type == Collection.CAR_MODELS) {
                    Log.d(TAG, "got response for car models");
                    mapCarModels(response);
                }*/
                if(type == Collection.CATEGORIES) {
                    Log.d(TAG, "got response for categories");
                    DataMapper.mapCategories(response);
                }
                else if(type == Collection.SHOWROOM_MAP) {
                    Log.d(TAG, "got response for showroom map");
                    DataMapper.mapMapObjects(response);
                }
            }

        }, new Response.ErrorListener() { //Create an error listener to handle errors appropriately.
            @Override
            public void onErrorResponse(VolleyError error) {
                //This code is executed if there is an error.
            }
        });
        this.addToRequestQueue(request);
    }

    /**
     * save customer c data to the cms database
     * @param c
     */
    public void sendCustomerData(Customer c) {
        String url = "https://loomo.exocreations.de/api/collections/save/customers?token=account-1d02ac9dab107851012a327336009c";
        String bodyContent = "{\"data\": {\"first_name\": \"" + c.getFirstName() + "\", \"last_name\": \"" + c.getLastName() + "\", \"email\": \"" +
                c.getEmail() + "\", \"phone_number\": \"" + c.getPhoneNumber() + "\", \"address\": \"" + c.getAddress() +
                "\", \"house_number\": \"" + c.getHouseNumber() + "\", \"zip_code\": \"" + c.getZipCode() + "\", \"city\": \"" + c.getCity() +
                "\", \"interest\": \"" + c.getInterest() + "\"}}";

        try {
            JSONObject body = new JSONObject(bodyContent);
            JsonObjectRequest request = new JsonObjectRequest(url, body, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, response.toString());

            }

        }, new Response.ErrorListener() { //Create an error listener to handle errors appropriately.
            @Override
            public void onErrorResponse(VolleyError error) {
                //This code is executed if there is an error.
            }
        });
        this.addToRequestQueue(request);
        }
        catch(JSONException e) {
            Log.w( null, "Exception: ", e);
        }
    }

    /**
     * class which defines the possible request types
     */
    public class Collection {
        //public static final String CAR_MODELS = "carModels";
        public static final String CATEGORIES = "categories";
        public static final String SHOWROOM_MAP = "showroomMap";
    }

}
