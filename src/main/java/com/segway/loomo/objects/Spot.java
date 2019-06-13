package com.segway.loomo.objects;

/**
 * class which represents an exhibition spot in the car showroom, it has a x- and a y-coordinate
 */
public class Spot extends AppObject {
    private String name;
    private float x_coordinate;
    private float y_coordinate;

    public Spot() {
    }

    public Spot(String name, float x_coordinate, float y_coordinate) {
        this.x_coordinate = x_coordinate;
        this.y_coordinate = y_coordinate;
    }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public float getX_coordinate() {
        return x_coordinate;
    }

    public void setX_coordinate(double x_coordinate) {
        float x = (float) x_coordinate;
        this.x_coordinate = x;
    }

    public float getY_coordinate() {
        return y_coordinate;
    }

    public void setY_coordinate(double y_coordinate) {
        float y = (float) y_coordinate;
        this.y_coordinate = y;
    }
}
