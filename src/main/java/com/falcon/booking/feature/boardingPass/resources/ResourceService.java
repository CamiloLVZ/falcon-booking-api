package com.falcon.booking.feature.boardingPass.resources;

public interface ResourceService {

    String loadAsString(String path);
    byte[] loadAsBytes(String path);

}