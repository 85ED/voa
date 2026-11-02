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
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
public class PublicRateLimitFilter extends OncePerRequestFilter {

    private final int limitPerMinute;
    private final Map<String, Window> windows = new ConcurrentHashMap<>();

    public PublicRateLimitFilter(VoaProperties properties) {
        this.limitPerMinute = Math.max(1, properties.publicApi().rateLimitPerMinute());
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return !path.startsWith("/api/v1/public/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String key = clientKey(request);
        long minute = Instant.now().getEpochSecond() / 60;
        Window window = windows.compute(key, (k, existing) -> {
            if (existing == null || existing.minute != minute) {
                return new Window(minute, new AtomicInteger(0));
            }
            return existing;
        });
        int count = window.count.incrementAndGet();
        if (count > limitPerMinute) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"rate_limit_exceeded\"}");
            return;
        }
        filterChain.doFilter(request, response);
    }

    private static String clientKey(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private static final class Window {
        private final long minute;
        private final AtomicInteger count;

        private Window(long minute, AtomicInteger count) {
            this.minute = minute;
            this.count = count;
        }
    }
}
