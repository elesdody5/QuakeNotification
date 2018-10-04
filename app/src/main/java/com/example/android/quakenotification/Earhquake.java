package com.example.android.quakenotification;

/**
 * Created by mohamed on 10/4/2018.
 */

public class Earhquake {
  private  int magnitude ;
  private  String location;
  private  String body ;

    public Earhquake(int magnitude, String location, String body) {
        this.magnitude = magnitude;
        this.location = location;
        this.body = body;
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
}
