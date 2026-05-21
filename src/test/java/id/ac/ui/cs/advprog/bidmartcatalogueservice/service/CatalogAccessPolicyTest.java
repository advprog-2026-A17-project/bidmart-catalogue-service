package id.ac.ui.cs.advprog.bidmartcatalogueservice.service;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CatalogAccessPolicyTest {

    private final CatalogAccessPolicy policy = new CatalogAccessPolicy();

    @Test
    void requireAdminShouldPassForAdminRole() {
        assertDoesNotThrow(() -> policy.requireAdmin("BUYER,ADMIN"));
    }

    @Test
    void requireAdminShouldRejectMissingRoles() {
        assertThrows(ResponseStatusException.class, () -> policy.requireAdmin(null));
        assertThrows(ResponseStatusException.class, () -> policy.requireAdmin("SELLER"));
    }
}
