package com.example.asteriotest.controllers;

import com.example.asteriotest.model.Banner;
import com.example.asteriotest.model.Category;
import com.example.asteriotest.model.RequestJournal;
import com.example.asteriotest.repository.BannerRepository;
import com.example.asteriotest.repository.CategoriesRepository;
import com.example.asteriotest.repository.RequestJournalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.*;

@Controller
public class BannerShowController {
    BannerRepository bannerRepo;
    CategoriesRepository categoryRepo;
    RequestJournalRepository journalRepo;

    @Autowired
    public BannerShowController(BannerRepository bannerRepo, CategoriesRepository categoryRepo, RequestJournalRepository journalRepo) {
        this.bannerRepo = bannerRepo;
        this.categoryRepo = categoryRepo;
        this.journalRepo = journalRepo;
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


        Optional<List<Banner>> optionalBanners = bannerRepo.findAllByCategories_requestIdIn(categories); // Looking for banners by category
        String userAgent = servletRequest.getHeader("User-Agent");
        String ip = servletRequest.getRemoteAddr();

        if (optionalBanners.get().size() > 0) { // Check if there is at least one banner

        //sort banners by price
        List<Banner> banners = optionalBanners.get();
        Comparator<Banner> priceComparator = Comparator.comparing(Banner::getPrice);
        Collections.sort(banners, priceComparator);


        Banner finalBanner = null;
        for (Banner current : banners) {
            Optional<RequestJournal> journalRecord = journalRepo.findByBannerAndIpAddressAndUserAgent(current, ip, userAgent);
            if (journalRecord.isEmpty()) { // Checking if a banner with these properties has been shown before (if yes, it was logged)
                System.out.println("now banner" + current.getNameBanner() + " is finalBanner");
                finalBanner = current; // Choosing a banner
            }
        }

        // If there is no matching banner, returns error 204
        if (finalBanner == null) {
            String errorBanner = "ERROR 204: This banner has already been shown to this user before";
            System.out.println(errorBanner);

            RequestJournal log = new RequestJournal(ip, userAgent,
                    LocalDateTime.now(), errorBanner);

            journalRepo.save(log);

            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("ERROR 204: This banner has already been shown to this user before");
        }


//        Optional<Banner> optBanners = bannerRepo.findFirstByCategories_requestIdInOrderByPriceDesc(categories);
//        Banner banner = optBanners.get();


            RequestJournal log = new RequestJournal(ip, userAgent,
                    LocalDateTime.now(), finalBanner,
                    finalBanner.getPrice());

            journalRepo.save(log);

            return ResponseEntity.ok(finalBanner.getText());
        } else {
            String errorBody = "ERROR 204: banner with this category not found";
            System.out.println(errorBody);

            RequestJournal log = new RequestJournal(ip, userAgent,
                    LocalDateTime.now(), errorBody);

            journalRepo.save(log);

            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(errorBody);
        }
    }
}
