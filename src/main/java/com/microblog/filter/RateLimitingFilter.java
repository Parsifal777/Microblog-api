package com.microblog.filter;

import com.microblog.config.RateLimitingConfig;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class RateLimitingFilter extends OncePerRequestFilter {

    private final RateLimitingConfig rateLimitingConfig;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // Пропускаем запросы к auth
        if (request.getRequestURI().startsWith("/api/auth")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Получаем userId из запроса (из JWT)
        String userId = getUserIdFromRequest(request);
        if (userId == null) {
            filterChain.doFilter(request, response);
            return;
        }

        Bucket bucket = rateLimitingConfig.getBucket(userId);
        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.getWriter().write("Too many requests. Please try again later.");
            response.getWriter().flush();
        }
    }

    private String getUserIdFromRequest(HttpServletRequest request) {
        // Извлекаем userId из SecurityContext
        // Временно возвращаем "default"
        return "default";
    }
}
