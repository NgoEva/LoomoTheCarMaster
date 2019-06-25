package com.segway.loomo.services;

import android.util.Log;

import com.segway.loomo.MainActivity;
import com.segway.loomo.objects.Car;
import com.segway.loomo.objects.CarModel;
import com.segway.loomo.objects.Category;
import com.segway.loomo.objects.MapObject;
import com.segway.loomo.objects.Spot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class DataMapper {
    private static String TAG = "DataMapper";

    /**
     * maps the response JSON object to Category objects and saves them in a global variable
     * @param response
     */
    public static void mapCategories(JSONObject response) {
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
            Log.d(TAG, "Exception: ", e);
        }
        MainActivity.getInstance().categories = categories;
    }

    /**
     * maps the obj JSON object to a Car object and returns it
     * @param obj
     * @return
     */
    private static Car mapCar(JSONObject obj) {
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
            Log.d(TAG, "Exception: ", e);
        }
        return car;
    }

    /**
     * maps the obj JSON object to a Spot object and returns it
     * @param obj
     * @return
     */
    private static Spot mapSpot(JSONObject obj) {
        Spot spot = new Spot();
        try {
            JSONObject spotObject = obj.getJSONObject("spot");
            spot.setId(spotObject.getString("_id"));
            spot.setName(spotObject.getString("name"));
            spot.setX_coordinate(spotObject.getDouble("x_coordinate"));
            spot.setY_coordinate(spotObject.getDouble("y_coordinate"));

        }
        catch(JSONException e) {
            Log.d(TAG, "Exception: ", e);
        }
        return spot;
    }

    /**
     * maps the response JSON object to MapObject objects and saves them in a global variable
     * @param response
     */
    public static void mapMapObjects(JSONObject response) {
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
            Log.d(TAG, "Exception: ", e);
        }
        MainActivity.getInstance().cars = mapObjects;
    }

    /**
     * maps to the parameter values to the global customer object
     * @param firstName
     * @param lastName
     * @param email
     * @param address
     * @param houseNumber
     * @param phoneNumber
     * @param zipCode
     * @param city
     */
    public static void mapCustomer(String firstName, String lastName, String email, String address, String houseNumber, String phoneNumber, String zipCode, String city) {
        MainActivity.getInstance().customer.setFirstName(firstName);
        MainActivity.getInstance().customer.setLastName(lastName);
        MainActivity.getInstance().customer.setEmail(email);
        MainActivity.getInstance().customer.setAddress(address);
        MainActivity.getInstance().customer.setHouseNumber(houseNumber);
        MainActivity.getInstance().customer.setPhoneNumber(phoneNumber);
        MainActivity.getInstance().customer.setZipCode(zipCode);
        MainActivity.getInstance().customer.setCity(city);
    }
}
