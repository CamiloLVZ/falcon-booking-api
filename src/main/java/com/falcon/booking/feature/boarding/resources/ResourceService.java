package com.falcon.booking.feature.boarding.resources;

public interface ResourceService {

    String loadAsString(String path);
    byte[] loadAsBytes(String path);

}