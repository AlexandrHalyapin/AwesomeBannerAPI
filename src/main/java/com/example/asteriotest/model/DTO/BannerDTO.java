package com.example.asteriotest.model.DTO;

import com.example.asteriotest.model.Banner;
import com.example.asteriotest.model.Category;

import java.util.Set;

public class BannerDTO {
    Banner banner;
    Set<Long> categoriesId; // id fot to change

    public Banner getBanner() {
        return banner;
    }

    public void setBanner(Banner banner) {
        this.banner = banner;
    }

    public Set<Long> getCategoriesId() {
        return categoriesId;
    }

    public void setCategoriesId(Set<Long> categoriesId) {
        this.categoriesId = categoriesId;
    }
}
