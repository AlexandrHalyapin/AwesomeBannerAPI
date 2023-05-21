package com.example.asteriotest.controllers;

import com.example.asteriotest.exception.bannerExceptions.BannerAlreadyShownException;
import com.example.asteriotest.exception.bannerExceptions.BannerNotFoundException;
import com.example.asteriotest.model.Banner;
import com.example.asteriotest.model.Category;
import com.example.asteriotest.model.RequestJournal;
import com.example.asteriotest.repository.BannerRepository;
import com.example.asteriotest.repository.CategoriesRepository;
import com.example.asteriotest.repository.RequestJournalRepository;
import com.example.asteriotest.services.BannerManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Controller
public class BannerShowController {
    BannerRepository bannerRepo;
    CategoriesRepository categoryRepo;
    RequestJournalRepository journalRepo;
    BannerManagerService bannerManagerService;

    public BannerShowController(BannerRepository bannerRepo, CategoriesRepository categoryRepo, RequestJournalRepository journalRepo, BannerManagerService bannerManagerService) {
        this.bannerRepo = bannerRepo;
        this.categoryRepo = categoryRepo;
        this.journalRepo = journalRepo;
        this.bannerManagerService = bannerManagerService;
    }

    /**
     * We get the banner text by one of the categories of the parameter.
     * If the banner matches at least ONE of the query categories,
     * its TEXT is returned as a response.
     * If several banners match the query parameters, the one with the highest PRICE is returned
     * The HTTP request log is also recorded,
     * the banner (and attached categories) is recorded,
     * as well as information about the source of the request
     * */
    @GetMapping("/bid")
    public ResponseEntity<String> bid(HttpServletRequest servletRequest, @RequestParam("cat") List<String> categories) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(bannerManagerService.bid(servletRequest, categories));
        } catch (BannerAlreadyShownException exc) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(exc.getMessage());
        } catch (BannerNotFoundException exc) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(exc.getMessage());
        }
    }
}
