package com.example.asteriotest.model;



import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Where;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Entity
@Where(clause = "deleted = false")
public class Banner {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id;

    @Column(nullable = false, unique = true)
    private String nameBanner;
    @Column(nullable = false)
    private String text;
    @Column(nullable = false)
    private double price;
    @ColumnDefault("false")
    private boolean deleted;

    @Column(nullable = false)
    @ManyToMany(cascade = CascadeType.MERGE)
    Set<Category> categories;

    public Banner() {}

    public Banner(String nameBanner, int price, Set<Category> categories) {
        this.nameBanner = nameBanner;
        this.price = price;
        this.categories = categories;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNameBanner() {
        return nameBanner;
    }

    public void setNameBanner(String nameBanner) {
        this.nameBanner = nameBanner;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public Set<Category> getCategories() {
        return categories;
    }

    public void setCategories(Set<Category> categories) {
        this.categories = categories;
    }

    public void putCategory(Category category) {
        if (categories == null) {
            categories = new HashSet<>();
            categories.add(category);
        } else {
            categories.add(category);
        }

    }

    @Override
    public String toString() {
        return "name: " + nameBanner + ", price: " + price + ", \n text: " + text;
    }


}
