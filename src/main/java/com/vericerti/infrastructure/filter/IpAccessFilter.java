package com.vericerti.infrastructure.filter;

import com.vericerti.infrastructure.config.FilterProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

/**
 * IP 접근 제어 필터
 * - Allowlist 모드: 허용 IP만 통과
 * - Blocklist 모드: 차단 IP만 거부
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)  // RequestLoggingFilter 다음
@RequiredArgsConstructor
public class IpAccessFilter extends OncePerRequestFilter {

    private final FilterProperties filterProperties;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        FilterProperties.IpFilter ipFilter = filterProperties.getIpFilter();

        if (!ipFilter.isEnabled()) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = getClientIp(request);
        boolean isMatch = isIpInList(clientIp, ipFilter.getAddresses());

        boolean allowed;
        if ("allowlist".equalsIgnoreCase(ipFilter.getMode())) {
            // Allowlist: 목록에 있으면 허용
            allowed = isMatch;
        } else {
            // Blocklist: 목록에 없으면 허용
            allowed = !isMatch;
        }

        if (!allowed) {
            log.warn("IP access denied: {} (mode: {})", clientIp, ipFilter.getMode());
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Access denied\", \"message\": \"Your IP is not allowed\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private boolean isIpInList(String clientIp, List<String> addresses) {
        for (String address : addresses) {
            if (address.contains("/")) {
                // CIDR 표기법 처리
                if (isIpInCidr(clientIp, address)) {
                    return true;
                }
            } else {
                // 단일 IP 비교
                if (address.equals(clientIp)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * CIDR 표기법으로 IP 범위 체크
     * 예: 192.168.1.0/24 → 192.168.1.0 ~ 192.168.1.255
     */
    private boolean isIpInCidr(String clientIp, String cidr) {
        try {
            String[] parts = cidr.split("/");
            String networkAddress = parts[0];
            int prefixLength = Integer.parseInt(parts[1]);

            InetAddress clientAddr = InetAddress.getByName(clientIp);
            InetAddress networkAddr = InetAddress.getByName(networkAddress);

            byte[] clientBytes = clientAddr.getAddress();
            byte[] networkBytes = networkAddr.getAddress();

            int mask = 0xFFFFFFFF << (32 - prefixLength);

            int clientInt = bytesToInt(clientBytes);
            int networkInt = bytesToInt(networkBytes);

            return (clientInt & mask) == (networkInt & mask);
        } catch (UnknownHostException | NumberFormatException e) {
            log.warn("Invalid CIDR notation: {}", cidr);
            return false;
        }
    }

    private int bytesToInt(byte[] bytes) {
        return ((bytes[0] & 0xFF) << 24) |
               ((bytes[1] & 0xFF) << 16) |
               ((bytes[2] & 0xFF) << 8) |
               (bytes[3] & 0xFF);
    }
}
