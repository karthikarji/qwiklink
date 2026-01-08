package com.link.qwiklink.auth.user;

import com.link.qwiklink.models.User;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * Spring Security wrapper around our domain {@link User}.
 *
 * Why this exists:
 * Spring Security works with the {@link UserDetails} interface, not your own User entity.
 * This class adapts your entity into what Spring Security expects during authentication/authorization.
 */
@Data
@NoArgsConstructor
public class AppUserDetails implements UserDetails {
    private static final long serialVersionUID = 1L;

    /**
     * Database identifier - useful for logging/auditing, and for attaching to tokens (if you choose).
     */
    private Long id;
    /**
     * Username used by Spring Security as the principal name.
     */
    private String userName;
    /**
     * Kept for app usage
     */
    private String emailId;
    /**
     * Password as stored in DB. Should be hashed/encoded.
     */
    private String password;
    /**
     * Authorities represent permissions/roles in Spring Security terms.
     * Example: ROLE_USER, ROLE_ADMIN
     */
    private Collection<? extends GrantedAuthority> authorities;

    private AppUserDetails(Long id, String userName, String emailId, String password, Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.userName = userName;
        this.emailId = emailId;
        this.password = password;
        this.authorities = authorities;
    }

    /**
     * Factory method to create security principal from our domain user.
     */
    public static AppUserDetails build(User user) {
        String role = user.getRole();
        GrantedAuthority authority = new SimpleGrantedAuthority(role);
        return new AppUserDetails(
                user.getId(),
                user.getUserName(),
                user.getEmail(),
                user.getPassword(),
                Collections.singleton(authority)
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public @Nullable String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return userName;
    }
}
