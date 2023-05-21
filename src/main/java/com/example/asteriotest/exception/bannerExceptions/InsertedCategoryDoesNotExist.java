package com.example.asteriotest.exception.bannerExceptions;

public class InsertedCategoryDoesNotExist extends RuntimeException {
    public InsertedCategoryDoesNotExist(String message) {
        super(message);
    }
}
