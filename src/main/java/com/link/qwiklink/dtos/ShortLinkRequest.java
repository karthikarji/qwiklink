package com.link.qwiklink.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

@Data
public class ShortLinkRequest {
    @NotBlank(message = "ActualUrl is required")
    @URL(message = "ActualUrl must be a valid URL (include http/https)")
    private String actualUrl;
}
