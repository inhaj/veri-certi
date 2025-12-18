package com.vericerti.infrastructure.filter;

import com.vericerti.infrastructure.config.FilterProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * HTTP 요청/응답 로깅 필터
 * - Request ID 생성 및 MDC 설정
 * - 요청 시작/종료 로깅
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class RequestLoggingFilter extends OncePerRequestFilter {

    private final FilterProperties filterProperties;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    private static final String REQUEST_ID_KEY = "requestId";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        if (!filterProperties.getLogging().isEnabled()) {
            filterChain.doFilter(request, response);
            return;
        }

        // 제외 경로 체크
        String uri = request.getRequestURI();
        if (isExcludedPath(uri)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Request ID 생성 및 MDC 설정
        String requestId = generateRequestId();
        MDC.put(REQUEST_ID_KEY, requestId);

        long startTime = System.currentTimeMillis();
        String method = request.getMethod();
        String clientIp = getClientIp(request);
        String userAgent = request.getHeader("User-Agent");

        log.info("[{}] {} {} from {} ({})", requestId, method, uri, clientIp, 
                 userAgent != null ? userAgent.substring(0, Math.min(50, userAgent.length())) : "N/A");

        try {
            filterChain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            int status = response.getStatus();
            
            log.info("[{}] {} {} completed with {} in {}ms", requestId, method, uri, status, duration);
            MDC.remove(REQUEST_ID_KEY);
        }
    }

    private String generateRequestId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private boolean isExcludedPath(String uri) {
        return filterProperties.getLogging().getExcludePaths().stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, uri));
    }
}
