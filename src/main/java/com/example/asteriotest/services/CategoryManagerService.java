package com.example.asteriotest.services;

import com.example.asteriotest.exception.categoriesExceptions.CategoryAlreadyExistsException;
import com.example.asteriotest.exception.categoriesExceptions.CategoryNotFoundException;
import com.example.asteriotest.exception.categoriesExceptions.DependentСategoryException;
import com.example.asteriotest.model.Banner;
import com.example.asteriotest.model.Category;
import com.example.asteriotest.repository.BannerRepository;
import com.example.asteriotest.repository.CategoriesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class CategoryManagerService {
    private final CategoriesRepository categoriesRepo;
    private final BannerRepository bannerRepo;

    @Autowired
    public CategoryManagerService(CategoriesRepository categoriesRepo, BannerRepository bannerRepo) {
        this.categoriesRepo = categoriesRepo;
        this.bannerRepo = bannerRepo;
    }

    /**
     * Adding a new category. The method accepts a category parameter using the RequestBody,
     * if one of the properties of the passed category is not unique, execution stops
     * */
    public void addCategory(Category category) {
        if (categoriesRepo.existsByName(category.getName())) { // Check that this category has not been created before (name)
            throw new CategoryAlreadyExistsException("Category with this name already exist");
        }
        if (categoriesRepo.existsByRequestId(category.getRequestId())) { // Check that this category has not been created before (requestId)
            throw new CategoryAlreadyExistsException("Category with this requestId alreadyExist");
        }

        categoriesRepo.save(category); // if the new category is unique, save it
    }

    /**
     * The method of removing categories.
     * A category can only be deleted if there are no dependent banners,
     * if there are dependent banners,
     * the category can ONLY be deleted if the request contains the parameter "cascadeRemove=true".
     * */
    public String deleteCategory(Long id, Boolean cascadeRemove) {
        Optional<Category> toDelete = categoriesRepo.findById(id);

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
                throw new DependentСategoryException("The category cannot be deleted because other banners depend on it. Set \"true\" for the \"cascadeRemode\" parameter to remove all related banners.");
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


            categoriesRepo.save(category);
            return response;
        } else {
            throw new CategoryNotFoundException("Removed category does not exist");
        }
    }

    /**
     * Search for a category by name. Not case sensitive
     * */
    public List<Category> searchCategory(String name) throws IllegalArgumentException {
        if (name == null || name.isBlank()) { // If the parameter is empty, stop execution
            throw new IllegalArgumentException("Category name cannot be empty");
        }

        name = name.toLowerCase();
        Optional<List<Category>> categories = categoriesRepo.findAllByNameContains(name);

        if (categories.isEmpty()) {
            throw new CategoryNotFoundException("Category with this name does not exist");
        }

        return categories.get();

    }

    /**
     * Category update method.
     * The category name and requestID are checked for uniqueness are has not been created before
     * */
    public String updateCategory(Category category) {
        // If the category name has been changed. we need to check if this name is taken by another category in the database
        Optional<Category> categoryForNameCheck = categoriesRepo.findByName(category.getName());
        if (categoryForNameCheck.isPresent()) {
            if (!categoryForNameCheck.get().getId().equals(category.getId())) { // The category we are updating has a name taken by another category
                throw new CategoryAlreadyExistsException("Category with this name already exists");
            }
        }

        Optional<Category> categoryForRequestIdCheck = categoriesRepo.findByRequestId(category.getRequestId());
        if (categoryForRequestIdCheck.isPresent()) {
            if (!categoryForRequestIdCheck.get().getId().equals(category.getId())) { // The category we are updating has a requestId taken by another category
                throw new CategoryAlreadyExistsException("Category with this requestId already exists");
            }
        }

        if (categoriesRepo.existsById(category.getId())) {
            categoriesRepo.save(category);
            System.out.println("Category has been edited");
            return "Category has been edited";
        } else {
            System.out.println("Category with this ID not found");
            throw new CategoryNotFoundException("Category with this ID not found");
        }

    }

}
