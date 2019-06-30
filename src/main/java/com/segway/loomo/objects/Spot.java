package com.segway.loomo.objects;

/**
 * class which represents an exhibition spot in the car showroom, it has a x- and y-coordinate
 */
public class Spot extends AppObject {

    /**
     * the name of the spot
     */
    private String name;

    /**
     * the x-coordinate of the spot
     */
    private float x_coordinate;

    /**
     * the y-coordinate of the spot
     */
    private float y_coordinate;

    /**
     * empty default constructor of a spot
     */
    public Spot() {
    }

    /**
     * constructor of a spot
     */
    public Spot(String name, float x_coordinate, float y_coordinate) {
        this.x_coordinate = x_coordinate;
        this.y_coordinate = y_coordinate;
    }

    /**
     * getter function to get the name of the spot
     * @return name
     */
    public String getName() { return name; }

    /**
     * setter function to set the name of the spot
     * @param name
     */
    public void setName(String name) { this.name = name; }

    /**
     * getter function to get the x-coordinate of the spot
     * @return x_coordinate
     */
    public float getX_coordinate() {
        return x_coordinate;
    }

    /**
     * setter function to set the x-coordinate of the spot
     * @param x_coordinate
     */
    public void setX_coordinate(double x_coordinate) {
        float x = (float) x_coordinate;
        this.x_coordinate = x;
    }

    /**
     * getter function to get the y-coordinate of the spot
     * @return y_coordinate
     */
    public float getY_coordinate() {
        return y_coordinate;
    }

    /**
     * setter function to set the y-coordinate of the spot
     * @param y_coordinate
     */
    public void setY_coordinate(double y_coordinate) {
        float y = (float) y_coordinate;
        this.y_coordinate = y;
    }
}
