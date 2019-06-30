package com.segway.loomo.objects;

/**
 * class which represents an map item in the car showroom, it includes the Car object and its respective Spot object
 */
public class MapObject extends AppObject{

    /**
     * the car of the map item
     */
    private Car car;

    /**
     * the respective spot of the map item
     */
    private Spot spot;

    /**
     * getter function to get the car of the map object
     * @return car
     */
    public Car getCar() {
        return car;
    }

    /**
     * setter function to set the car of the map object
     * @param car
     */
    public void setCar(Car car) {
        this.car = car;
    }

    /**
     * getter function to get the spot of the map object
     * @return spot
     */
    public Spot getSpot() {
        return spot;
    }

    /**
     * setter function to set the spot of the map object
     * @param spot
     */
    public void setSpot(Spot spot) {
        this.spot = spot;
    }
}
