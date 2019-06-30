package com.segway.loomo.objects;

/**
 * class which represents a car in the car showroom, in includes car information, e.g. maximum speed, seat number etc.
 */
public class Car extends AppObject{

    /**
     * the corresponding category of the car
     */
    private Category category;

    /**
     * the car model of the car
     */
    private CarModel carModel;

    /**
     * the name of the car
     */
    private String name;

    /**
     * the color of the car
     */
    private String color;

    /**
     * the seat number of the car
     */
    private int seatNumber;

    /**
     * the power of the car in horsepower
     */
    private int power;

    /**
     * the maximum speed of the car in kilometres per hour
     */
    private int maxSpeed;

    /**
     * the transmission of the car, either Automatic or Manual
     */
    private String transmission;

    /**
     * the fuel type of the car, either Diesel or Petrol
     */
    private String fuelType;

    /**
     * the maximum fuel consumption of the car in litres per 100 kilometres
     */
    private float maxFuelConsumption;

    /**
     * the price of the car in euros
     */
    private int price;

    /**
     * empty default constructor of a car object
     */
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
     * getter function to get the name of the car
     * @return name
     */
    public String getName() { return name; }

    /**
     * setter function to set the name of the car
     * @param name
     */

    public void setName(String name) {
        this.name = name;
    }

    /**
     * getter function to get the category of the car
     * @return category
     */
    public Category getCategory() { return category; }


    /**
     * setter function to set the category of the car
     * @param category
     */
    public void setCategory(Category category) { this.category = category; }

    /**
     * getter function to get the car model of the car
     * @return carModel
     */
    public CarModel getCarModel() { return carModel; }

    /**
     * setter function to set the car model of the car
     * @param carModel
     */
    public void setCarModel(CarModel carModel) { this.carModel = carModel; }

    /**
     * getter function to get the color of the car
     * @return color
     */
    public String getColor() { return color; }

    /**
     * setter function to set the color of the car
     * @param color
     */
    public void setColor(String color) {
        this.color = color;
    }

    /**
     * getter function to get the seat number of the car
     * @return seatNumber
     */
    public int getSeatNumber(){
        return seatNumber;
    }

    /**
     * setter function to set the seat number of the car
     * @param seatNumber
     */
    public void setSeatNumber(int seatNumber){
        this.seatNumber = seatNumber;
    }

    /**
     * getter function to get the power of the car
     * @return power
     */
    public int getPower(){
        return power;
    }

    /**
     * setter function to set the power of the car
     * @param power
     */
    public void setPower(int power){
        this.power = power;
    }

    /**
     * getter function to get the maximum speed of the car
     * @return maxSpeed
     */
    public int getMaxSpeed(){
        return maxSpeed;
    }

    /**
     * setter function to set the maximum speed of the car
     * @param maxSpeed
     */
    public void setMaxSpeed(int maxSpeed){
        this.maxSpeed = maxSpeed;
    }

    /**
     * getter function to get the transmission of the car
     * @return transmission
     */
    public String getTransmission(){
        return transmission;
    }

    /**
     * setter function to set the transimission of the car
     * @param transmission
     */
    public void setTransmission(String transmission){ this.transmission = transmission; }

    /**
     * getter function to get the fuel type of the car
     * @return fuelType
     */
    public String getFuelType(){
        return fuelType;
    }


    /**
     * setter function to set the fuel type of the car
     * @param fuelType
     */
    public void setFuelType(String fuelType){
        this.fuelType = fuelType;
    }

    /**
     * getter function to get the maximum fuel consumption of the car
     * @return maxFuelConsumption
     */
    public float getMaxFuelConsumption(){
        return maxFuelConsumption;
    }

    /**
     * setter function to set the maximum fuel consumption of the car
     * @param maxFuelConsumption
     */
    public void setMaxFuelConsumption(double maxFuelConsumption){
        float max = (float) maxFuelConsumption;
        this.maxFuelConsumption = max; }

    /**
     * getter function to get the price of the car
      * @return price
     */
    public int getPrice(){
        return price;
    }

    /**
     * setter function to set the price of the car
     * @param price
     */
    public void setPrice(int price){
        this.price = price;
    }


}
