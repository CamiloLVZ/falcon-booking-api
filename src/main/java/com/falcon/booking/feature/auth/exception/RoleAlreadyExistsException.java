package com.falcon.booking.feature.auth.exception;

public class RoleAlreadyExistsException extends RuntimeException {
    public RoleAlreadyExistsException(String roleName) {

        super("Role with name " + roleName + " already exists");
    }
}
