package com.link.qwiklink.controller;

import com.link.qwiklink.dtos.ResponseBody;
import com.link.qwiklink.dtos.SignUpRequest;
import com.link.qwiklink.dtos.UserCreatedDto;
import com.link.qwiklink.models.User;
import com.link.qwiklink.services.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<ResponseBody<UserCreatedDto>> createUser(@Valid @RequestBody SignUpRequest signUpRequest) {
        User created = userService.createUser(signUpRequest);

        UserCreatedDto data = new UserCreatedDto(
                created.getId(),
                created.getUserName(),
                created.getRole()
        );

        ResponseBody<UserCreatedDto> body =
                ResponseBody.created("Registration successfully", data);

        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }
}
