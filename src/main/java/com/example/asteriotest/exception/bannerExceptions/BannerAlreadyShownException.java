package com.example.asteriotest.exception.bannerExceptions;

public class BannerAlreadyShownException extends RuntimeException {
    public BannerAlreadyShownException(String message) {
        super(message);
    }
}
