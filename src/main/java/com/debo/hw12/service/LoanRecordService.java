package com.debo.hw12.service;

import com.debo.hw12.exception.BusinessRuleException;
import com.debo.hw12.exception.EntityNotFoundException;
import com.debo.hw12.exception.ValidationException;
import com.debo.hw12.model.LoanRecord;
import com.debo.hw12.model.Reservation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LoanRecordService {
    LoanRecord checkoutItem(UUID patronId, UUID itemCopyId, LocalDateTime dueDate) throws ValidationException, BusinessRuleException;

    LoanRecord returnItem(UUID loanId) throws ValidationException, EntityNotFoundException;

    Reservation reserveItem(UUID patronId, UUID itemId) throws ValidationException, BusinessRuleException;

    void cancelReservation(UUID reservationId) throws ValidationException;

    void processExpiredReservations();

    List<Reservation> getPatronReservations(UUID patronId);

    List<Reservation> getItemReservations(UUID itemId);

    Optional<LoanRecord> getLoanById(UUID loanId);

    List<LoanRecord> getActiveLoans();

    List<LoanRecord> getOverdueLoans();

    List<LoanRecord> getPatronActiveLoans(UUID patronId);

    List<LoanRecord> getPatronLoanHistory(UUID patronId);

    double calculateLateFees(UUID loanId) throws EntityNotFoundException;

    void processOverdueLoans();
}
