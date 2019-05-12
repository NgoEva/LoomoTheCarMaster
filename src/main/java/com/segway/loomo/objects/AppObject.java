package com.segway.loomo.objects;

public abstract class AppObject {

    private String id = "";

    public AppObject() {
    }

    public String getId() {
        return id;
    }


    public void setId(String id) {
        this.id = id;
    }
}