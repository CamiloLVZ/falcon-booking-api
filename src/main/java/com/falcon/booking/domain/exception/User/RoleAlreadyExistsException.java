package com.falcon.booking.domain.exception.User;

public class RoleAlreadyExistsException extends RuntimeException {
    public RoleAlreadyExistsException(String roleName) {

        super("Role with name " + roleName + " already exists");
    }
}
