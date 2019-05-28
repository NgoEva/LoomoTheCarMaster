package com.segway.loomo.objects;

/**
 * abstract class which represents a basic business object in this application
 */
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