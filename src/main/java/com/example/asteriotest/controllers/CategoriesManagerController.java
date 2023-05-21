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


    @GetMapping("/categories/search")
    public ResponseEntity<List<Category>> searchCategory(@RequestParam String name) throws IllegalArgumentException {
        try {
            return ResponseEntity.ok(categoryManagerService.searchCategory(name));
        } catch (IllegalArgumentException exc) {
            return ResponseEntity.noContent().build();
        } catch (CategoryNotFoundException exc) {
            return ResponseEntity.noContent().build();
        }
    }


    @PutMapping("/categories/update")
    public ResponseEntity<String> updateCategory(@RequestBody Category category) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(categoryManagerService.updateCategory(category));
        } catch (CategoryAlreadyExistsException exc) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(exc.getMessage());
        } catch (CategoryNotFoundException exc) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exc.getMessage());
        }
    }

}
