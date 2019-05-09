package com.segway.loomo.objects;

public abstract class AppObject {

    private int id = 0;

    public AppObject() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}