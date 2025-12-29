package com.vericerti.domain.organization.entity;

import com.vericerti.domain.common.vo.BusinessNumber;
import com.vericerti.domain.exception.OrganizationOperationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Organization Entity")
class OrganizationTest {

    private Organization pendingOrganization;
    private Organization activeOrganization;

    @BeforeEach
    void setUp() {
        pendingOrganization = Organization.builder()
                .id(1L)
                .name("Test Org")
                .businessNumber(BusinessNumber.of("123-45-67890"))
                .description("Test Description")
                .status(OrganizationStatus.PENDING)
                .build();

        activeOrganization = Organization.builder()
                .id(2L)
                .name("Active Org")
                .businessNumber(BusinessNumber.of("987-65-43210"))
                .description("Active Description")
                .status(OrganizationStatus.ACTIVE)
                .build();
    }

    @Nested
    @DisplayName("Approve Operation")
    class ApproveOperation {

        @Test
        @DisplayName("should approve pending organization")
        void shouldApprovePendingOrganization() {
            // when
            pendingOrganization.approve();

            // then
            assertThat(pendingOrganization.getStatus()).isEqualTo(OrganizationStatus.ACTIVE);
            assertThat(pendingOrganization.getApprovedAt()).isNotNull();
        }

        @Test
        @DisplayName("should throw exception when approving already active organization")
        void shouldThrowExceptionWhenApprovingActive() {
            // when & then
            assertThatThrownBy(() -> activeOrganization.approve())
                    .isInstanceOf(OrganizationOperationException.class)
                    .hasMessageContaining("pending");
        }
    }

    @Nested
    @DisplayName("Suspend Operation")
    class SuspendOperation {

        @Test
        @DisplayName("should suspend active organization")
        void shouldSuspendActiveOrganization() {
            // when
            activeOrganization.suspend("Policy violation");

            // then
            assertThat(activeOrganization.getStatus()).isEqualTo(OrganizationStatus.SUSPENDED);
            assertThat(activeOrganization.getSuspendedAt()).isNotNull();
            assertThat(activeOrganization.getSuspensionReason()).isEqualTo("Policy violation");
        }

        @Test
        @DisplayName("should throw exception when suspending already suspended")
        void shouldThrowExceptionWhenSuspendingAlreadySuspended() {
            // given
            activeOrganization.suspend("First suspension");

            // when & then
            assertThatThrownBy(() -> activeOrganization.suspend("Second suspension"))
                    .isInstanceOf(OrganizationOperationException.class)
                    .hasMessageContaining("already suspended");
        }
    }

    @Nested
    @DisplayName("Reactivate Operation")
    class ReactivateOperation {

        @Test
        @DisplayName("should reactivate suspended organization")
        void shouldReactivateSuspendedOrganization() {
            // given
            activeOrganization.suspend("Temporary");

            // when
            activeOrganization.reactivate();

            // then
            assertThat(activeOrganization.getStatus()).isEqualTo(OrganizationStatus.ACTIVE);
            assertThat(activeOrganization.getSuspendedAt()).isNull();
            assertThat(activeOrganization.getSuspensionReason()).isNull();
        }

        @Test
        @DisplayName("should throw exception when reactivating already active")
        void shouldThrowExceptionWhenReactivatingActive() {
            // when & then
            assertThatThrownBy(() -> activeOrganization.reactivate())
                    .isInstanceOf(OrganizationOperationException.class)
                    .hasMessageContaining("already active");
        }
    }

    @Nested
    @DisplayName("Deactivate Operation")
    class DeactivateOperation {

        @Test
        @DisplayName("should deactivate organization")
        void shouldDeactivateOrganization() {
            // when
            activeOrganization.deactivate();

            // then
            assertThat(activeOrganization.getStatus()).isEqualTo(OrganizationStatus.DEACTIVATED);
            assertThat(activeOrganization.getDeactivatedAt()).isNotNull();
        }

        @Test
        @DisplayName("should throw exception when deactivating already deactivated")
        void shouldThrowExceptionWhenDeactivatingAlreadyDeactivated() {
            // given
            activeOrganization.deactivate();

            // when & then
            assertThatThrownBy(() -> activeOrganization.deactivate())
                    .isInstanceOf(OrganizationOperationException.class)
                    .hasMessageContaining("already deactivated");
        }
    }

    @Nested
    @DisplayName("Update Operation")
    class UpdateOperation {

        @Test
        @DisplayName("should update active organization")
        void shouldUpdateActiveOrganization() {
            // when
            activeOrganization.update("New Name", "New Description");

            // then
            assertThat(activeOrganization.getName()).isEqualTo("New Name");
            assertThat(activeOrganization.getDescription()).isEqualTo("New Description");
        }

        @Test
        @DisplayName("should throw exception when updating deactivated organization")
        void shouldThrowExceptionWhenUpdatingDeactivated() {
            // given
            activeOrganization.deactivate();

            // when & then
            assertThatThrownBy(() -> activeOrganization.update("New", "Desc"))
                    .isInstanceOf(OrganizationOperationException.class)
                    .hasMessageContaining("deactivated");
        }
    }

    @Nested
    @DisplayName("Status Checks")
    class StatusChecks {

        @Test
        @DisplayName("pending organization should return isPending true")
        void pendingOrgShouldReturnIsPendingTrue() {
            assertThat(pendingOrganization.isPending()).isTrue();
            assertThat(pendingOrganization.isActive()).isFalse();
        }

        @Test
        @DisplayName("active organization should return isActive true")
        void activeOrgShouldReturnIsActiveTrue() {
            assertThat(activeOrganization.isActive()).isTrue();
            assertThat(activeOrganization.isPending()).isFalse();
        }
    }
}
