package com.example.asteriotest.repository;

import com.example.asteriotest.model.Banner;
import com.example.asteriotest.model.RequestJournal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RequestJournalRepository extends JpaRepository<RequestJournal, Long> {
    Optional<RequestJournal> findByBannerAndIpAddressAndUserAgent(Banner banner, String ipAddress, String userAgent);
}
