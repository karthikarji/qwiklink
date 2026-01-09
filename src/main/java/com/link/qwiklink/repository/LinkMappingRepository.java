package com.link.qwiklink.repository;

import com.link.qwiklink.models.LinkMapping;
import com.link.qwiklink.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LinkMappingRepository extends JpaRepository<LinkMapping, Long> {
    Optional<LinkMapping> findByShortLink(String shortLink);

    Optional<LinkMapping> findByUser_IdAndActualLink(Long userId, String actualLink);

    List<LinkMapping> findAllByUser_IdOrderByCreatedAtDesc(Long userId);

    List<LinkMapping> findByUser(User user);

    boolean existsByShortLink(String shortLink);
}
