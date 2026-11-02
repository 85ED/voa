package br.jlturismo.voa.modules.core.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "voa")
public record VoaProperties(
        Jwt jwt,
        Cors cors,
        Seed seed,
        Cookies cookies,
        Comissao comissao,
        Auth auth,
        PublicApi publicApi,
        Security security,
        String publicBaseUrl
) {
    public record Jwt(String accessSecret, int accessTtlMinutes, int refreshTtlDays) {}
    public record Cors(List<String> allowedOrigins) {}
    public record Seed(String adminEmail, String adminPassword) {}
    public record Cookies(boolean secure) {}
    public record Comissao(boolean autoEnabled) {}
    public record Auth(int loginRateLimitPerMinute) {}
    public record PublicApi(int rateLimitPerMinute, int postRateLimitPerMinute) {}
    public record Security(String contentSecurityPolicy) {}
}
