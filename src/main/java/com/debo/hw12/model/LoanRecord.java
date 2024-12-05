package com.debo.hw12.model;

import com.debo.hw12.enums.LoanStatus;
import com.debo.hw12.enums.PatronType;
import com.debo.hw12.model.accrual.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
public class LoanRecord {
    @EqualsAndHashCode.Include
    private final UUID id;
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private ItemCopy itemCopy;
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Patron patron;
    @EqualsAndHashCode.Exclude
    private LocalDateTime checkoutDate;
    @EqualsAndHashCode.Exclude
    private LocalDateTime dueDate;
    @EqualsAndHashCode.Exclude
    private LocalDateTime returnDate;
    @EqualsAndHashCode.Exclude
    private double lateFees;
    @EqualsAndHashCode.Exclude
    private LoanStatus status;
    @EqualsAndHashCode.Exclude
    private LateFeeAccrual lateFeeAccrual;


    public LoanRecord(ItemCopy itemCopy, Patron patron, LocalDateTime dueDate) {
        this.id = UUID.randomUUID();
        this.itemCopy = itemCopy;
        this.patron = patron;
        this.checkoutDate = LocalDateTime.now();
        this.dueDate = dueDate;
        this.status = LoanStatus.ACTIVE;
        this.lateFeeAccrual = assignDefaultLateFeeAccrual(patron.getType());
    }

    private LateFeeAccrual assignDefaultLateFeeAccrual(PatronType type) {
        return switch (type) {
            case STUDENT -> new WeeklyLateFeeAccrualImpl();
            case FACULTY -> new BiWeeklyLateFeeAccrualImpl();
            case SENIOR -> new MonthlyLateFeeAccrualImpl();
            default -> new DailyLateFeeAccrualImpl();
        };
    }
}
