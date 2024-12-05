package com.debo.hw12.service;

import com.debo.hw12.enums.ItemStatus;
import com.debo.hw12.enums.LoanStatus;
import com.debo.hw12.enums.PatronType;
import com.debo.hw12.enums.ReservationStatus;
import com.debo.hw12.exception.BusinessRuleException;
import com.debo.hw12.exception.EntityNotFoundException;
import com.debo.hw12.exception.ValidationException;
import com.debo.hw12.model.*;
import com.debo.hw12.model.accrual.BiWeeklyLateFeeAccrualImpl;
import com.debo.hw12.model.accrual.DailyLateFeeAccrualImpl;
import com.debo.hw12.model.accrual.MonthlyLateFeeAccrualImpl;
import com.debo.hw12.model.accrual.WeeklyLateFeeAccrualImpl;
import com.debo.hw12.service.impl.LoanRecordServiceImpl;
import com.debo.hw12.state.LibraryState;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

@DisplayName("LoanRecord Service Implementation Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ExtendWith(MockitoExtension.class)
class LoanRecordTest {

    @Mock
    private PatronService patronService;
    private LibraryState libraryState;
    private LoanRecordServiceImpl loanRecordService;

    @BeforeEach
    void setUp() {
        libraryState = LibraryState.getInstance();
        libraryState.getLoans().clear();
        libraryState.getReservations().clear();
        libraryState.getItemCopies().clear();
        loanRecordService = new LoanRecordServiceImpl(patronService);
    }

    @Nested
    @DisplayName("Checkout Tests")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class CheckoutTests {
        private Book book;
        private ItemCopy itemCopy;
        private Patron patron;
        private LocalDateTime dueDate;

        @BeforeEach
        void setUp() {
            book = Book.builder()
                    .title("Test Book")
                    .isbn("123456789")
                    .author("Test Author")
                    .copies(new ArrayList<>())
                    .build();

            itemCopy = new ItemCopy(book, "BC001", "SHELF-A1");
            book.addCopy(itemCopy);
            libraryState.getItemCopies().put(itemCopy.getId(), itemCopy);

            patron = Patron.builder()
                    .id(UUID.randomUUID())
                    .name("John Doe")
                    .email("john@example.com")
                    .type(PatronType.STUDENT)
                    .registrationDate(LocalDateTime.now())
                    .active(true)
                    .currentLoans(new HashSet<>())
                    .loanHistory(new ArrayList<>())
                    .build();

            dueDate = LocalDateTime.now().plusDays(14);
        }

        @Test
        @Order(1)
        @DisplayName("Should successfully checkout available item")
        void shouldCheckoutAvailableItem() throws ValidationException, BusinessRuleException {
            when(patronService.getPatronById(patron.getId())).thenReturn(Optional.of(patron));
            when(patronService.isPatronEligibleForBorrowing(patron.getId())).thenReturn(true);

            LoanRecord result = loanRecordService.checkoutItem(patron.getId(), itemCopy.getId(), dueDate);

            assertThat(result.getItemCopy()).isEqualTo(itemCopy);
            assertThat(result.getPatron()).isEqualTo(patron);
            assertThat(result.getStatus()).isEqualTo(LoanStatus.ACTIVE);
            assertThat(result.getDueDate()).isEqualTo(dueDate);
            assertThat(result.getLateFeeAccrual())
                    .isInstanceOf(WeeklyLateFeeAccrualImpl.class);
            assertThat(itemCopy.getStatus()).isEqualTo(ItemStatus.CHECKED_OUT);
            assertThat(patron.getCurrentLoans()).contains(result);
        }

        @ParameterizedTest
        @Order(2)
        @EnumSource(PatronType.class)
        @DisplayName("Should assign correct late fee accrual based on patron type")
        void shouldAssignCorrectLateFeeAccrual(PatronType patronType) throws ValidationException, BusinessRuleException {
            patron.setType(patronType);
            when(patronService.getPatronById(patron.getId())).thenReturn(Optional.of(patron));
            when(patronService.isPatronEligibleForBorrowing(patron.getId())).thenReturn(true);

            LoanRecord result = loanRecordService.checkoutItem(patron.getId(), itemCopy.getId(), dueDate);

            Class<?> expectedAccrualClass = switch (patronType) {
                case STUDENT -> WeeklyLateFeeAccrualImpl.class;
                case FACULTY -> BiWeeklyLateFeeAccrualImpl.class;
                case SENIOR -> MonthlyLateFeeAccrualImpl.class;
                default -> DailyLateFeeAccrualImpl.class;
            };
            assertThat(result.getLateFeeAccrual()).isInstanceOf(expectedAccrualClass);
        }

        @Test
        @Order(3)
        @DisplayName("Should prevent checkout when patron has reached loan limit")
        void shouldPreventCheckoutAtLoanLimit() {
            when(patronService.getPatronById(patron.getId())).thenReturn(Optional.of(patron));
            when(patronService.isPatronEligibleForBorrowing(patron.getId())).thenReturn(false);

            assertThatThrownBy(() ->
                    loanRecordService.checkoutItem(patron.getId(), itemCopy.getId(), dueDate))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("not eligible for borrowing");
        }
    }

    @Nested
    @DisplayName("Return Tests")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class ReturnTests {
        private Book book;
        private ItemCopy itemCopy;
        private Patron patron;
        private LoanRecord loanRecord;
        private LocalDateTime dueDate;

        @BeforeEach
        void setUp() {
            book = Book.builder()
                    .title("Test Book")
                    .isbn("123456789")
                    .author("Test Author")
                    .copies(new ArrayList<>())
                    .build();

            itemCopy = new ItemCopy(book, "BC001", "SHELF-A1");
            book.addCopy(itemCopy);
            libraryState.getItemCopies().put(itemCopy.getId(), itemCopy);

            patron = Patron.builder()
                    .id(UUID.randomUUID())
                    .name("John Doe")
                    .email("john@example.com")
                    .type(PatronType.STUDENT)
                    .registrationDate(LocalDateTime.now())
                    .active(true)
                    .currentLoans(new HashSet<>())
                    .loanHistory(new ArrayList<>())
                    .build();

            dueDate = LocalDateTime.now().minusDays(14);
            loanRecord = new LoanRecord(itemCopy, patron, dueDate);
            patron.getCurrentLoans().add(loanRecord);

            libraryState.getLoans().put(loanRecord.getId(), loanRecord);
            libraryState.getItemCopies().put(itemCopy.getId(), itemCopy);
        }

        @Test
        @Order(1)
        @DisplayName("Should successfully return item on time")
        void shouldReturnItemOnTime() throws ValidationException, EntityNotFoundException {
            loanRecord.setDueDate(LocalDateTime.now().plusDays(14));
            LoanRecord result = loanRecordService.returnItem(loanRecord.getId());

            assertThat(result.getStatus()).isEqualTo(LoanStatus.RETURNED);
            assertThat(result.getReturnDate()).isNotNull();
            assertThat(result.getLateFees()).isZero();
            assertThat(result.getItemCopy().getStatus()).isEqualTo(ItemStatus.AVAILABLE);
            assertThat(patron.getCurrentLoans()).doesNotContain(result);
            assertThat(patron.getLoanHistory()).contains(result);
        }

        @Test
        @Order(2)
        @DisplayName("Should calculate late fees for overdue return")
        void shouldCalculateLateFees() throws ValidationException, EntityNotFoundException {
            LoanRecord result = loanRecordService.returnItem(loanRecord.getId());

            assertThat(result.getLateFees()).isGreaterThan(0.0);
            assertThat(result.getStatus()).isEqualTo(LoanStatus.RETURNED);
        }
    }

    @Nested
    @DisplayName("Reservation Tests")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class ReservationTests {
        private Book book;
        private ItemCopy itemCopy;
        private Patron patron;

        @BeforeEach
        void setUp() {
            book = Book.builder()
                    .id(UUID.randomUUID())
                    .title("Test Book")
                    .isbn("123456789")
                    .author("Test Author")
                    .copies(new ArrayList<>())
                    .build();

            itemCopy = new ItemCopy(book, "BC001", "SHELF-A1");
            book.addCopy(itemCopy);
            libraryState.getItemCopies().put(itemCopy.getId(), itemCopy);

            patron = new Patron("John Doe", "john@example.com", PatronType.STUDENT);
        }

        @Test
        @Order(1)
        @DisplayName("Should create reservation with correct defaults")
        void shouldCreateReservationWithDefaults() throws BusinessRuleException, ValidationException {
            when(patronService.getPatronById(patron.getId())).thenReturn(Optional.of(patron));

            Reservation result = loanRecordService.reserveItem(patron.getId(), itemCopy.getId());

            assertThat(result.getItem()).isEqualTo(book);
            assertThat(result.getPatron()).isEqualTo(patron);
            assertThat(result.getStatus()).isEqualTo(ReservationStatus.PENDING);
            assertThat(result.getQueuePosition()).isEqualTo(1);
            assertThat(result.getReservationDate()).isNotNull();
            assertThat(result.getExpiryDate()).isBeforeOrEqualTo(LocalDateTime.now().plusDays(30));
        }

        @Test
        @Order(2)
        @DisplayName("Should maintain correct queue positions for multiple reservations")
        void shouldMaintainQueuePositions() throws BusinessRuleException, ValidationException {
            Patron patron2 = new Patron("Jane Doe", "jane@example.com", PatronType.STUDENT);
            when(patronService.getPatronById(any(UUID.class)))
                    .thenAnswer(invocation -> {
                        UUID id = invocation.getArgument(0);
                        return Optional.of(id.equals(patron.getId()) ? patron : patron2);
                    });

            Reservation result1 = loanRecordService.reserveItem(patron.getId(), itemCopy.getId());
            Reservation result2 = loanRecordService.reserveItem(patron2.getId(), itemCopy.getId());

            assertThat(result1.getQueuePosition()).isEqualTo(1);
            assertThat(result2.getQueuePosition()).isEqualTo(2);
        }

        @Test
        @Order(3)
        @DisplayName("Should update queue positions after cancellation")
        void shouldUpdateQueueAfterCancellation() throws BusinessRuleException, ValidationException {
            Patron patron2 = new Patron("Jane Doe", "jane@example.com", PatronType.STUDENT);
            when(patronService.getPatronById(any(UUID.class)))
                    .thenAnswer(invocation -> {
                        UUID id = invocation.getArgument(0);
                        return Optional.of(id.equals(patron.getId()) ? patron : patron2);
                    });

            Reservation res1 = loanRecordService.reserveItem(patron.getId(), itemCopy.getId());
            Reservation res2 = loanRecordService.reserveItem(patron2.getId(), itemCopy.getId());

            loanRecordService.cancelReservation(res1.getId());

            List<Reservation> activeReservations = loanRecordService.getItemReservations(itemCopy.getId());
            assertThat(activeReservations).hasSize(1);
            assertThat(activeReservations.getFirst().getQueuePosition()).isEqualTo(1);
        }
    }
}