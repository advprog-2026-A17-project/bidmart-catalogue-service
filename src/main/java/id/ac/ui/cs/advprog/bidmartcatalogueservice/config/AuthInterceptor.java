package id.ac.ui.cs.advprog.bidmartcatalogueservice.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private static final String HEADER_USER_ID = "X-User-Id";
    private static final String HEADER_USER_ROLES = "X-User-Roles";
    private static final Set<String> SELLER_ROLES = Set.of("SELLER");
    private static final Set<String> ADMIN_ROLES = Set.of("ADMIN");

    private final String jwtSecret;

    public AuthInterceptor(
            @Value("${bidmart.auth.jwt-secret:bidmart-auth-secret-key-bidmart-auth-secret-key}") String jwtSecret
    ) {
        this.jwtSecret = jwtSecret;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String method = request.getMethod();

        // GET dan OPTIONS tidak memerlukan autentikasi (katalog terbuka untuk publik)
        if ("GET".equalsIgnoreCase(method) || "OPTIONS".equalsIgnoreCase(method)) {
            return true;
        }

        boolean adminRoute = request.getRequestURI().contains("/admin/");

        // Strategy 1: Gateway already enforced granular permissions (listing:create, listing:manage, admin:users).
        String gatewayUserId = request.getHeader(HEADER_USER_ID);
        if (gatewayUserId != null && !gatewayUserId.isBlank()) {
            if (adminRoute) {
                String rolesHeader = request.getHeader(HEADER_USER_ROLES);
                if (hasRole(rolesHeader, ADMIN_ROLES)) {
                    return true;
                }
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("{\"error\": \"Akses ditolak. Hanya admin yang dapat mengelola rute admin katalog.\"}");
                response.setContentType("application/json");
                return false;
            }
            return true;
        }

        // Strategy 2: Fallback — parse JWT langsung (untuk direct calls tanpa gateway)
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\": \"Token tidak ditemukan. Harap login terlebih dahulu.\"}");
            response.setContentType("application/json");
            return false;
        }

        String token = authHeader.substring(7);

        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            // mengambil roles dari klaim JWT
            Object rolesObj = claims.get("roles");
            boolean isSeller = false;

            if (rolesObj instanceof List<?> rolesList) {
                for (Object roleItem : rolesList) {
                    if (roleItem instanceof Map<?, ?> roleMap) {
                        Object roleName = roleMap.get("name");
                        if ("SELLER".equals(roleName)) {
                            isSeller = true;
                            break;
                        }
                    } else if ("SELLER".equals(roleItem.toString())) {
                        isSeller = true;
                        break;
                    }
                }
            }

            boolean isAdmin = false;
            if (rolesObj instanceof List<?> rolesList) {
                for (Object roleItem : rolesList) {
                    if (roleItem instanceof Map<?, ?> roleMap) {
                        if ("ADMIN".equals(roleMap.get("name"))) {
                            isAdmin = true;
                            break;
                        }
                    } else if ("ADMIN".equals(roleItem.toString())) {
                        isAdmin = true;
                        break;
                    }
                }
            }

            if (adminRoute && isAdmin) {
                request.setAttribute(HEADER_USER_ID, claims.getSubject());
                return true;
            }

            if (!isSeller) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("{\"error\": \"Akses ditolak. Hanya penjual yang dapat mengelola katalog.\"}");
                response.setContentType("application/json");
                return false;
            }

            request.setAttribute(HEADER_USER_ID, claims.getSubject());
            return true;

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\": \"Token tidak valid atau sudah kedaluwarsa.\"}");
            response.setContentType("application/json");
            return false;
        }
    }

    /**
     * Cek apakah roles header dari gateway mengandung SELLER.
     * Format header: "SELLER" atau "BUYER,SELLER" (comma-separated).
     */
    private boolean hasRole(String rolesHeader, Set<String> allowedRoles) {
        if (rolesHeader == null || rolesHeader.isBlank()) {
            return false;
        }
        for (String role : rolesHeader.split(",")) {
            if (allowedRoles.contains(role.trim().toUpperCase())) {
                return true;
            }
        }
        return false;
    }
}
