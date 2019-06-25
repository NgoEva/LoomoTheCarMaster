package com.segway.loomo.objects;

/**
 * abstract class which represents a basic business object in this application
 */
public abstract class AppObject {

    /**
     * the id of an app object
     */
    private String id = "";

    /**
     * default constructor of an app object
     */
    public AppObject() {
    }

    /**
     * getter function to get the id of the app object
     * @return id
     */
    public String getId() {
        return id;
    }

    /**
     * setter function to set the id of the app object
     * @param id
     */
    public void setId(String id) {
        this.id = id;
    }
}