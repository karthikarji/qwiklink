package com.link.qwiklink.controller;

import com.link.qwiklink.dtos.LinkMappingDTO;
import com.link.qwiklink.dtos.ResponseBody;
import com.link.qwiklink.dtos.ShortLinkRequest;
import com.link.qwiklink.models.User;
import com.link.qwiklink.services.LinkMappingService;
import com.link.qwiklink.services.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/links")
@AllArgsConstructor
public class LinkMappingController {
    private LinkMappingService linkMappingService;
    private UserService userService;

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ResponseBody<LinkMappingDTO>> createShortUrl(
            @Valid @RequestBody ShortLinkRequest request,
            Principal principal
    ){
        // principal name = username as per your security setup
        User user = userService.findByUsername(principal.getName());

        LinkMappingDTO dto = linkMappingService.createShortUrl(request, user);
        String msg = dto.isAlreadyExists() ? "Short URL Already available" : "Short URL created";
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseBody.created(msg, dto));
    }

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<LinkMappingDTO>> getUserUrls(Principal principal){
        User user = userService.findByUsername(principal.getName());
        List<LinkMappingDTO> urls = linkMappingService.getUrlsByUser(user);
        return ResponseEntity.ok(urls);
    }
}
