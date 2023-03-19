package com.example.asteriotest.repository;

import com.example.asteriotest.model.Banner;
import com.example.asteriotest.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BannerRepository extends JpaRepository<Banner, Long> {
    @Override
    Optional<Banner> findById(Long aLong);
    Optional<List<Banner>> findAllByNameBannerContains(String name);
    Optional<List<Banner>> findAllByCategories_name(String name);
    boolean existsByNameBanner(String nameBanner);
    boolean existsById(Long id);
    Optional<Banner> findByNameBanner(String name);

    Optional<List<Banner>> findAllByCategories_nameIn(List<String> categories);

    Optional<Banner> findFirstByCategories_requestIdInOrderByPriceDesc(List<String> categories);


    Optional<List<Banner>> findAllByCategories_requestIdIn(List<String> categories);



}
