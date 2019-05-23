package com.segway.loomo;

import android.content.Context;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.Volley;
import com.segway.loomo.objects.AppObject;
import com.segway.loomo.objects.CarModel;
import com.segway.loomo.objects.Category;
import com.segway.loomo.objects.Car;
import com.segway.loomo.objects.MapObject;
import com.segway.loomo.objects.Spot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class RequestHandler {
    private static String TAG = "RequestHandler";
    private final Context context;

    private static RequestQueue requestQueue;
    private static RequestHandler requestHandler;

    private String url = "https://loomo.exocreations.de/api/collections/get/%s?token=account-1d02ac9dab107851012a327336009c";
    private String body = "{\"populate\":1}";
    private JSONObject jsonRequestBody;

    public static RequestHandler getInstance() {
        Log.d(TAG, "get request handler instance");
        if (requestHandler == null) {
            throw new IllegalStateException("request handler instance not initialized yet");
        }
        return requestHandler;
    }

    public RequestHandler(Context context) {
        Log.d(TAG, "request handler initiated");
        this.context = context;
        init();
        requestHandler = this;
    }

    public void init() {
        try {
            jsonRequestBody = new JSONObject(this.body);
        }
        catch(JSONException e) {
            Log.w( null, "Exception: ", e);
        }
    }

    public RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(context);
        }
        return requestQueue;
    }

    public void addToRequestQueue(JsonRequest request) {
        VolleyLog.d("Adding request to queue: %s", request.getUrl());
        request.setTag(TAG);
        getRequestQueue().add(request);
    }

    public void cancelPendingRequests() {
        if(requestQueue != null) {
            requestQueue.cancelAll(TAG);
        }
    }

    public ArrayList<? extends AppObject> makeRequest(final String type) {
        String url = String.format(this.url, type);
        ArrayList<? extends AppObject> responseObjects = new ArrayList<>();
        RequestFuture<JSONObject> future = RequestFuture.newFuture();
        JSONObject response;

        JsonObjectRequest request = new JsonObjectRequest(url, jsonRequestBody, future, future);
        addToRequestQueue(request);

        try {
            response = future.get();
            switch (type) {
                case Collection.CAR_MODELS:
                    responseObjects = mapCarModels(response);
                    break;
                case Collection.CATEGORIES:
                    responseObjects = mapCategories(response);
                    break;
                case Collection.SHOWROOM_MAP:
                    responseObjects = mapMapObjects(response);
                    break;
                default:
                    return null;
            }
            return responseObjects;
        }
        catch(ExecutionException e) {
            Log.w( null, "Exception: ", e);
        }
        catch(InterruptedException e) {
            Log.w( null, "Exception: ", e);
        }
        return null;
    }

    private ArrayList<CarModel> mapCarModels(JSONObject response) {
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
    }

    private ArrayList<Category> mapCategories(JSONObject response) {
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

        return categories;
    }

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

    private ArrayList<MapObject> mapMapObjects(JSONObject response) {
        ArrayList<MapObject> mapObjects = new ArrayList<MapObject>();
        try {
            JSONArray objects = response.getJSONArray("entries");
            for (int i = 0; i < objects.length(); i++) {
                JSONObject obj = objects.getJSONObject(i);
                MapObject mapObject = new MapObject();
                for(int j = 0; j < obj.length(); j++){
                    mapObject.setCar(mapCar(obj));
                    mapObject.setSpot(mapSpot(obj));
                    mapObjects.add(mapObject);
                }
            }
        } catch(JSONException e) {
            Log.d( TAG, "Exception: ", e);
        }

        return mapObjects;
    }

    public class Collection {
        public static final String CAR_MODELS = "carModels";
        public static final String CATEGORIES = "categories";
        public static final String SHOWROOM_MAP = "showroomMap";
    }

}
