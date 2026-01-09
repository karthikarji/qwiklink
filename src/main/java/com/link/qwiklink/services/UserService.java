package com.link.qwiklink.services;

import com.link.qwiklink.auth.jwt.AuthResponse;
import com.link.qwiklink.auth.jwt.JwtService;
import com.link.qwiklink.auth.user.AppUserDetails;
import com.link.qwiklink.dtos.SignInRequest;
import com.link.qwiklink.dtos.SignUpRequest;
import com.link.qwiklink.exceptions.AppException;
import com.link.qwiklink.exceptions.ErrorType;
import com.link.qwiklink.models.User;
import com.link.qwiklink.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import static org.springframework.http.HttpStatus.CONFLICT;


@Service
@AllArgsConstructor
public class UserService {
    private PasswordEncoder passwordEncoder;
    private UserRepository userRepository;
    private AuthenticationManager authManager;
    private JwtService jwtService;

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

    /**
     * Authenticates a user using username and password credentials.
     *
     * Flow:
     * - Delegates authentication to Spring Security's AuthenticationManager
     * - On success, stores the Authentication in the SecurityContext
     * - Generates a JWT access token for the authenticated user
     *
     * Errors:
     * - Invalid credentials → 401 (AUTH)
     * - Other authentication failures → 401 (AUTH)
     */
    public AuthResponse authenticateUser(SignInRequest request) {
        try {
            /**
             * Delegate authentication to Spring Security.
             * This will internally:
             * - load the user via UserDetailsService
             * - verify the password using PasswordEncoder
             */
            Authentication authentication = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUserName(),
                            request.getPassword()
                    )
            );

            /**
             * Store authentication in SecurityContext so downstream
             * components can access the authenticated principal if needed.
             */
            SecurityContextHolder.getContext().setAuthentication(authentication);

            /**
             * Extract the authenticated application user
             * At this point authentication is guaranteed to be successful
             */
            AppUserDetails appUser = (AppUserDetails) authentication.getPrincipal();

             /**
              * Issue a signed JWT token for the authenticated user
            */
            String token = jwtService.issueToken(appUser);

            return new AuthResponse(token);

        } catch (BadCredentialsException ex) {
            // Wrong username/password
            throw new AppException(
                    HttpStatus.UNAUTHORIZED,
                    ErrorType.AUTH,
                    "Invalid username or password",
                    ex
            );
        } catch (AuthenticationException ex) {
            // Any other auth failure
            throw new AppException(
                    HttpStatus.UNAUTHORIZED,
                    ErrorType.AUTH,
                    "Authentication failed",
                    ex
            );
        }
    }

    public User findByUsername(String name) {
        return userRepository.findByUserName(name)
                .orElseThrow(() -> new AppException(
                        HttpStatus.NOT_FOUND,
                        ErrorType.NOT_FOUND,
                        "User not found with username: " + name
                ));
    }
}
