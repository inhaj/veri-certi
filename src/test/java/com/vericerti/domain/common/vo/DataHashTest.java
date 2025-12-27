package com.vericerti.domain.common.vo;

import com.vericerti.domain.exception.InvalidHashException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

@DisplayName("DataHash Value Object")
class DataHashTest {

    // Valid SHA-256 hash (64 characters)
    private static final String VALID_HASH = "a".repeat(64);

    @Nested
    @DisplayName("Creation")
    class Creation {

        @Test
        @DisplayName("should create hash with valid 64-character string")
        void shouldCreateHashWithValidFormat() {
            // when
            DataHash hash = DataHash.of(VALID_HASH);
            
            // then
            assertThat(hash.getValue()).isEqualTo(VALID_HASH);
        }

        @Test
        @DisplayName("should create hash with actual SHA-256 format")
        void shouldCreateHashWithActualSha256() {
            // given
            String sha256 = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";
            
            // when
            DataHash hash = DataHash.of(sha256);
            
            // then
            assertThat(hash.getValue()).isEqualTo(sha256);
        }
    }

    @Nested
    @DisplayName("Validation")
    class Validation {

        @ParameterizedTest
        @DisplayName("should throw exception for null or empty hash")
        @NullAndEmptySource
        void shouldThrowExceptionForNullOrEmptyHash(String invalidHash) {
            // when & then
            assertThatThrownBy(() -> DataHash.of(invalidHash))
                    .isInstanceOf(InvalidHashException.class)
                    .hasMessageContaining("empty");
        }

        @ParameterizedTest
        @DisplayName("should throw exception for invalid hash length")
        @ValueSource(strings = {
            "abc",                                                              // Too short
            "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",   // 63 chars
            "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", // 65 chars
            "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" // 128 chars
        })
        void shouldThrowExceptionForInvalidLength(String invalidHash) {
            // when & then
            assertThatThrownBy(() -> DataHash.of(invalidHash))
                    .isInstanceOf(InvalidHashException.class)
                    .hasMessageContaining("64");
        }
    }

    @Nested
    @DisplayName("Equality")
    class Equality {

        @Test
        @DisplayName("should be equal for same hash value")
        void shouldBeEqualForSameValue() {
            // given
            DataHash hash1 = DataHash.of(VALID_HASH);
            DataHash hash2 = DataHash.of(VALID_HASH);
            
            // then
            assertThat(hash1).isEqualTo(hash2);
            assertThat(hash1.hashCode()).isEqualTo(hash2.hashCode());
        }

        @Test
        @DisplayName("should not be equal for different hash values")
        void shouldNotBeEqualForDifferentValues() {
            // given
            DataHash hash1 = DataHash.of("a".repeat(64));
            DataHash hash2 = DataHash.of("b".repeat(64));
            
            // then
            assertThat(hash1).isNotEqualTo(hash2);
        }
    }
}
