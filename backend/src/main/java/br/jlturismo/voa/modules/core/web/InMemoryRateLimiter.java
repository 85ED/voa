package br.jlturismo.voa.modules.core.web;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class InMemoryRateLimiter {

    private final Map<String, Window> windows = new ConcurrentHashMap<>();

    public boolean tryConsume(String bucketKey, int limitPerMinute) {
        int limit = Math.max(1, limitPerMinute);
        long minute = Instant.now().getEpochSecond() / 60;
        String key = bucketKey + ":" + minute;
        Window window = windows.compute(key, (k, existing) -> {
            if (existing == null || existing.minute != minute) {
                return new Window(minute, new AtomicInteger(0));
            }
            return existing;
        });
        return window.count.incrementAndGet() <= limit;
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
