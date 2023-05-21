package com.example.asteriotest.exception.bannerExceptions;

public class BannerNotFoundException extends RuntimeException {
    public BannerNotFoundException(String message) {
        super(message);
    }
}
