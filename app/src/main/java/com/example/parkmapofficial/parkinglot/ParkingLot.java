package com.example.parkmapofficial.parkinglot;

import com.example.parkmapofficial.userrating.UserRating;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

import java.io.Serializable;
import java.util.ArrayList;

public class ParkingLot implements ClusterItem, Serializable {
    private String name;
    private String address;
    private transient LatLng position;
    private float latitude;
    private float longitude;
    private String price;
    private transient ArrayList<UserRating> userRatings;
    private float averageRating;

    // CONSTRUCTORS
    public ParkingLot() {}
    public ParkingLot(float latitude, float longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }
    public ParkingLot(float latitude, float longitude, String name, String address, String price) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.setLatLng();
        this.name = name;
        this.address = address;
        this.price = price;
    }

    // SETTERS
    public void setName(String name) {
        this.name = name;
    }
    public void setAddress(String address) {
        this.address = address;
    }
    public void setLatLng() {
        position = new LatLng(latitude, longitude);
    }
    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }
    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }

    // GETTERS
    public String getName() {
        return name;
    }
    public String getAddress() {
        return address;
    }
    public float getLatitude() {
        return latitude;
    }
    public float getLongitude() {
        return longitude;
    }
    public String getPrice() {
        return price;
    }
    public float getAverageRating() {
        return averageRating;
    }
    public ArrayList<UserRating> getUserRatings() {
        return userRatings;
    }

    // CLUSTER MARKERS
    @Override
    public LatLng getPosition() {
        return new LatLng(latitude, longitude);
    }
    @Override
    public String getTitle() {
        return name;
    }
    @Override
    public String getSnippet() {
        return address;
    }
}
