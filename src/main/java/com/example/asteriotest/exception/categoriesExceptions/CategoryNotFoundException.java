package com.example.asteriotest.exception.categoriesExceptions;


public class CategoryNotFoundException extends RuntimeException {

    public CategoryNotFoundException(String message) {
        super(message);
    }
}
