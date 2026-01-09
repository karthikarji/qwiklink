package com.link.qwiklink.services;

import com.link.qwiklink.dtos.LinkMappingDTO;
import com.link.qwiklink.dtos.ShortLinkRequest;
import com.link.qwiklink.exceptions.AppException;
import com.link.qwiklink.exceptions.ErrorType;
import com.link.qwiklink.models.LinkMapping;
import com.link.qwiklink.models.User;
import com.link.qwiklink.repository.LinkMappingRepository;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class LinkMappingService {
    @Autowired
    private LinkMappingRepository linkMappingRepository;

    // Example: https://qwik.link  (used to return full short URL)
    @Value("${app.short-base-url:http://localhost:8080}")
    private String shortBaseUrl;

    private static final String BASE62 = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final int CODE_LENGTH = 8;
    private static final int MAX_ATTEMPTS = 6;

    /**
     * Creates a new short URL mapping.
     * Uses collision-safe code generation and optional reuse of existing mapping.
     */
    public LinkMappingDTO createShortUrl(ShortLinkRequest request, User user) {
        String normalizedUrl = normalizeUrl(request.getActualUrl());

        Optional<LinkMapping> existing =
                linkMappingRepository.findByUser_IdAndActualLink(user.getId(), normalizedUrl);
        if (existing.isPresent()) {
            return toDto(existing.get(), true);
        }


        // Generate + save with collision handling
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            String code = generateCode(CODE_LENGTH);

            LinkMapping mapping = new LinkMapping();
            mapping.setActualLink(normalizedUrl);
            mapping.setShortLink(code);
            mapping.setUser(user);
            mapping.setClickCount(0);
            mapping.setCreatedAt(LocalDateTime.now());

            try {
                LinkMapping saved = linkMappingRepository.save(mapping);
                return toDto(saved, false);
            } catch (DataIntegrityViolationException ex) {
                // Usually means unique constraint violated for shortLink (collision)
                // Retry with a new code.
                if (attempt == MAX_ATTEMPTS) {
                    throw new AppException(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            ErrorType.INTERNAL,
                            "Could not generate a unique short URL. Please try again.",
                            ex
                    );
                }
            }
        }

        // Should never reach here
        throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, ErrorType.INTERNAL, "Unexpected error");
    }

    private String generateShortUrl() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

        Random random = new Random();
        StringBuilder shortUrl = new StringBuilder(8);

        for (int i = 0; i < 8; i++) {
            shortUrl.append(characters.charAt(random.nextInt(characters.length())));
        }
        return shortUrl.toString();
    }

    /**
     * Basic URL normalization:
     * - trims whitespace
     * - validates URI format
     * - keeps scheme/host/path/query intact
     *
     * (At large scale you may also lowercase host, remove default ports, etc.)
     */
    private String normalizeUrl(String originalUrl) {
        try {
            String trimmed = originalUrl.trim();
            URI uri = URI.create(trimmed);

            // Require http/https for safety + consistency
            String scheme = uri.getScheme();
            if (scheme == null || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))) {
                throw new AppException(HttpStatus.BAD_REQUEST, ErrorType.VALIDATION,
                        "URL must start with http:// or https://");
            }
            return trimmed;
        } catch (IllegalArgumentException ex) {
            throw new AppException(HttpStatus.BAD_REQUEST, ErrorType.VALIDATION, "Invalid URL format", ex);
        }
    }

    /**
     * Generates a Base62 random code.
     * ThreadLocalRandom is fast and safe under concurrency.
     */
    private String generateCode(int length) {
        StringBuilder sb = new StringBuilder(length);
        var rnd = ThreadLocalRandom.current();
        for (int i = 0; i < length; i++) {
            sb.append(BASE62.charAt(rnd.nextInt(BASE62.length())));
        }
        return sb.toString();
    }

    public List<LinkMappingDTO> getUrlsByUser(User user) {
        return linkMappingRepository.findAllByUser_IdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(mapping -> toDto(mapping, true))
                .toList();
    }

    private LinkMappingDTO toDto(LinkMapping mapping, Boolean alreadyExists) {
        String fullShortUrl = shortBaseUrl.endsWith("/")
                ? shortBaseUrl + mapping.getShortLink()
                : shortBaseUrl + "/" + mapping.getShortLink();

        return new LinkMappingDTO(
                mapping.getId(),
                mapping.getActualLink(),
                fullShortUrl,
                mapping.getClickCount(),
                mapping.getCreatedAt(),
                mapping.getUser().getUserName(),
                alreadyExists
        );
    }


}
