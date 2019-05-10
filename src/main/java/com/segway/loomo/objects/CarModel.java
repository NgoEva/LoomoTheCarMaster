package com.segway.loomo.objects;

public class CarModel extends AppObject {
    private String name;
    private int category_id;

    public CarModel (String name, int category_id) {

        this.name = name;
        this.category_id = category_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCategory_id() {
        return category_id;
    }

    public void setCategory_id(int category_id) {
        this.category_id = category_id;
    }
}
