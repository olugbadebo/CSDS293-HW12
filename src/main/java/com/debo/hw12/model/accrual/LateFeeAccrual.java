package com.debo.hw12.model.accrual;

import com.debo.hw12.enums.LoanStatus;
import com.debo.hw12.model.LoanRecord;

public interface LateFeeAccrual {
    double calculateLateFee(LoanRecord loanRecord);
}
