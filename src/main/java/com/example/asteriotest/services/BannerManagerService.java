package com.example.asteriotest.services;

import com.example.asteriotest.exception.bannerExceptions.BannerAlreadyExistsException;
import com.example.asteriotest.exception.bannerExceptions.BannerAlreadyShownException;
import com.example.asteriotest.exception.bannerExceptions.BannerNotFoundException;
import com.example.asteriotest.exception.bannerExceptions.InsertedCategoryDoesNotExist;
import com.example.asteriotest.model.Banner;
import com.example.asteriotest.model.Category;
import com.example.asteriotest.model.DTO.BannerDTO;
import com.example.asteriotest.model.RequestJournal;
import com.example.asteriotest.repository.BannerRepository;
import com.example.asteriotest.repository.CategoriesRepository;
import com.example.asteriotest.repository.RequestJournalRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Logger;

@Service
public class BannerManagerService {
    private final CategoriesRepository categoriesRepo;
    private final BannerRepository bannerRepo;
    private final RequestJournalRepository journalRepo;

    private final Logger logger = Logger.getLogger(BannerManagerService.class.getName());

    public BannerManagerService(CategoriesRepository categoriesRepo, BannerRepository bannerRepo, RequestJournalRepository journalRepo) {
        this.categoriesRepo = categoriesRepo;
        this.bannerRepo = bannerRepo;
        this.journalRepo = journalRepo;
    }

    /**
     * The method of adding a banner, a banner can only be added if a banner with the same name has not been created before.
     * Categories to be attached to the banner must be created.
     * */
    public String addBanner(Banner banner) {
        if (bannerRepo.existsByNameBanner(banner.getNameBanner())) { // Check if the banner already exists
            logger.info("Banner with this name already exists");
            throw new BannerAlreadyExistsException("ERROR 409: Banner with this name already exists");
        }

        Set<Category> categories = new HashSet<>(banner.getCategories());
        for (Category category : categories) {
            if (!categoriesRepo.existsById(category.getId())) {
                logger.info("One of the categories does not exist");
                throw new InsertedCategoryDoesNotExist("ERROR 404: The category inserted into the banner does not exist");
            }
        }

        bannerRepo.save(banner);
        logger.info("Banner has been created");
        return "Banner has been created with properties: " + banner.toString();
    }

    /**
     *  the function of "logical" deletion. To comply with the uniqueness of records,
     *  the time when the record was deleted is added to the properties of the deleted record
     * */
    public String deleteBanner(Long id) {
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

            return "Banner " + banner.getNameBanner() + " has been deleted";
        } else {
            throw new BannerNotFoundException("ERROR 404: Removed banner does not exist");
        }

    }

    /**
     *  Banner search request.
     *  The method is not case sensitive.
     */
    public List<Banner> searchBanner(String name) {
        if (name == null || name.isBlank()) { //If the parameter is empty, stop execution
            throw new IllegalArgumentException("Category name cannot be empty");
        }

        name = name.toLowerCase();

        Optional<List<Banner>> banners = bannerRepo.findAllByNameBannerContains(name);

        if (banners.isEmpty()) {
            throw new BannerNotFoundException("Category with this name does not exist");
        }

        return banners.get();
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
    public String updateBanner(BannerDTO bannerDTO) {
        Banner banner = bannerDTO.getBanner();

        //If the banner name has been changed, we should check if there is a task with the same name in the database
        Optional<Banner> bannerForCheck = bannerRepo.findByNameBanner(banner.getNameBanner());
        if (bannerForCheck.isPresent()) {
            if (!bannerForCheck.get().getId().equals(banner.getId())) { // The banner we are updating has a name taken by another banner
                throw new BannerAlreadyExistsException("Banner with this name already exists");
            }
        }

        if (bannerRepo.existsById(banner.getId())) { // check if the banner exists
            Set<Long> categoriesId = bannerDTO.getCategoriesId();
            for (Long id : categoriesId) {
                Optional<Category> category = categoriesRepo.findById(id);
                if (category.isPresent()) { // Accepted category id is inserted into the updated banner
                    banner.putCategory(category.get());
                }
            }
            bannerRepo.save(banner);
            logger.info("Banner has been edited");
            return "Banner has been edited";
        } else {
            logger.info("Banner with this ID not found");
            throw new BannerNotFoundException("Banner with this ID not found");
        }
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
    public String bid(HttpServletRequest servletRequest, @RequestParam("cat") List<String> categories) {


        Optional<List<Banner>> optionalBanners = bannerRepo.findAllByCategories_requestIdIn(categories); // Looking for banners by category
        String userAgent = servletRequest.getHeader("User-Agent");
        String ip = servletRequest.getRemoteAddr();

        if (optionalBanners.get().size() > 0) { // Check if there is at least one banner

            //sort banners by price
            List<Banner> banners = optionalBanners.get();
            Comparator<Banner> priceComparator = Comparator.comparing(Banner::getPrice);
            Collections.sort(banners, priceComparator);

            // get request parameters
            LocalDate today = LocalDate.now();
            LocalDateTime startOfDay = today.atStartOfDay();
            LocalDateTime endOfDay = today.atTime(23, 59, 59);

            Banner finalBanner = null;
            for (Banner current : banners) {

                /*
                 * Search parameters: IP and User-agent are the same as the current one, the record was made within 24 hours
                 * */
                Optional<RequestJournal> journalRecord = journalRepo.findByBannerAndIpAddressAndUserAgentAndRequestTimeBetween(current, ip, userAgent, startOfDay, endOfDay);
                if (journalRecord.isEmpty()) { // Checking if a banner with these properties has been shown before (if yes, it was logged)
                    logger.info("now banner" + current.getNameBanner() + " is finalBanner");
                    finalBanner = current; // Choosing a banner
                }
            }

            // If there is no matching banner, returns error 204
            if (finalBanner == null) {
                String errorBanner = "ERROR 204: This banner has already been shown to this user before";
                logger.info(errorBanner);

                RequestJournal log = new RequestJournal(ip, userAgent,
                        LocalDateTime.now(), errorBanner);

                journalRepo.save(log);

                throw new BannerAlreadyShownException("ERROR 204: This banner has already been shown to this user before");
            }


            RequestJournal log = new RequestJournal(ip, userAgent,
                    LocalDateTime.now(), finalBanner,
                    finalBanner.getPrice());

            journalRepo.save(log);

            return finalBanner.getText();
        } else {
            String errorBody = "ERROR 204: banner with this category not found";
            logger.info(errorBody);

            RequestJournal log = new RequestJournal(ip, userAgent,
                    LocalDateTime.now(), errorBody);

            journalRepo.save(log);

            throw new BannerNotFoundException(errorBody);
        }
    }
}
