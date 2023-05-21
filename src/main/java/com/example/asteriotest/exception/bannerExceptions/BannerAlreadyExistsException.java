package com.example.asteriotest.exception.bannerExceptions;

public class BannerAlreadyExistsException extends RuntimeException{
    public BannerAlreadyExistsException(String message) {
        super(message);
    }
}
