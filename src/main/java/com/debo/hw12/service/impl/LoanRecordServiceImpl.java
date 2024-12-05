package com.debo.hw12.service.impl;

import com.debo.hw12.enums.ItemStatus;
import com.debo.hw12.enums.LoanStatus;
import com.debo.hw12.enums.PatronType;
import com.debo.hw12.enums.ReservationStatus;
import com.debo.hw12.exception.BusinessRuleException;
import com.debo.hw12.exception.EntityNotFoundException;
import com.debo.hw12.exception.ValidationException;
import com.debo.hw12.model.ItemCopy;
import com.debo.hw12.model.LoanRecord;
import com.debo.hw12.model.Patron;
import com.debo.hw12.model.Reservation;
import com.debo.hw12.model.accrual.*;
import com.debo.hw12.service.LoanRecordService;
import com.debo.hw12.service.PatronService;
import com.debo.hw12.state.LibraryState;
import com.debo.hw12.util.Logger;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class LoanRecordServiceImpl implements LoanRecordService {
    private static final long RESERVATION_EXPIRY_DAYS = 30;
    private final LibraryState libraryState = LibraryState.getInstance();
    private final Logger logger = Logger.getInstance();
    private final PatronService patronService;

    public LoanRecordServiceImpl(PatronService patronService) {
        this.patronService = patronService;
    }

    @Override
    public LoanRecord checkoutItem(UUID patronId, UUID itemCopyId, LocalDateTime dueDate) throws ValidationException, BusinessRuleException {
        Patron patron = patronService.getPatronById(patronId)
                .orElseThrow(() -> new ValidationException("Patron not found"));

        if (!patronService.isPatronEligibleForBorrowing(patronId)) {
            throw new ValidationException("Patron is not eligible for borrowing");
        }

        ItemCopy itemCopy = Optional.ofNullable(libraryState.getItemCopies().get(itemCopyId))
                .orElseThrow(() -> new ValidationException("Item copy not found"));

        if (!itemCopy.isAvailable()) {
            throw new BusinessRuleException("Item is not available for checkout");
        }

        LoanRecord loan = new LoanRecord(itemCopy, patron, dueDate);
        loan.setLateFeeAccrual(createLateFeeAccrual(patron.getType()));

        itemCopy.setStatus(ItemStatus.CHECKED_OUT);
        patron.getCurrentLoans().add(loan);
        libraryState.getLoans().put(loan.getId(), loan);

        logger.info(String.format("Item %s checked out to patron %s",
                itemCopyId, patron.getName()));

        return loan;
    }

    @Override
    public LoanRecord returnItem(UUID loanId) throws ValidationException, EntityNotFoundException {
        LoanRecord loan = getLoanById(loanId)
                .orElseThrow(() -> new EntityNotFoundException("Loan record not found"));

        if (loan.getStatus() != LoanStatus.ACTIVE) {
            throw new ValidationException("Loan is not active or overdue");
        }

        LocalDateTime now = LocalDateTime.now();
        loan.setReturnDate(now);
        loan.setStatus(LoanStatus.RETURNED);
        loan.setLateFees(loan.getLateFeeAccrual().calculateLateFee(loan));


        loan.getItemCopy().setStatus(ItemStatus.AVAILABLE);
        loan.getPatron().getCurrentLoans().remove(loan);
        loan.getPatron().getLoanHistory().add(loan);

        logger.info(String.format("Item returned for loan %s", loanId));

        return loan;
    }

    @Override
    public Reservation reserveItem(UUID patronId, UUID itemCopyId) throws BusinessRuleException, ValidationException {
        Patron patron = patronService.getPatronById(patronId)
                .orElseThrow(() -> new ValidationException("Patron not found"));

        ItemCopy itemCopy = Optional.ofNullable(libraryState.getItemCopies().get(itemCopyId))
                .orElseThrow(() -> new ValidationException("Item not found"));

        boolean hasExistingReservation = libraryState.getReservations().values().stream()
                .anyMatch(r -> r.getPatron().getId().equals(patronId) &&
                        r.getItem().getId().equals(itemCopy.getItem().getId()) &&
                        r.getStatus() == ReservationStatus.PENDING);

        if (hasExistingReservation) {
            throw new BusinessRuleException("Patron already has an active reservation for this item");
        }

        List<Reservation> activeReservations = libraryState.getReservations().values().stream()
                .filter(r -> r.getItem().getId().equals(itemCopy.getItem().getId()))
                .filter(r -> r.getStatus() == ReservationStatus.PENDING)
                .toList();

        int queuePosition = activeReservations.size() + 1;

        Reservation reservation = new Reservation(itemCopy.getItem(), patron);
        reservation.setExpiryDate(LocalDateTime.now().plusDays(RESERVATION_EXPIRY_DAYS));
        reservation.setQueuePosition(queuePosition);

        libraryState.getReservations().put(reservation.getId(), reservation);
        logger.info(String.format("Item %s reserved for patron %s", itemCopyId, patron.getName()));

        return reservation;
    }

    @Override
    public void cancelReservation(UUID reservationId) throws ValidationException {
        Reservation reservation = Optional.ofNullable(libraryState.getReservations().get(reservationId))
                .orElseThrow(() -> new ValidationException("Reservation not found"));

        if (reservation.getStatus() != ReservationStatus.PENDING) {
            throw new ValidationException("Reservation is not in PENDING status");
        }

        reservation.setStatus(ReservationStatus.CANCELLED);
        updateReservationQueue(reservation.getItem().getId());
        logger.info(String.format("Reservation %s cancelled", reservationId));
    }

    @Override
    public void processExpiredReservations() {
        LocalDateTime now = LocalDateTime.now();

        libraryState.getReservations().values().stream()
                .filter(r -> r.getStatus() == ReservationStatus.PENDING)
                .filter(r -> now.isAfter(r.getExpiryDate()))
                .forEach(reservation -> {
                    reservation.setStatus(ReservationStatus.EXPIRED);
                    updateReservationQueue(reservation.getItem().getId());
                    logger.info(String.format("Reservation %s expired", reservation.getId()));
                });
    }

    @Override
    public List<Reservation> getPatronReservations(UUID patronId) {
        return libraryState.getReservations().values().stream()
                .filter(r -> r.getPatron().getId().equals(patronId))
                .filter(r -> r.getStatus() == ReservationStatus.PENDING)
                .toList();
    }

    @Override
    public List<Reservation> getItemReservations(UUID itemId) {
        ItemCopy itemCopy = libraryState.getItemCopies().get(itemId);
        if (itemCopy == null) return List.of();

        return libraryState.getReservations().values().stream()
                .filter(r -> r.getItem().getId().equals(itemCopy.getItem().getId()))
                .filter(r -> r.getStatus() == ReservationStatus.PENDING)
                .sorted(Comparator.comparingInt(Reservation::getQueuePosition))
                .toList();
    }

    @Override
    public Optional<LoanRecord> getLoanById(UUID loanId) {
        return Optional.ofNullable(libraryState.getLoans().get(loanId));
    }

    @Override
    public List<LoanRecord> getActiveLoans() {
        return libraryState.getLoans().values().stream()
                .filter(loan -> loan.getStatus() == LoanStatus.ACTIVE)
                .toList();
    }

    @Override
    public List<LoanRecord> getOverdueLoans() {
        LocalDateTime now = LocalDateTime.now();
        return libraryState.getLoans().values().stream()
                .filter(loan -> loan.getStatus() == LoanStatus.ACTIVE)
                .filter(loan -> now.isAfter(loan.getDueDate()))
                .toList();
    }

    @Override
    public List<LoanRecord> getPatronActiveLoans(UUID patronId) {
        return libraryState.getLoans().values().stream()
                .filter(loan -> loan.getPatron().getId().equals(patronId))
                .filter(loan -> loan.getStatus() == LoanStatus.ACTIVE)
                .toList();
    }

    @Override
    public List<LoanRecord> getPatronLoanHistory(UUID patronId) {
        return libraryState.getLoans().values().stream()
                .filter(loan -> loan.getPatron().getId().equals(patronId))
                .toList();
    }

    @Override
    public double calculateLateFees(UUID loanId) throws EntityNotFoundException {
        return getLoanById(loanId)
                .map(loan -> loan.getLateFeeAccrual().calculateLateFee(loan))
                .orElseThrow(() -> new EntityNotFoundException("Loan record not found"));
    }

    @Override
    public void processOverdueLoans() {
        LocalDateTime now = LocalDateTime.now();

        getActiveLoans().stream()
                .filter(loan -> now.isAfter(loan.getDueDate()))
                .forEach(loan -> {
                    loan.setLateFees(loan.getLateFeeAccrual().calculateLateFee(loan));
                    logger.info(String.format("Late fees calculated for loan %s", loan.getId()));
                });
    }

    private void updateReservationQueue(UUID itemId) {
        List<Reservation> activeReservations = libraryState.getReservations().values().stream()
                .filter(r -> r.getItem().getId().equals(itemId))
                .filter(r -> r.getStatus() == ReservationStatus.PENDING)
                .toList();

        for (int i = 0; i < activeReservations.size(); i++) {
            activeReservations.get(i).setQueuePosition(i + 1);
        }
    }

    private LateFeeAccrual createLateFeeAccrual(PatronType patronType) {
        return switch (patronType) {
            case STUDENT -> new WeeklyLateFeeAccrualImpl();
            case FACULTY -> new BiWeeklyLateFeeAccrualImpl();
            case SENIOR -> new MonthlyLateFeeAccrualImpl();
            default -> new DailyLateFeeAccrualImpl();
        };
    }
}
