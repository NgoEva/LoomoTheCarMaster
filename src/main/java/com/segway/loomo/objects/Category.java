package com.segway.loomo.objects;

public class Category extends AppObject {

    private String name;

    /**
     * constructor of a category object
     * @param name
     */
    public Category(String name) {
        this.name = name;
    }

    /**
     * getter function to get the name of the category object
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * setter function to set the name of the category object
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

}
