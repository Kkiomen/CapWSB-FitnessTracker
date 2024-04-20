package com.capgemini.wsb.fitnesstracker.user.api;

public class UserExistsException extends Exception{
    public UserExistsException(String email) {
        super("User with EMAIL=%s is exists".formatted(email));
    }

}
