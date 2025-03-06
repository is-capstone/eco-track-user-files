package com.enzulode.file.util;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.security.Principal;

@Component
public class SecurityContextHelper {

  public String findUserName() {
    Principal principal = SecurityContextHolder.getContext().getAuthentication();
    if (principal.getName() == null)
      throw new AccessDeniedException("Unable to perform operation: no information about the authentication is present");
    return principal.getName();
  }

  public boolean isAdmin() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null
        || !authentication.isAuthenticated()
        && authentication instanceof AnonymousAuthenticationToken) {
      return false;
    }

    return authentication.getAuthorities().stream()
        .anyMatch(
            grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN")
        );
  }
}
