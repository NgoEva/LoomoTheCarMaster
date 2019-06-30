package com.segway.loomo.objects;

public class Customer extends AppObject{
    private String firstName;
    private String lastName;
    private String email;
    private String address;
    private String houseNumber;
    private String zipCode;
    private String city;
    private String phoneNumber;
    private String interest;

    /**
     * default constructor of a customer object
     */
    public Customer() {}

    /**
     * getter function to get the first name of the customer object
     * @return firstName
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * setter function to set the first name of the customer object
     * @param firstName
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * getter function to get the last name of the customer obejct
     * @return lastName
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * setter function to set the last name of the customer object
     * @param lastName
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * getter function to get the email of the customer object
     * @return email
     */
    public String getEmail() { return email; }

    /**
     * setter function to set the email of the customer object
     * @param email
     */
    public void setEmail(String email) { this.email = email; }

    /**
     * getter function to get the address of the customer object
     * @return address
     */
    public String getAddress() {
        return address;
    }

    /**
     * setter function to set the address of the customer object
     * @param address
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * getter function to get the house number of the customer object
     * @return houseNumber
     */
    public String getHouseNumber() {
        return houseNumber;
    }

    /**
     * setter function to set the house number of the customer object
     * @param houseNumber
     */
    public void setHouseNumber(String houseNumber) {
        this.houseNumber = houseNumber;
    }

    /**
     * getter function to get the zip code of the customer object
     * @return zipCode
     */
    public String getZipCode() {
        return zipCode;
    }

    /**
     * setter function to set the zip code of the customer object
     * @param zipCode
     */
    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    /**
     * getter function to get the city of the customer object
     * @return city
     */
    public String getCity() {
        return city;
    }

    /**
     * setter function to set the city of the customer object
     * @param city
     */
    public void setCity(String city) {
        this.city = city;
    }

    /**
     * getter function to get the phone number of the customer object
     * @return phoneNumber
     */
    public String getPhoneNumber() {
        return phoneNumber;
    }

    /**
     * setter function to set the phone number of the customer object
     * @param phoneNumber
     */
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    /**
     * getter function to get the interest of the customer object
     * @return interest
     */
    public String getInterest() {
        return interest;
    }


    /**
     * setter function to set the interest of the customer object
     * @param interest
     */
    public void setInterest(String interest) {
        this.interest = interest;
    }
}
