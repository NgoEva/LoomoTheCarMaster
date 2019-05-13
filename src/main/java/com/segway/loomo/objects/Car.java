package com.segway.loomo.objects;

public class Car extends AppObject{

    private String color;
    private int seatNumber;
    private int power;
    private int maxSpeed;
    private String transmission;
    private String fuelType;
    private float maxFuelConsumption;
    private int price;

    public Car (String color, int seatNumber, int power, int maxSpeed, String transmission,
                String fuelType, float maxFuelConsumption, int price ){
        this.color = color;
        this.seatNumber = seatNumber;
        this.power = power;
        this.maxSpeed = maxSpeed;
        this.transmission = transmission;
        this.fuelType = fuelType;
        this.maxFuelConsumption = maxFuelConsumption;
        this.price = price;

    }

    public String getColor() {

        return color;
    }

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
    public void setTransmission(String transmission){
        this.transmission = transmission;

    }

    public String getFuelType(){
        return fuelType;
    }

    public void setFuelType(String fuelType){
        this.fuelType = fuelType;
    }

    public float getMaxFuelConsumption(){
        return maxFuelConsumption;
    }

    public void setMaxFuelConsumption(float maxFuelConsumption){
        this.maxFuelConsumption = maxFuelConsumption;
    }

    public int getPrice(){
        return price;
    }

    public void setPrice(int price){
        this.price = price;
    }


}
