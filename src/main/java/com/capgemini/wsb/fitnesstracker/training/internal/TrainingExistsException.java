package com.capgemini.wsb.fitnesstracker.training.internal;

public class TrainingExistsException extends Exception{
    public TrainingExistsException(String email) {
        super("Training is exists");
    }

}
