package com.segway.loomo.objects;

public class MapObject extends AppObject{
    private Car car;
    private Spot spot;

    public Car getCar() {
        return car;
    }

    public void setCar(Car car) {
        this.car = car;
    }

    public Spot getSpot() {
        return spot;
    }

    public void setSpot(Spot spot) {
        this.spot = spot;
    }
}
