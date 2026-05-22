package id.ac.ui.cs.advprog.bidmartcatalogueservice.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthInterceptorTest {

    private static final String JWT_SECRET = "catalogue-test-secret-key-catalogue-test-secret-key";

    @Test
    void acceptsSellerTokenSignedWithConfiguredSecret() throws Exception {
        String token = tokenWithRoles(List.of(Map.of("name", "SELLER")));

        AuthInterceptor interceptor = new AuthInterceptor(JWT_SECRET);
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/catalogue/listings");
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader("Authorization", "Bearer " + token);

        assertTrue(interceptor.preHandle(request, response, new Object()));
        assertEquals("seller-1", request.getAttribute("X-User-Id"));
    }

    @Test
    void allowsPublicReadAndOptionsRequests() throws Exception {
        AuthInterceptor interceptor = new AuthInterceptor(JWT_SECRET);

        assertTrue(interceptor.preHandle(
                new MockHttpServletRequest("GET", "/api/v1/catalogue/listings"),
                new MockHttpServletResponse(),
                new Object()
        ));
        assertTrue(interceptor.preHandle(
                new MockHttpServletRequest("OPTIONS", "/api/v1/catalogue/listings"),
                new MockHttpServletResponse(),
                new Object()
        ));
    }

    @Test
    void allowsGatewayUserOnSellerRoutesWithoutJwt() throws Exception {
        AuthInterceptor interceptor = new AuthInterceptor(JWT_SECRET);
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/catalogue/listings");
        request.addHeader("X-User-Id", "seller-1");

        assertTrue(interceptor.preHandle(request, new MockHttpServletResponse(), new Object()));
    }

    @Test
    void requiresAdminRoleForGatewayAdminRoutes() throws Exception {
        AuthInterceptor interceptor = new AuthInterceptor(JWT_SECRET);
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/catalogue/admin/listings/1/approve");
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader("X-User-Id", "seller-1");
        request.addHeader("X-User-Roles", "SELLER");

        assertFalse(interceptor.preHandle(request, response, new Object()));
        assertEquals(403, response.getStatus());

        request = new MockHttpServletRequest("POST", "/api/v1/catalogue/admin/listings/1/approve");
        response = new MockHttpServletResponse();
        request.addHeader("X-User-Id", "admin-1");
        request.addHeader("X-User-Roles", "BUYER,ADMIN");

        assertTrue(interceptor.preHandle(request, response, new Object()));
    }

    @Test
    void rejectsMissingInvalidAndNonSellerJwt() throws Exception {
        AuthInterceptor interceptor = new AuthInterceptor(JWT_SECRET);
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/catalogue/listings");
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertFalse(interceptor.preHandle(request, response, new Object()));
        assertEquals(401, response.getStatus());

        request = new MockHttpServletRequest("POST", "/api/v1/catalogue/listings");
        response = new MockHttpServletResponse();
        request.addHeader("Authorization", "Bearer invalid");

        assertFalse(interceptor.preHandle(request, response, new Object()));
        assertEquals(401, response.getStatus());

        request = new MockHttpServletRequest("POST", "/api/v1/catalogue/listings");
        response = new MockHttpServletResponse();
        request.addHeader("Authorization", "Bearer " + tokenWithRoles(List.of("BUYER")));

        assertFalse(interceptor.preHandle(request, response, new Object()));
        assertEquals(403, response.getStatus());
    }

    @Test
    void allowsAdminJwtOnAdminRoutes() throws Exception {
        AuthInterceptor interceptor = new AuthInterceptor(JWT_SECRET);
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/catalogue/admin/listings/1/approve");
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader("Authorization", "Bearer " + tokenWithRoles(List.of("ADMIN")));

        assertTrue(interceptor.preHandle(request, response, new Object()));
        assertEquals("seller-1", request.getAttribute("X-User-Id"));
    }

    private String tokenWithRoles(List<?> roles) {
        return Jwts.builder()
                .subject("seller-1")
                .claim("roles", roles)
                .signWith(Keys.hmacShaKeyFor(JWT_SECRET.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }
}
