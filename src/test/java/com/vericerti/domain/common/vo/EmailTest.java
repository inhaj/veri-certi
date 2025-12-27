package com.vericerti.domain.common.vo;

import com.vericerti.domain.exception.InvalidEmailException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Email Value Object")
class EmailTest {

    @Nested
    @DisplayName("Creation")
    class Creation {

        @Test
        @DisplayName("should create email with valid format")
        void shouldCreateEmailWithValidFormat() {
            // given
            String validEmail = "test@example.com";
            
            // when
            Email email = Email.of(validEmail);
            
            // then
            assertThat(email.getValue()).isEqualTo(validEmail);
        }

        @ParameterizedTest
        @DisplayName("should create email with various valid formats")
        @ValueSource(strings = {
            "user@domain.com",
            "user.name@domain.com",
            "user+tag@domain.co.kr",
            "user_name@sub.domain.org",
            "USER@DOMAIN.COM"
        })
        void shouldCreateEmailWithVariousValidFormats(String validEmail) {
            // when
            Email email = Email.of(validEmail);
            
            // then
            assertThat(email.getValue()).isEqualTo(validEmail);
        }
    }

    @Nested
    @DisplayName("Validation")
    class Validation {

        @ParameterizedTest
        @DisplayName("should throw exception for null or empty email")
        @NullAndEmptySource
        void shouldThrowExceptionForNullOrEmptyEmail(String invalidEmail) {
            // when & then
            assertThatThrownBy(() -> Email.of(invalidEmail))
                    .isInstanceOf(InvalidEmailException.class)
                    .hasMessageContaining("empty");
        }

        @ParameterizedTest
        @DisplayName("should throw exception for invalid email format")
        @ValueSource(strings = {
            "invalid",
            "no-at-sign",
            "@no-local-part.com",
            "spaces in@email.com",
            "no-domain@"
        })
        void shouldThrowExceptionForInvalidFormat(String invalidEmail) {
            // when & then
            assertThatThrownBy(() -> Email.of(invalidEmail))
                    .isInstanceOf(InvalidEmailException.class);
        }
    }

    @Nested
    @DisplayName("Equality")
    class Equality {

        @Test
        @DisplayName("should be equal for same email value")
        void shouldBeEqualForSameValue() {
            // given
            Email email1 = Email.of("test@example.com");
            Email email2 = Email.of("test@example.com");
            
            // then
            assertThat(email1).isEqualTo(email2);
            assertThat(email1.hashCode()).isEqualTo(email2.hashCode());
        }

        @Test
        @DisplayName("should not be equal for different email values")
        void shouldNotBeEqualForDifferentValues() {
            // given
            Email email1 = Email.of("test1@example.com");
            Email email2 = Email.of("test2@example.com");
            
            // then
            assertThat(email1).isNotEqualTo(email2);
        }
    }
}
