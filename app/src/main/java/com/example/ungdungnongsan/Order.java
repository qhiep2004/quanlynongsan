package com.example.ungdungnongsan;

import java.io.Serializable;
import java.util.ArrayList;

public class Order implements Serializable {
    private String orderId;
    private String name;
    private String address;
    private String phone;
    private ArrayList<CartItem> cartItems;
    private double totalAmount;
    private double latitude;
    private double longitude;

    public Order(String orderId, String name, String address, String phone, ArrayList<CartItem> cartItems, double totalAmount) {
        this.orderId = orderId;
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.cartItems = cartItems;
        this.totalAmount = totalAmount;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getPhone() {
        return phone;
    }

    public ArrayList<CartItem> getCartItems() {
        return cartItems;
    }

    public double getTotalAmount() {
        return totalAmount;
    }
    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}