package com.segway.loomo.objects;

/**
 * class which represents a customer who enters the car showroom and is interested in getting some information about the cars, it includes the contact
 * information of the customer for later contact, either for more information as a phone call, a test drive or a sales offer
 */
public class Customer extends AppObject{

    /**
     * the first name of the customer
     */
    private String firstName;

    /**
     * the last name of the customer
     */
    private String lastName;

    /**
     * the email of the customer
     */
    private String email;

    /**
     * the address of the customer
     */
    private String address;

    /**
     * the house number of the customer
     */
    private String houseNumber;

    /**
     * the zip code of the customer
     */
    private String zipCode;

    /**
     * the city of the customer
     */
    private String city;

    /**
     * the phone number of the customer
     */
    private String phoneNumber;

    /**
     * the interest of the customer
     */
    private String interest;

    /**
     * default constructor of a customer object
     */
    public Customer() {}

    /**
     * getter function to get the first name of the customer
     * @return firstName
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * setter function to set the first name of the customer
     * @param firstName
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * getter function to get the last name of the customer
     * @return lastName
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * setter function to set the last name of the customer
     * @param lastName
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * getter function to get the email of the customer
     * @return email
     */
    public String getEmail() { return email; }

    /**
     * setter function to set the email of the customer
     * @param email
     */
    public void setEmail(String email) { this.email = email; }

    /**
     * getter function to get the address of the customer
     * @return address
     */
    public String getAddress() {
        return address;
    }

    /**
     * setter function to set the address of the customer
     * @param address
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * getter function to get the house number of the customer
     * @return houseNumber
     */
    public String getHouseNumber() {
        return houseNumber;
    }

    /**
     * setter function to set the house number of the customer
     * @param houseNumber
     */
    public void setHouseNumber(String houseNumber) {
        this.houseNumber = houseNumber;
    }

    /**
     * getter function to get the zip code of the customer
     * @return zipCode
     */
    public String getZipCode() {
        return zipCode;
    }

    /**
     * setter function to set the zip code of the customer
     * @param zipCode
     */
    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    /**
     * getter function to get the city of the customer
     * @return city
     */
    public String getCity() {
        return city;
    }

    /**
     * setter function to set the city of the customer
     * @param city
     */
    public void setCity(String city) {
        this.city = city;
    }

    /**
     * getter function to get the phone number of the customer
     * @return phoneNumber
     */
    public String getPhoneNumber() {
        return phoneNumber;
    }

    /**
     * setter function to set the phone number of the customer
     * @param phoneNumber
     */
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    /**
     * getter function to get the interest of the customer
     * @return interest
     */
    public String getInterest() {
        return interest;
    }


    /**
     * setter function to set the interest of the customer
     * @param interest
     */
    public void setInterest(String interest) {
        this.interest = interest;
    }
}
