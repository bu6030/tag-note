package com.example.tagnote.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    public String getUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = null;

        // Log authentication details
        if (authentication != null) {
            logger.info("Authentication details: authenticated={}, principal={}",
                authentication.isAuthenticated(), authentication.getPrincipal());

            // Log user authorities/roles
            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
            if (authorities != null && !authorities.isEmpty()) {
                StringBuilder roles = new StringBuilder();
                for (GrantedAuthority authority : authorities) {
                    if (roles.length() > 0)
                        roles.append(", ");
                    roles.append(authority.getAuthority());
                }
                logger.info("User roles/authorities: {}", roles.toString());
            } else {
                logger.warn("No authorities found for user");
            }
        } else {
            logger.warn("No authentication found in SecurityContext");
        }

        if (authentication != null && authentication.getPrincipal() instanceof User) {
            username = ((User) authentication.getPrincipal()).getUsername();
        } else if (authentication != null && authentication.getPrincipal() instanceof OAuth2User) {
            username = ((OAuth2User) authentication.getPrincipal())
                .getAttribute("preferred_username");
        }

        logger.info("Resolved username: {}", username);
        return username;
    }
}