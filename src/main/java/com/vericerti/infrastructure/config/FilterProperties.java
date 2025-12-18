package com.vericerti.infrastructure.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 필터 관련 설정을 외부화하여 관리
 * application.yml의 app.* 설정을 매핑
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "app")
public class FilterProperties {

    private Logging logging = new Logging();
    private IpFilter ipFilter = new IpFilter();
    private SlowExecution slowExecution = new SlowExecution();

    @Getter
    @Setter
    public static class Logging {
        private boolean enabled = true;
        private boolean includeHeaders = false;
        private List<String> excludePaths = new ArrayList<>();
    }

    @Getter
    @Setter
    public static class IpFilter {
        private boolean enabled = false;
        private String mode = "allowlist";  // allowlist 또는 blocklist
        private String addressesString = "127.0.0.1";  // 환경변수용 (쉼표 구분)

        /**
         * 환경변수(쉼표 구분 문자열)를 List로 변환하여 반환
         */
        public List<String> getAddresses() {
            if (addressesString == null || addressesString.isBlank()) {
                return new ArrayList<>();
            }
            return Arrays.stream(addressesString.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();
        }
    }

    @Getter
    @Setter
    public static class SlowExecution {
        private boolean enabled = true;
        private long defaultThresholdMs = 1000L;
    }
}
