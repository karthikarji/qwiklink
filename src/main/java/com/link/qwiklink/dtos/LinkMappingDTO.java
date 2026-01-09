package com.link.qwiklink.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class LinkMappingDTO {
    private Long id;
    private String originalUrl;
    private String shortUrl;
    private long clickCount;
    private LocalDateTime createdAt;
    private String username;
    private boolean alreadyExists;
}
