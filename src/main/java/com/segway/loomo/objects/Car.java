package com.segway.loomo.objects;

public class Car extends AppObject{
    private Category category;
    private CarModel carModel;

    private String name;
    private String color;
    private int seatNumber;
    private int power;
    private int maxSpeed;
    private String transmission;
    private String fuelType;
    private float maxFuelConsumption;
    private int price;

    public Car() {}

    public Car (String name, String color, int seatNumber, int power, int maxSpeed, String transmission,
                String fuelType, float maxFuelConsumption, int price ){
        this.name = name;
        this.color = color;
        this.seatNumber = seatNumber;
        this.power = power;
        this.maxSpeed = maxSpeed;
        this.transmission = transmission;
        this.fuelType = fuelType;
        this.maxFuelConsumption = maxFuelConsumption;
        this.price = price;
    }

    public String getName() { return name; }

    public void setName(String name) {
        this.name = name;
    }

    public Category getCategory() { return category; }

    public void setCategory(Category category) { this.category = category; }

    public CarModel getCarModel() { return carModel; }

    public void setCarModel(CarModel carModel) { this.carModel = carModel; }

    public String getColor() { return color; }

    public void setColor(String color) {
        this.color = color;
    }

    public int getSeatNumber(){
        return seatNumber;
    }

    public void setSeatNumber(int seatNumber){
        this.seatNumber = seatNumber;
    }

    public int getPower(){
        return power;
    }

    public void setPower(int power){
        this.power = power;
    }

    public int getMaxSpeed(){
        return maxSpeed;
    }

    public void setMaxSpeed(int maxSpeed){
        this.maxSpeed = maxSpeed;
    }

    public String getTransmission(){
        return transmission;
    }

    public void setTransmission(String transmission){ this.transmission = transmission; }

    public String getFuelType(){
        return fuelType;
    }

    public void setFuelType(String fuelType){
        this.fuelType = fuelType;
    }

    public float getMaxFuelConsumption(){
        return maxFuelConsumption;
    }

    public void setMaxFuelConsumption(double maxFuelConsumption){
        float max = (float) maxFuelConsumption;
        this.maxFuelConsumption = max; }

    public int getPrice(){
        return price;
    }

    public void setPrice(int price){
        this.price = price;
    }


}
