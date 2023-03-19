package com.example.asteriotest.repository;

import com.example.asteriotest.model.Banner;
import com.example.asteriotest.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoriesRepository extends JpaRepository<Category, Long> {
    boolean existsById(Long id);
    boolean existsByName(String name);
    boolean existsByRequestId(String requestId);

    Optional<List<Category>> findAllByNameContains(String name);
    Optional<Category> findByName(String name);
    Optional<Category> findByRequestId(String requestId);
}
