package com.example.asteriotest.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Entity
public class RequestJournal {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;


    @Column
    private String ipAddress;
    @Column
    private String userAgent;
    @Column
    private LocalDateTime requestTime;

    @ManyToOne
    public Banner banner;

//    @OneToMany
//    public Set<Category> categories;
    @Column
    private double bannerPrice;
    @Column
    private String errorMessage;

    public RequestJournal(String ipAddress, String userAgent, LocalDateTime requestTime, Banner banner, double bannerPrice) {
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.requestTime = requestTime;
        this.banner = banner;
//        this.categories = banner.getCategories();
        this.bannerPrice = bannerPrice;
    }

    public RequestJournal(String ipAddress, String userAgent, LocalDateTime requestTime, String errorMessage) {
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.requestTime = requestTime;
        this.errorMessage = errorMessage;
    }

    public RequestJournal() {

    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public LocalDateTime getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(LocalDateTime requestTime) {
        this.requestTime = requestTime;
    }

    public Banner getBanner() {
        return banner;
    }

    public void setBanner(Banner banner) {
        this.banner = banner;
    }

//    public Set<Category> getCategories() {
//        return categories;
//    }
//
//    public void setCategories(Set<Category> categories) {
//        this.categories = categories;
//    }

    public double getBannerPrice() {
        return bannerPrice;
    }

    public void setBannerPrice(double bannerPrice) {
        this.bannerPrice = bannerPrice;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
