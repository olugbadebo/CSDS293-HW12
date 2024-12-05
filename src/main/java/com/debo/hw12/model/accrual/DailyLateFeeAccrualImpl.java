package com.debo.hw12.model.accrual;

import com.debo.hw12.enums.LoanStatus;
import com.debo.hw12.model.LoanRecord;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@NoArgsConstructor
public class DailyLateFeeAccrualImpl implements LateFeeAccrual {
    final double PENALTY_FEE = 1.5;

    @Override
    public double calculateLateFee(LoanRecord loanRecord) {
        if (loanRecord.getStatus() == LoanStatus.RETURNED) {
            return 0.0;
        }

        LocalDateTime effectiveDate = loanRecord.getReturnDate() != null ? loanRecord.getReturnDate() : LocalDateTime.now();
        long daysLate = ChronoUnit.DAYS.between(loanRecord.getDueDate(), effectiveDate);
        return Math.max(0, daysLate * PENALTY_FEE);
    }
}
