package com.vericerti.domain.common.vo;

import com.vericerti.domain.exception.InvalidAmountException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Money Value Object")
class MoneyTest {

    @Nested
    @DisplayName("Creation")
    class Creation {

        @Test
        @DisplayName("should create money with positive value")
        void shouldCreateMoneyWithPositiveValue() {
            // given
            BigDecimal amount = new BigDecimal("100.00");
            
            // when
            Money money = Money.of(amount);
            
            // then
            assertThat(money.getValue()).isEqualByComparingTo(amount);
        }

        @ParameterizedTest
        @DisplayName("should create money with various positive values")
        @ValueSource(strings = {"0.01", "1", "100", "999999.99", "0.001"})
        void shouldCreateMoneyWithVariousPositiveValues(String amountStr) {
            // given
            BigDecimal amount = new BigDecimal(amountStr);
            
            // when
            Money money = Money.of(amount);
            
            // then
            assertThat(money.getValue()).isEqualByComparingTo(amount);
        }
    }

    @Nested
    @DisplayName("Validation")
    class Validation {

        @Test
        @DisplayName("should throw exception for null amount")
        void shouldThrowExceptionForNullAmount() {
            // when & then
            assertThatThrownBy(() -> Money.of(null))
                    .isInstanceOf(InvalidAmountException.class);
        }

        @Test
        @DisplayName("should throw exception for zero amount")
        void shouldThrowExceptionForZeroAmount() {
            // when & then
            assertThatThrownBy(() -> Money.of(BigDecimal.ZERO))
                    .isInstanceOf(InvalidAmountException.class);
        }

        @ParameterizedTest
        @DisplayName("should throw exception for negative amounts")
        @ValueSource(strings = {"-0.01", "-1", "-100", "-999999.99"})
        void shouldThrowExceptionForNegativeAmounts(String amountStr) {
            // given
            BigDecimal amount = new BigDecimal(amountStr);
            
            // when & then
            assertThatThrownBy(() -> Money.of(amount))
                    .isInstanceOf(InvalidAmountException.class);
        }
    }

    @Nested
    @DisplayName("Operations")
    class Operations {

        @Test
        @DisplayName("should add two money values")
        void shouldAddTwoMoneyValues() {
            // given
            Money money1 = Money.of(new BigDecimal("100.00"));
            Money money2 = Money.of(new BigDecimal("50.50"));
            
            // when
            Money result = money1.add(money2);
            
            // then
            assertThat(result.getValue()).isEqualByComparingTo(new BigDecimal("150.50"));
        }

        @Test
        @DisplayName("should return true for positive amount")
        void shouldReturnTrueForPositiveAmount() {
            // given
            Money money = Money.of(new BigDecimal("100"));
            
            // then
            assertThat(money.isPositive()).isTrue();
        }
    }

    @Nested
    @DisplayName("Equality")
    class Equality {

        @Test
        @DisplayName("should be equal for same amount")
        void shouldBeEqualForSameAmount() {
            // given
            Money money1 = Money.of(new BigDecimal("100.00"));
            Money money2 = Money.of(new BigDecimal("100.00"));
            
            // then
            assertThat(money1).isEqualTo(money2);
        }
    }
}
