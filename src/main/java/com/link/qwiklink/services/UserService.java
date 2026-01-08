package com.link.qwiklink.services;

import com.link.qwiklink.dtos.SignUpRequest;
import com.link.qwiklink.exceptions.AppException;
import com.link.qwiklink.exceptions.ErrorType;
import com.link.qwiklink.models.User;
import com.link.qwiklink.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import static org.springframework.http.HttpStatus.CONFLICT;


@Service
@AllArgsConstructor
public class UserService {
    private PasswordEncoder passwordEncoder;
    private UserRepository userRepository;

    /**
     * Register a new user.
     * - validates username uniqueness
     * - encodes password
     * - applies default role
     */
    public User createUser(SignUpRequest request) {
        // If you already have a different lookup method, adjust accordingly
        if (userRepository.findByUserName(request.getUserName()).isPresent()) {
            throw new AppException(CONFLICT, ErrorType.CONFLICT, "Username exists");
        }

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new AppException(CONFLICT, ErrorType.CONFLICT, "Email exists");
        }

        User user = new User();
        user.setUserName(request.getUserName());
        user.setEmail(request.getEmail());
        user.setRole("ROLE_USER");
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        return userRepository.save(user);
    }
}
