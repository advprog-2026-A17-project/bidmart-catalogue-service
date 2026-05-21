package id.ac.ui.cs.advprog.bidmartcatalogueservice.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class CatalogAccessPolicy {

    private static final String ADMIN_ROLE = "ADMIN";

    public void requireAdmin(String rolesHeader) {
        if (!hasRole(rolesHeader, ADMIN_ROLE)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only administrators can perform this catalogue action");
        }
    }

    private boolean hasRole(String rolesHeader, String role) {
        if (rolesHeader == null || rolesHeader.isBlank()) {
            return false;
        }
        for (String value : rolesHeader.split(",")) {
            if (role.equalsIgnoreCase(value.trim())) {
                return true;
            }
        }
        return false;
    }
}
