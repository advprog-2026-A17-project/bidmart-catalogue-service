package id.ac.ui.cs.advprog.bidmartcatalogueservice.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AuthInterceptorTest {

    private AuthInterceptor interceptor;
    private final String jwtSecret = "catalogue-test-secret-key-catalogue-test-secret-key";

    @BeforeEach
    void setUp() {
        interceptor = new AuthInterceptor(jwtSecret);
    }

    @Test
    void allowsGetAndOptionsMethodsWithoutAuth() throws Exception {
        MockHttpServletRequest requestGet = new MockHttpServletRequest("GET", "/api/v1/catalogue/listings");
        MockHttpServletResponse responseGet = new MockHttpServletResponse();
        assertTrue(interceptor.preHandle(requestGet, responseGet, new Object()));

        MockHttpServletRequest requestOptions = new MockHttpServletRequest("OPTIONS", "/api/v1/catalogue/listings");
        MockHttpServletResponse responseOptions = new MockHttpServletResponse();
        assertTrue(interceptor.preHandle(requestOptions, responseOptions, new Object()));
    }

    @Test
    void acceptsAdminRouteWithGatewayAdminRole() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/catalogue/listings/123/admin/close");
        request.addHeader("X-User-Id", "admin-1");
        request.addHeader("X-User-Roles", "ADMIN");
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertTrue(interceptor.preHandle(request, response, new Object()));
    }

    @Test
    void rejectsAdminRouteWithGatewayWithoutAdminRole() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/catalogue/listings/123/admin/close");
        request.addHeader("X-User-Id", "seller-1");
        request.addHeader("X-User-Roles", "SELLER");
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertFalse(interceptor.preHandle(request, response, new Object()));
        assertEquals(403, response.getStatus());
    }

    @Test
    void acceptsNonAdminRouteWithGatewayUserId() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/catalogue/listings");
        request.addHeader("X-User-Id", "seller-1");
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertTrue(interceptor.preHandle(request, response, new Object()));
    }

    @Test
    void rejectsDirectCallWithoutAuthHeader() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/catalogue/listings");
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertFalse(interceptor.preHandle(request, response, new Object()));
        assertEquals(401, response.getStatus());
    }

    @Test
    void rejectsDirectCallWithInvalidAuthHeaderFormat() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/catalogue/listings");
        request.addHeader("Authorization", "InvalidTokenFormat");
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertFalse(interceptor.preHandle(request, response, new Object()));
        assertEquals(401, response.getStatus());
    }

    @Test
    void acceptsSellerTokenSignedWithConfiguredSecret() throws Exception {
        String token = Jwts.builder()
                .subject("seller-1")
                .claim("roles", List.of(Map.of("name", "SELLER")))
                .signWith(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)))
                .compact();

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/catalogue/listings");
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertTrue(interceptor.preHandle(request, response, new Object()));
        assertEquals("seller-1", request.getAttribute("X-User-Id"));
    }

    @Test
    void acceptsSellerTokenWithRolesAsStringList() throws Exception {
        String token = Jwts.builder()
                .subject("seller-1")
                .claim("roles", List.of("SELLER"))
                .signWith(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)))
                .compact();

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/catalogue/listings");
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertTrue(interceptor.preHandle(request, response, new Object()));
    }

    @Test
    void rejectsValidTokenWithoutSellerRole() throws Exception {
        String token = Jwts.builder()
                .subject("buyer-1")
                .claim("roles", List.of("BUYER"))
                .signWith(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)))
                .compact();

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/catalogue/listings");
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertFalse(interceptor.preHandle(request, response, new Object()));
        assertEquals(403, response.getStatus());
    }

    @Test
    void acceptsAdminTokenForAdminRoute() throws Exception {
        String token = Jwts.builder()
                .subject("admin-1")
                .claim("roles", List.of(Map.of("name", "ADMIN")))
                .signWith(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)))
                .compact();

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/catalogue/listings/123/admin/close");
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertTrue(interceptor.preHandle(request, response, new Object()));
        assertEquals("admin-1", request.getAttribute("X-User-Id"));
    }
    
    @Test
    void acceptsAdminTokenWithRolesAsStringForAdminRoute() throws Exception {
        String token = Jwts.builder()
                .subject("admin-1")
                .claim("roles", List.of("ADMIN"))
                .signWith(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)))
                .compact();

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/catalogue/listings/123/admin/close");
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertTrue(interceptor.preHandle(request, response, new Object()));
    }

    @Test
    void rejectsExpiredToken() throws Exception {
        String token = Jwts.builder()
                .subject("seller-1")
                .claim("roles", List.of("SELLER"))
                .expiration(new Date(System.currentTimeMillis() - 10000))
                .signWith(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)))
                .compact();

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/catalogue/listings");
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertFalse(interceptor.preHandle(request, response, new Object()));
        assertEquals(401, response.getStatus());
    }
}
