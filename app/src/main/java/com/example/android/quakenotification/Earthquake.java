package com.example.android.quakenotification;

/**
 * Created by mohamed on 10/4/2018.
 */

public class Earthquake {
  private  int magnitude ;
  private  String location;
  private String title;
  private  String body ;

    public Earthquake(int magnitude, String location, String title, String body) {




        this.magnitude = magnitude;
        this.location = location;
        this.title=title;
        this.body = body;

    }

    public Earthquake() {

    }

    public int getMagnitude() {
        return magnitude;
    }

    public String getLocation() {
        return location;
    }

    public String getBody() {
        return body;
    }

    public String getTitle() {
        return title;
    }
}
