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

    /**
     *  constructor of a car object
     * @param name
     * @param color
     * @param seatNumber
     * @param power
     * @param maxSpeed
     * @param transmission
     * @param fuelType
     * @param maxFuelConsumption
     * @param price
     */
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

    /**
     *
     * getter function to get the name of the car object
     * @return name
     */
    public String getName() { return name; }

    /**
     * setter function to set the name of the car object
     * @param name
     */

    public void setName(String name) {
        this.name = name;
    }

    /**
     * getter function to get the category of the car object
     * @return category
     */
    public Category getCategory() { return category; }


    /**
     * setter function to set the category of the car object
     * @param category
     */
    public void setCategory(Category category) { this.category = category; }

    /**
     * getter function to get the car model of the car object
     * @return carModel
     */
    public CarModel getCarModel() { return carModel; }

    /**
     * setter function to set the car model of the car object
     * @param carModel
     */
    public void setCarModel(CarModel carModel) { this.carModel = carModel; }

    /**
     * getter function to get the color of the car object
     * @return color
     */
    public String getColor() { return color; }

    /**
     * setter function to set the color of the car object
     * @param color
     */
    public void setColor(String color) {
        this.color = color;
    }

    /**
     * getter function to get the seat number of the car object
     * @return seatNumber
     */
    public int getSeatNumber(){
        return seatNumber;
    }

    /**
     * setter function to set the seat number of the car object
     * @param seatNumber
     */
    public void setSeatNumber(int seatNumber){
        this.seatNumber = seatNumber;
    }

    /**
     * getter function to get the power of the car object
     * @return power
     */
    public int getPower(){
        return power;
    }

    /**
     * setter function to set the power of the car object
     * @param power
     */
    public void setPower(int power){
        this.power = power;
    }

    /**
     * getter function to get the maximum speed of the  car object
     * @return maxSpeed
     */
    public int getMaxSpeed(){
        return maxSpeed;
    }

    /**
     * setter function to set the maximum speed of the car object
     * @param maxSpeed
     */
    public void setMaxSpeed(int maxSpeed){
        this.maxSpeed = maxSpeed;
    }

    /**
     * getter function to get the transmission of the car object
     * @return transmission
     */
    public String getTransmission(){
        return transmission;
    }

    /**
     * setter function to set the transimission of the car object
     * @param transmission
     */
    public void setTransmission(String transmission){ this.transmission = transmission; }

    /**
     * getter function to get the fuel type of the car object
     * @return fuelType
     */
    public String getFuelType(){
        return fuelType;
    }


    /**
     * setter function to set the fuel type of the car object
     * @param fuelType
     */
    public void setFuelType(String fuelType){
        this.fuelType = fuelType;
    }

    /**
     * getter function to get the maximum fuel consumption of the car object
     * @return maxFuelConsumption
     */
    public float getMaxFuelConsumption(){
        return maxFuelConsumption;
    }

    /**
     * setter function to set the maximum fuel consumption of the car object
     * @param maxFuelConsumption
     */
    public void setMaxFuelConsumption(double maxFuelConsumption){
        float max = (float) maxFuelConsumption;
        this.maxFuelConsumption = max; }

    /**
     * getter function to get the price of the car object
      * @return price
     */
    public int getPrice(){
        return price;
    }

    /**
     * setter function to set the price of the car object
     * @param price
     */
    public void setPrice(int price){
        this.price = price;
    }


}
