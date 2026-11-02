package br.jlturismo.voa.modules.core.web;

import br.jlturismo.voa.modules.core.config.VoaProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 15)
public class LoginRateLimitFilter extends OncePerRequestFilter {

    private final InMemoryRateLimiter rateLimiter;
    private final int limitPerMinute;

    public LoginRateLimitFilter(InMemoryRateLimiter rateLimiter, VoaProperties properties) {
        this.rateLimiter = rateLimiter;
        this.limitPerMinute = Math.max(1, properties.auth().loginRateLimitPerMinute());
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String path = request.getRequestURI();
        return !path.equals("/api/v1/auth/login")
                && !path.equals("/api/v1/auth/forgot-password")
                && !path.equals("/api/v1/auth/reset-password");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String key = "login:" + clientKey(request);
        if (!rateLimiter.tryConsume(key, limitPerMinute)) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"login_rate_limit_exceeded\"}");
            return;
        }
        filterChain.doFilter(request, response);
    }

    static String clientKey(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
