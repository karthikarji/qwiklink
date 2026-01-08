package com.link.qwiklink.auth.user;

import com.link.qwiklink.models.User;
import com.link.qwiklink.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Loads application users from the database for Spring Security.
 *
 * This service acts as a bridge between:
 *  - our domain User entity
 *  - Spring Security's authentication mechanism
 */
@Service
public class AppUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    /**
     * Called by Spring Security during authentication.
     * Retrieves user data and adapts it to {@link UserDetails}.
     */
    @Override
    @Transactional
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
        User user = userRepository.findByUserName(userName)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "No user found with username: " + userName
                        )
                );
        return AppUserDetails.build(user);
    }
}
