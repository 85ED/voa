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
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
public class PublicRateLimitFilter extends OncePerRequestFilter {

    private final InMemoryRateLimiter rateLimiter;
    private final int readLimitPerMinute;
    private final int postLimitPerMinute;

    public PublicRateLimitFilter(InMemoryRateLimiter rateLimiter, VoaProperties properties) {
        this.rateLimiter = rateLimiter;
        this.readLimitPerMinute = Math.max(1, properties.publicApi().rateLimitPerMinute());
        this.postLimitPerMinute = Math.max(1, properties.publicApi().postRateLimitPerMinute());
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/api/v1/public/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        int limit = resolveLimit(request);
        String bucket = resolveBucket(request);
        if (!rateLimiter.tryConsume(bucket, limit)) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"rate_limit_exceeded\"}");
            return;
        }
        filterChain.doFilter(request, response);
    }

    private int resolveLimit(HttpServletRequest request) {
        if ("POST".equalsIgnoreCase(request.getMethod()) && isPublicSubmission(request.getRequestURI())) {
            return postLimitPerMinute;
        }
        return readLimitPerMinute;
    }

    private static boolean isPublicSubmission(String path) {
        return path.startsWith("/api/v1/public/solicitacoes")
                || path.matches("/api/v1/public/parceiros/[^/]+/solicitacoes");
    }

    private static String resolveBucket(HttpServletRequest request) {
        String ip = LoginRateLimitFilter.clientKey(request);
        String path = request.getRequestURI();
        if (path.startsWith("/api/v1/public/parceiros/") && path.endsWith("/solicitacoes")) {
            String slug = path.substring("/api/v1/public/parceiros/".length());
            slug = slug.substring(0, slug.indexOf('/'));
            return "public:post:partner:" + slug + ":" + ip;
        }
        if (path.startsWith("/api/v1/public/solicitacoes")) {
            return "public:post:viagem:" + ip;
        }
        return "public:read:" + ip;
    }
}
