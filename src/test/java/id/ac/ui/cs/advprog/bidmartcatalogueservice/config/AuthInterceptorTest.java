package id.ac.ui.cs.advprog.bidmartcatalogueservice.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthInterceptorTest {

    @Test
    void acceptsSellerTokenSignedWithConfiguredSecret() throws Exception {
        String jwtSecret = "catalogue-test-secret-key-catalogue-test-secret-key";
        String token = Jwts.builder()
                .subject("seller-1")
                .claim("roles", List.of(Map.of("name", "SELLER")))
                .signWith(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)))
                .compact();

        AuthInterceptor interceptor = new AuthInterceptor(jwtSecret);
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/catalogue/listings");
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader("Authorization", "Bearer " + token);

        assertTrue(interceptor.preHandle(request, response, new Object()));
    }
}
