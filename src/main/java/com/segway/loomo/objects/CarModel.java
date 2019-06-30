package com.segway.loomo.objects;

/**
 * class which represents a car model that the customer can select from, e.g. A-Class, B-Class, etc.
 */
public class CarModel extends AppObject {
    private String name;

    /**
     * constructor of a car model object
     * @param name
     */
    public CarModel (String name) {
        this.name = name;
    }

    /**
     * getter function to get the name of the car model object
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * setter function to set the name of the car model object
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }
}
