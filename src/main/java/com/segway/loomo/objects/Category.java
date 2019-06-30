package com.segway.loomo.objects;

/**
 * class which represents a car category that the customer can select from, e.g. Hatchback, Saloon, ...
 */
public class Category extends AppObject {

    /**
     * the category name
     */
    private String name;

    /**
     * constructor of a category object
     * @param name
     */
    public Category(String name) {
        this.name = name;
    }

    /**
     * getter function to get the category name
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * setter function to set the category name
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

}
