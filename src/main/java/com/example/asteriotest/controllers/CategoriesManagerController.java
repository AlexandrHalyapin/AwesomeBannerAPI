package com.example.asteriotest.controllers;

import com.example.asteriotest.model.Banner;
import com.example.asteriotest.model.Category;
import com.example.asteriotest.repository.BannerRepository;
import com.example.asteriotest.repository.CategoriesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Controller
public class CategoriesManagerController {

    private CategoriesRepository categoryRepo;
    private BannerRepository bannerRepo;

    @Autowired
    public CategoriesManagerController(CategoriesRepository categoryRepo, BannerRepository bannerRepo) {
        this.categoryRepo = categoryRepo;
        this.bannerRepo = bannerRepo;
    }



    /**
    * Adding a new category. The method accepts a category parameter using the RequestBody,
    * if one of the properties of the passed category is not unique, execution stops
    * */
    @PostMapping("categories/addCategories")
    public ResponseEntity<String> addCategory(@RequestBody Category category) {
        if (categoryRepo.existsByName(category.getName())) { // Check that this category has not been created before (name)
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Category with this name already exist");
        }
        if (categoryRepo.existsByRequestId(category.getRequestId())) { // Check that this category has not been created before (requestId)
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Category with this requestId already exist");
        }

        categoryRepo.save(category); // if the new category is unique, save it

        return ResponseEntity.status(HttpStatus.OK).body("Category has been created with properties: " + category.toString());
    }

    /**
     * The method of removing categories.
     * A category can only be deleted if there are no dependent banners,
     * if there are dependent banners,
     * the category can ONLY be deleted if the request contains the parameter "cascadeRemove=true".
     * */
    @DeleteMapping("/categories/delete/{id}")
    public ResponseEntity<String> deleteCategory(@PathVariable Long id, @RequestParam Boolean cascadeRemove) {
        Optional<Category> toDelete = categoryRepo.findById(id);
        if (toDelete.isPresent()) { // check if the category exists
            Category category = toDelete.get();


            /*
            * If cascadeRemove = true is selected,
            * then we remove the category and all associated banners.
            */
            String response = "Category " + category.getName() + " has been deleted";
            if (cascadeRemove) {
               Optional<List<Banner>> bannersToDelete = bannerRepo.findAllByCategories_name(toDelete.get().getName());
                if (bannersToDelete.get().size() > 0) {
                    for (Banner banner : bannersToDelete.get()) {
                        banner.setDeleted(true);
                        banner.setNameBanner(banner.getNameBanner() + " # Deleted: " + new Date());
                        bannerRepo.save(banner);
                    }
                    response +=", removed all related banners along with it";
                }
            } else {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("The category cannot be deleted because other banners depend on it. Set \"true\" for the \"cascadeRemode\" parameter to remove all related banners.");
            }

            /*
            * If we delete a record logically, it is no longer considered in queries,
            * while it is important for us to maintain uniqueness of new record names,
            * we get a conflict: uniqueness check in controller does not know about records "deleted = false"
            * and allows to save record, and then we get "duplicate" error.
            * In this case, deleting changes the names of deleted records,
            * this will avoid the "duplicate" error in the future.
            * */

            category.setDeleted(true);
            category.setName(category.getName() + " # Deleted: " + new Date());
            category.setRequestId(category.getRequestId() + " # Deleted: " + new Date());


            categoryRepo.save(category);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Removed category does not exist");
        }
    }

    @GetMapping("/categories/search")
    public ResponseEntity<List<Category>> searchCategory(@RequestParam String name) {
        if (name == null || name.isBlank()) { //If the parameter is empty, stop execution
            return ResponseEntity.noContent().build();
        }

        name = name.toLowerCase();
        Optional<List<Category>> categories = categoryRepo.findAllByNameContains(name);
        if (categories.get().size() > 0) {
            return ResponseEntity.ok(categories.get());
        } else {
            return ResponseEntity.noContent().build();
        }
    }
    @PutMapping("/categories/update")
    public ResponseEntity<String> updateCategory(@RequestBody Category category) {
        // If the category name has been changed. we need to check if this name is taken by another category in the database
        Optional<Category> categoryForNameCheck = categoryRepo.findByName(category.getName());
        if (categoryForNameCheck.isPresent()) {
            if (!categoryForNameCheck.get().getId().equals(category.getId())) { // The banner we are updating has a name taken by another banner
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Category with this name already exists");
            }
        }

        Optional<Category> categoryForRequestIdCheck = categoryRepo.findByRequestId(category.getRequestId());
        if (categoryForRequestIdCheck.isPresent()) {
            if (!categoryForRequestIdCheck.get().getId().equals(category.getId())) { // The banner we are updating has a name taken by another banner
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
