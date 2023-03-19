package com.example.asteriotest.controllers;

import com.example.asteriotest.model.Banner;
import com.example.asteriotest.model.Category;
import com.example.asteriotest.model.DTO.BannerDTO;
import com.example.asteriotest.repository.BannerRepository;
import com.example.asteriotest.repository.CategoriesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

@Controller
public class BannerManagerController {

    private BannerRepository bannerRepo;
    private CategoriesRepository categoryRepo;

    @Autowired
    public BannerManagerController(BannerRepository bannerRepo, CategoriesRepository categoryRepo) {
        this.bannerRepo = bannerRepo;
        this.categoryRepo = categoryRepo;
    }
    /**
     * The method of adding a banner, a banner can only be added if a banner with the same name has not been created before.
     * Categories to be attached to the banner must be created.
     * */
    @PostMapping("banners/addBanner")
    public ResponseEntity<String> addBanner(@RequestBody Banner banner) {
        if (bannerRepo.existsByNameBanner(banner.getNameBanner())) { // Check if the banner already exists
            System.out.println("Banner with this name already exists");
            return ResponseEntity.status(HttpStatus.CONFLICT).body("ERROR 409: Banner with this name already exists");
        }

        Set<Category> categories = new HashSet<>(banner.getCategories()); // If the category that we add to the banner has not yet been created
        for (Category category : categories ) {
            if (!categoryRepo.existsById(category.getId())) {
                System.out.println("One of the categories does not exist");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("ERROR 404: The category inserted into the banner does not exist");
            }
        }

        bannerRepo.save(banner);
        System.out.println("Banner has been created");
        return ResponseEntity.ok("Banner has been created with properties: " + banner.toString());
    }
    /**
     *  the function of "logical" deletion. To comply with the uniqueness of records,
     *  the time when the record was deleted is added to the properties of the deleted record
     * */
    @DeleteMapping("banners/delete/{id}")
    public ResponseEntity<String> deleteBanner(@PathVariable Long id) {
        Optional<Banner> toDelete = bannerRepo.findById(id);
        if(toDelete.isPresent()) { // check if the banner exists
            Banner banner = toDelete.get();
            banner.setDeleted(true);

            /*
             * If we delete a record logically, it is no longer considered in queries,
             * while it is important for us to maintain uniqueness of new record names,
             * we get a conflict: uniqueness check in controller does not know about records "deleted = false"
             * and allows to save record, and then we get "duplicate" error.
             * In this case, deleting changes the names of deleted records,
             * this will avoid the "duplicate" error in the future.
             * */

            banner.setNameBanner(banner.getNameBanner() + " # Deleted: " + new Date());
            bannerRepo.save(banner);
            return ResponseEntity.status(HttpStatus.OK).body("Banner " + banner.getNameBanner() + " has been deleted");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Removed banner does not exist");
        }
    }


    /**
     *  Banner search request.
     *  The method is not case sensitive.
     */
    @GetMapping("/banners/search")
    public ResponseEntity<List<Banner>> searchBanner(@RequestParam String name) {
            if (name == null || name.isBlank()) { //If the parameter is empty, stop execution
                return ResponseEntity.noContent().build();
            }

            name = name.toLowerCase();

            Optional<List<Banner>> banners = bannerRepo.findAllByNameBannerContains(name);
            if (banners.get().size() > 0) {
                return ResponseEntity.ok(banners.get());
            } else {
                return ResponseEntity.noContent().build();
            }
    }


    //DTO is used. If you use the original banner,
    // the category properties can be changed along with it (via JSON), this is not safe
    /**
     * Banner update method.
     * If the banner name has been changed,
     * a check is made to make sure that the banner with the same name has not been created before
     * Banner update method. If the banner name has been changed,
     * a check is made to make sure that the banner with the same name has not been created before
     * The Data Transfer Object is used, because if you pass a JSON analog of the Banner instance,
     * the Banner update request will allow you to unauthorized edit categories.
     * */
    @PutMapping("/banners/update")
    public ResponseEntity<String> updateBanner(@RequestBody BannerDTO bannerDTO) {
        Banner banner = bannerDTO.getBanner();

        //If the banner name has been changed, we should check if there is a task with the same name in the database
       Optional<Banner> bannerForCheck = bannerRepo.findByNameBanner(banner.getNameBanner());
       if (bannerForCheck.isPresent()) {
           if (!bannerForCheck.get().getId().equals(banner.getId())) { // The banner we are updating has a name taken by another banner
               return ResponseEntity.status(HttpStatus.CONFLICT).body("Banner with this name already exists");
           }
       }

        if (bannerRepo.existsById(banner.getId())) { // check if the banner exists
            Set<Long> categoriesId = bannerDTO.getCategoriesId();
            for (Long id : categoriesId) {
                Optional<Category> category = categoryRepo.findById(id);
                if (category.isPresent()) { // Accepted category id is inserted into the updated banner
                    banner.putCategory(category.get());
                }
            }
            bannerRepo.save(banner);
            System.out.println("Banner has been edited");
            return ResponseEntity.status(HttpStatus.OK).body("Banner has been edited");
        } else {
            System.out.println("Banner with this ID not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Banner with this ID not found");
        }

    }




}
