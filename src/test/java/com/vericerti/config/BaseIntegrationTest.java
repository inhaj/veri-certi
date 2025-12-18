package com.vericerti.config;

import com.redis.testcontainers.RedisContainer;
import com.vericerti.domain.donation.repository.DonationRepository;
import com.vericerti.domain.ledger.repository.LedgerEntryRepository;
import com.vericerti.domain.member.repository.MemberRepository;
import com.vericerti.domain.organization.repository.OrganizationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.Objects;

/**
 * 모든 Integration Test의 베이스 클래스.
 * MySQL과 Redis 컨테이너를 싱글톤으로 공유합니다.
 * 각 테스트 전 DB를 정리하여 테스트 격리를 보장합니다.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public abstract class BaseIntegrationTest {

    static MySQLContainer<?> mysql;
    static RedisContainer redis;

    @Autowired
    protected LedgerEntryRepository ledgerEntryRepository;

    @Autowired
    protected DonationRepository donationRepository;

    @Autowired
    protected MemberRepository memberRepository;

    @Autowired
    protected OrganizationRepository organizationRepository;

    @Autowired
    protected StringRedisTemplate redisTemplate;

    static {
        mysql = new MySQLContainer<>(DockerImageName.parse("mysql:8.0"))
                .withDatabaseName("vericerti_test")
                .withUsername("test")
                .withPassword("test");
        mysql.start();

        redis = new RedisContainer(DockerImageName.parse("redis:7-alpine"));
        redis.start();
    }

    /**
     * 각 테스트 전 DB 정리 (외래 키 순서 고려)
     */
    @BeforeEach
    void cleanUpDatabase() {
        // 자식 테이블부터 삭제 (외래 키 제약 조건)
        ledgerEntryRepository.deleteAll();
        donationRepository.deleteAll();
        memberRepository.deleteAll();
        organizationRepository.deleteAll();

        // Redis 정리
        Objects.requireNonNull(redisTemplate.getConnectionFactory())
                .getConnection()
                .serverCommands()
                .flushDb();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // MySQL
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);

        // Redis
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", redis::getFirstMappedPort);
    }
}
