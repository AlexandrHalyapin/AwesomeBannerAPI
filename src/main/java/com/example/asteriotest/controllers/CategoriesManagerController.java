package com.example.asteriotest.controllers;

import com.example.asteriotest.exception.categoriesExceptions.CategoryAlreadyExistsException;
import com.example.asteriotest.exception.categoriesExceptions.CategoryNotFoundException;
import com.example.asteriotest.exception.categoriesExceptions.DependentСategoryException;
import com.example.asteriotest.model.Category;
import com.example.asteriotest.repository.BannerRepository;
import com.example.asteriotest.repository.CategoriesRepository;
import com.example.asteriotest.services.CategoryManagerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
public class CategoriesManagerController {

    private CategoriesRepository categoryRepo;
    private BannerRepository bannerRepo;
    private CategoryManagerService categoryManagerService;

    public CategoriesManagerController(CategoriesRepository categoryRepo, BannerRepository bannerRepo, CategoryManagerService categoryManagerService) {
        this.categoryRepo = categoryRepo;
        this.bannerRepo = bannerRepo;
        this.categoryManagerService = categoryManagerService;
    }


    @PostMapping("categories/addCategories")
    public ResponseEntity<String> addCategory(@RequestBody Category category) {
        try {
            categoryManagerService.addCategory(category);
        } catch (CategoryAlreadyExistsException exc) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(exc.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body("Category has been created with properties: " + category.toString());
    }


    @DeleteMapping("/categories/delete/{id}")
    public ResponseEntity<String> deleteCategory(@PathVariable Long id, @RequestParam Boolean cascadeRemove) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(categoryManagerService.deleteCategory(id, cascadeRemove));  // in case if all is well
        } catch (DependentСategoryException exc) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(exc.getMessage()); // in case if category depend from other
        } catch (CategoryNotFoundException exc) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exc.getMessage()); // in case if this category does not exist
        }
    }

    /**
     * Search for a category by name. Not case sensitive
     * */
    @GetMapping("/categories/search")
    public ResponseEntity<List<Category>> searchCategory(@RequestParam String name) throws IllegalArgumentException {
        try {
            return ResponseEntity.ok(categoryManagerService.searchCategory(name));
        } catch (IllegalArgumentException exc) {
            return ResponseEntity.noContent().build();
        }
    }

    /**
     * Category update method.
     * The category name and requestID are checked for uniqueness are has not been created before
     * */
    @PutMapping("/categories/update")
    public ResponseEntity<String> updateCategory(@RequestBody Category category) {
        // If the category name has been changed. we need to check if this name is taken by another category in the database
        Optional<Category> categoryForNameCheck = categoryRepo.findByName(category.getName());
        if (categoryForNameCheck.isPresent()) {
            if (!categoryForNameCheck.get().getId().equals(category.getId())) { // The category we are updating has a name taken by another category
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Category with this name already exists");
            }
        }

        Optional<Category> categoryForRequestIdCheck = categoryRepo.findByRequestId(category.getRequestId());
        if (categoryForRequestIdCheck.isPresent()) {
            if (!categoryForRequestIdCheck.get().getId().equals(category.getId())) { // The category we are updating has a requestId taken by another category
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Category with this requestId already exists");
            }
        }

        if (categoryRepo.existsById(category.getId())) {
            categoryRepo.save(category);
            System.out.println("Category has been edited");
            return ResponseEntity.status(HttpStatus.OK).body("Category has been edited");
        } else {
            System.out.println("Category with this ID not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Category with this ID not found");
        }
    }

}
