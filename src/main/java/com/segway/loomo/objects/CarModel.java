package com.segway.loomo.objects;

public class CarModel extends AppObject {
    private String name;

    public CarModel (String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
