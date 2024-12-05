package com.debo.hw12.service;

import com.debo.hw12.enums.PatronType;
import com.debo.hw12.exception.BusinessRuleException;
import com.debo.hw12.exception.DuplicateEntityException;
import com.debo.hw12.exception.ValidationException;
import com.debo.hw12.model.LoanRecord;
import com.debo.hw12.model.Patron;
import com.debo.hw12.service.impl.PatronServiceImpl;
import com.debo.hw12.state.LibraryState;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("PatronService Implementation Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PatronTest {

    private PatronServiceImpl patronService;
    private LibraryState libraryState;

    @BeforeEach
    void setUp() {
        patronService = new PatronServiceImpl();
        libraryState = LibraryState.getInstance();
        libraryState.getPatrons().clear();
    }

    @Nested
    @DisplayName("Patron Registration Tests")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class PatronRegistrationTests {

        @Test
        @Order(1)
        @DisplayName("Should successfully register a patron")
        void shouldRegisterPatron() throws ValidationException, DuplicateEntityException {
            Patron patron = patronService.registerPatron(
                    "John Doe",
                    "john.doe@example.com",
                    "STANDARD"
            );

            assertThat(patron).isNotNull();
            assertThat(patron.getName()).isEqualTo("John Doe");
            assertThat(patron.getEmail()).isEqualTo("john.doe@example.com");
            assertThat(patron.getType()).isEqualTo(PatronType.STANDARD);
            assertThat(patron.isActive()).isTrue();
            assertThat(patron.getCurrentLoans()).isEmpty();
            assertThat(patron.getLoanHistory()).isEmpty();
        }

        @ParameterizedTest
        @Order(2)
        @DisplayName("Should validate all patron types")
        @EnumSource(PatronType.class)
        void shouldAcceptAllValidPatronTypes(PatronType type) throws ValidationException, DuplicateEntityException {
            Patron patron = patronService.registerPatron(
                    "Test Patron",
                    "test" + type + "@example.com",
                    type.name()
            );

            assertThat(patron.getType()).isEqualTo(type);
            assertThat(patron.getType().getMaxItemLoanAmount())
                    .isEqualTo(type.getMaxItemLoanAmount());
        }

        @Test
        @Order(3)
        @DisplayName("Should prevent duplicate email registration")
        void shouldPreventDuplicateEmail() throws ValidationException, DuplicateEntityException {
            String email = "duplicate@example.com";
            patronService.registerPatron("First Patron", email, "STANDARD");

            assertThatThrownBy(() ->
                    patronService.registerPatron("Second Patron", email, "FACULTY"))
                    .isInstanceOf(DuplicateEntityException.class)
                    .hasMessageContaining("already exists");
        }

        @Test
        @Order(4)
        @DisplayName("Should reject empty patron name")
        void shouldRejectEmptyName() {
            assertThatThrownBy(() ->
                    patronService.registerPatron("", "valid@email.com", "STANDARD"))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("Name cannot be empty");
        }

        @Test
        @Order(5)
        @DisplayName("Should reject empty patron email")
        void shouldRejectEmptyEmail() {
            assertThatThrownBy(() ->
                    patronService.registerPatron("John Doe", "", "STANDARD"))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("Email cannot be empty");
        }

        @Test
        @Order(6)
        @DisplayName("Should reject invalid email format")
        void shouldRejectInvalidEmailFormat() {
            assertThatThrownBy(() ->
                    patronService.registerPatron("John Doe", "invalid-email", "STANDARD"))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("Invalid email format");
        }

        @Test
        @Order(7)
        @DisplayName("Should reject invalid patron type")
        void shouldRejectInvalidPatronType() {
            assertThatThrownBy(() ->
                    patronService.registerPatron("John Doe", "valid@email.com", "INVALID"))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("Invalid patron type");
        }
    }

    @Nested
    @DisplayName("Patron Search and Retrieval Tests")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class PatronSearchTests {

        @BeforeEach
        void setUpTestData() throws ValidationException, DuplicateEntityException {
            patronService.registerPatron("John Smith", "john@example.com", "STUDENT");
            patronService.registerPatron("Jane Doe", "jane@example.com", "FACULTY");
            patronService.registerPatron("Bob Wilson", "bob@example.com", "SENIOR");
        }

        @Test
        @Order(1)
        @DisplayName("Should find patron by ID")
        void shouldFindPatronById() throws ValidationException, DuplicateEntityException {
            Patron patron = patronService.registerPatron(
                    "Test Patron",
                    "test@example.com",
                    "STANDARD"
            );

            Optional<Patron> found = patronService.getPatronById(patron.getId());

            assertThat(found).isPresent();
            assertThat(found.get().getEmail()).isEqualTo("test@example.com");
        }

        @Test
        @Order(2)
        @DisplayName("Should search patrons by various criteria")
        void shouldSearchPatrons() {
            List<Patron> nameResults = patronService.searchPatrons("name", "John");
            assertThat(nameResults).hasSize(1);
            assertThat(nameResults.get(0).getName()).contains("John");

            List<Patron> emailResults = patronService.searchPatrons("email", "jane");
            assertThat(emailResults).hasSize(1);
            assertThat(emailResults.get(0).getEmail()).contains("jane");

            List<Patron> typeResults = patronService.searchPatrons("type", "faculty");
            assertThat(typeResults).hasSize(1);
            assertThat(typeResults.get(0).getType()).isEqualTo(PatronType.FACULTY);
        }
    }

    @Nested
    @DisplayName("Patron Update and Deactivation Tests")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class PatronUpdateAndDeactivationTests {

        private UUID patronId;

        @BeforeEach
        void setUpTestPatron() throws ValidationException, DuplicateEntityException {
            Patron patron = patronService.registerPatron(
                    "Original Name",
                    "original@example.com",
                    "STANDARD"
            );
            patronId = patron.getId();
        }

        @Test
        @Order(1)
        @DisplayName("Should update patron details")
        void shouldUpdatePatron() throws ValidationException, DuplicateEntityException {
            Patron updated = patronService.updatePatron(
                    patronId,
                    "New Name",
                    "new@example.com"
            );

            assertThat(updated.getName()).isEqualTo("New Name");
            assertThat(updated.getEmail()).isEqualTo("new@example.com");
        }

        @Test
        @Order(2)
        @DisplayName("Should deactivate patron with no loans")
        void shouldDeactivatePatron() throws ValidationException, BusinessRuleException {
            patronService.deactivatePatron(patronId);

            assertThat(patronService.getPatronById(patronId)).isEmpty();
        }

        @Test
        @Order(4)
        @DisplayName("Should prevent deactivation of patron with active loans")
        void shouldPreventDeactivationWithActiveLoans() {
            Patron patron = patronService.getPatronById(patronId)
                    .orElseThrow(() -> new RuntimeException("Test setup failed: Patron not found"));

            LoanRecord activeLoan = LoanRecord.builder()
                    .id(UUID.randomUUID())
                    .patron(patron)
                    .checkoutDate(LocalDateTime.now())
                    .dueDate(LocalDateTime.now().plusDays(14))
                    .build();

            patron.getCurrentLoans().add(activeLoan);

            assertThatThrownBy(() ->
                    patronService.deactivatePatron(patronId))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("Cannot deactivate patron with active loans");

            Optional<Patron> checkedPatron = patronService.getPatronById(patronId);
            assertThat(checkedPatron)
                    .isPresent()
                    .get()
                    .matches(Patron::isActive);
        }

        @Test
        @Order(4)
        @DisplayName("Should check borrowing eligibility")
        void shouldCheckBorrowingEligibility() throws ValidationException, DuplicateEntityException, BusinessRuleException {
            Patron patron = patronService.registerPatron(
                    "Test Patron",
                    "test@example.com",
                    "STANDARD"
            );

            assertThat(patronService.isPatronEligibleForBorrowing(patron.getId()))
                    .isTrue();

            patronService.deactivatePatron(patron.getId());

            assertThat(patronService.isPatronEligibleForBorrowing(patron.getId()))
                    .isFalse();
        }
    }
}