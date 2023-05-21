package com.example.asteriotest.controllers;

import com.example.asteriotest.exception.bannerExceptions.BannerAlreadyExistsException;
import com.example.asteriotest.exception.bannerExceptions.BannerNotFoundException;
import com.example.asteriotest.exception.bannerExceptions.InsertedCategoryDoesNotExist;
import com.example.asteriotest.model.Banner;
import com.example.asteriotest.model.Category;
import com.example.asteriotest.model.DTO.BannerDTO;
import com.example.asteriotest.model.RequestJournal;
import com.example.asteriotest.repository.BannerRepository;
import com.example.asteriotest.repository.CategoriesRepository;
import com.example.asteriotest.repository.RequestJournalRepository;
import com.example.asteriotest.services.BannerManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Controller
public class BannerManagerController {

    private BannerManagerService bannerManagerService;

    public BannerManagerController(BannerManagerService bannerManagerService) {
        this.bannerManagerService = bannerManagerService;
    }

    @PostMapping("banners/addBanner")
    public ResponseEntity<String> addBanner(@RequestBody Banner banner) {

        try {
            return ResponseEntity.ok(bannerManagerService.addBanner(banner));
        } catch (BannerAlreadyExistsException exc) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(exc.getMessage());
        } catch (InsertedCategoryDoesNotExist exc) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exc.getMessage());
        }
    }

    @DeleteMapping("banners/delete/{id}")
    public ResponseEntity<String> deleteBanner(@PathVariable Long id) {
        try{
            return ResponseEntity.status(HttpStatus.OK).body(bannerManagerService.deleteBanner(id));
        } catch (BannerNotFoundException exc) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exc.getMessage());
        }
    }

    @GetMapping("/banners/search")
    public ResponseEntity<List<Banner>> searchBanner(@RequestParam String name) {

        try {
            return ResponseEntity.ok(bannerManagerService.searchBanner(name));
        } catch (IllegalArgumentException exc) {
            return ResponseEntity.noContent().build();
        } catch (BannerNotFoundException exc) {
            return ResponseEntity.noContent().build();
        }

    }

    @PutMapping("/banners/update")
    public ResponseEntity<String> updateBanner(@RequestBody BannerDTO bannerDTO) {

        try {
            return ResponseEntity.ok().body(bannerManagerService.updateBanner(bannerDTO));
        } catch (BannerAlreadyExistsException exc) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(exc.getMessage());
        } catch (BannerNotFoundException exc) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exc.getMessage());
        }
    }




}
