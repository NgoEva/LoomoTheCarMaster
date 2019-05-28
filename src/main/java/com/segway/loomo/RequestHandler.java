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
import com.segway.loomo.objects.CarModel;
import com.segway.loomo.objects.Category;
import com.segway.loomo.objects.Car;
import com.segway.loomo.objects.Customer;
import com.segway.loomo.objects.MapObject;
import com.segway.loomo.objects.Spot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * class which handles requests to the cms database
 */
public class RequestHandler {
    private static String TAG = "RequestHandler";
    private final Context context;

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
        init();
        requestHandler = this;
    }

    /**
     * initialize the request body
     */
    public void init() {
        try {
            jsonRequestBody = new JSONObject(this.body);
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
        getRequestQueue().add(request);
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
                    mapCategories(response);
                }
                else if(type == Collection.SHOWROOM_MAP) {
                    Log.d(TAG, "got response for showroom map");
                    mapMapObjects(response);
                }
            }

        }, new Response.ErrorListener() { //Create an error listener to handle errors appropriately.
            @Override
            public void onErrorResponse(VolleyError error) {
                //This code is executed if there is an error.
            }
        });
        addToRequestQueue(request);
    }

   /* private ArrayList<CarModel> mapCarModels(JSONObject response) {
        ArrayList<CarModel> carModels = new ArrayList<CarModel>();
        try {
            JSONArray objects = response.getJSONArray("entries");
            for(int i = 0; i < objects.length(); i++) {
                JSONObject obj = objects.getJSONObject(i);
                CarModel model = new CarModel(obj.getString("name"));
                model.setId(obj.getString("_id"));
                carModels.add(model);
            }
        }
        catch(JSONException e) {
            Log.d( TAG, "Exception: ", e);
        }

        return carModels;
    }*/

    /**
     * maps the response JSON object to Category objects and saves them in a global variable
     * @param response
     */
    private void mapCategories(JSONObject response) {
        ArrayList<Category> categories = new ArrayList<Category>();
        try {
            JSONArray objects = response.getJSONArray("entries");
            for(int i = 0; i < objects.length(); i++) {
                JSONObject obj = objects.getJSONObject(i);
                Category category = new Category(obj.getString("name"));
                category.setId(obj.getString("_id"));
                categories.add(category);
            }
        }
        catch(JSONException e) {
            Log.d( TAG, "Exception: ", e);
        }
        MainActivity.categories = categories;
    }

    /**
     * maps the obj JSON object to a Car object and returns it
     * @param obj
     * @return
     */
    private Car mapCar(JSONObject obj) {
        Car car = new Car();
        try {
            JSONObject carObject = obj.getJSONObject("car");
            car.setId(carObject.getString("_id"));

            Category category = new Category(carObject.getJSONObject("category").getString("name"));
            category.setId(carObject.getJSONObject("category").getString("_id"));

            CarModel carModel = new CarModel(carObject.getJSONObject("car_model").getString("name"));
            carModel.setId(carObject.getJSONObject("car_model").getString("_id"));

            car.setCategory(category);
            car.setCarModel(carModel);

            car.setName(carObject.getString("name"));
            car.setColor(carObject.getString("color"));
            car.setSeatNumber(carObject.getInt("seat_number"));
            car.setPower(carObject.getInt("power"));
            car.setMaxSpeed(carObject.getInt("max_speed"));
            car.setTransmission(carObject.getString("transmission"));
            car.setFuelType(carObject.getString("fuel_type"));
            car.setMaxFuelConsumption(carObject.getDouble("max_fuel_consumption"));
            car.setPrice(carObject.getInt("price"));
        }
        catch(JSONException e) {
            Log.d( TAG, "Exception: ", e);
        }
        return car;
    }

    /**
     * maps the obj JSON object to a Spot object and returns it
     * @param obj
     * @return
     */
    private Spot mapSpot(JSONObject obj) {
        Spot spot = new Spot();
        try {
            JSONObject spotObject = obj.getJSONObject("spot");
            spot.setId(spotObject.getString("_id"));
            spot.setX_coordinate(spotObject.getDouble("x_coordinate"));
            spot.setY_coordinate(spotObject.getDouble("y_coordinate"));

        }
        catch(JSONException e) {
            Log.d( TAG, "Exception: ", e);
        }
        return spot;
    }

    /**
     * maps the response JSON object to MapObject objects and saves them in a global variable
     * @param response
     */
    private void mapMapObjects(JSONObject response) {
        ArrayList<MapObject> mapObjects = new ArrayList<MapObject>();
        try {
            JSONArray objects = response.getJSONArray("entries");
            for (int i = 0; i < objects.length(); i++) {
                JSONObject obj = objects.getJSONObject(i);
                MapObject mapObject = new MapObject();
                mapObject.setCar(mapCar(obj));
                mapObject.setSpot(mapSpot(obj));
                mapObjects.add(mapObject);
            }
        } catch(JSONException e) {
            Log.d( TAG, "Exception: ", e);
        }
        MainActivity.cars = mapObjects;
    }

    /**
     * save customer c data to the cms database
     * @param c
     */
    /*public void sendCustomerData(Customer c) {
        String url = "https://loomo.exocreations.de/api/collections/save/customers?token=account-1d02ac9dab107851012a327336009c";
        String bodyContent = "{\"data\": {\"first_name\": \"" + c.getFirstName() + "\", \"last_name\": \"" + c.getLastName() + "\", \"phone_number\": \"" +
                c.getPhoneNumber() + "\", \"interest\": \"" + c.getInterest() + "\"}}";

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
        addToRequestQueue(request);
        }
        catch(JSONException e) {
            Log.w( null, "Exception: ", e);
        }
    }*/

    /**
     * class which defines the possible request types
     */
    public class Collection {
        //public static final String CAR_MODELS = "carModels";
        public static final String CATEGORIES = "categories";
        public static final String SHOWROOM_MAP = "showroomMap";
    }

}
