package id.ac.ui.cs.advprog.bidmartcatalogueservice.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private static final String JWT_SECRET = "bidmart-auth-secret-key-bidmart-auth-secret-key";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String method = request.getMethod();

        // GET dan OPTIONS tidak memerlukan autentikasi (katalog terbuka untuk publik)
        if ("GET".equalsIgnoreCase(method) || "OPTIONS".equalsIgnoreCase(method)) {
            return true;
        }

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\": \"Token tidak ditemukan. Harap login terlebih dahulu.\"}");
            response.setContentType("application/json");
            return false;
        }

        String token = authHeader.substring(7);

        try {
            SecretKey key = Keys.hmacShaKeyFor(JWT_SECRET.getBytes(StandardCharsets.UTF_8));
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

            if (!isSeller) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("{\"error\": \"Akses ditolak. Hanya penjual yang dapat mengelola katalog.\"}");
                response.setContentType("application/json");
                return false;
            }

            return true;

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\": \"Token tidak valid atau sudah kedaluwarsa.\"}");
            response.setContentType("application/json");
            return false;
        }
    }
}
