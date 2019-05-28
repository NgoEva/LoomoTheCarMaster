package com.segway.loomo.objects;

public class Customer extends AppObject{
    private String firstName;
    private String lastName;
    private String address;
    private int houseNumber;
    private int zipCode;
    private String city;
    private String phoneNumber;
    private String interest;

    public Customer() {}

    public Customer(String firstName, String lastName, String address, int houseNumber,  int zipCode, String city, String phoneNumber, String interest) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
        this.houseNumber = houseNumber;
        this.zipCode = zipCode;
        this.city = city;
        this.phoneNumber = phoneNumber;
        this.interest = interest;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getHouseNumber() {
        return houseNumber;
    }

    public void setHouseNumber(int houseNumber) {
        this.houseNumber = houseNumber;
    }

    public int getZipCode() {
        return zipCode;
    }

    public void setZipCode(int zipCode) {
        this.zipCode = zipCode;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getInterest() {
        return interest;
    }

    public void setInterest(String interest) {
        this.interest = interest;
    }
}
