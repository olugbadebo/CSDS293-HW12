package com.debo.hw12.enums;

import lombok.Getter;

@Getter
public enum PatronType {
    STANDARD(1),
    STUDENT(3),
    FACULTY(10),
    SENIOR(5);

    private final int maxItemLoanAmount;

    PatronType(int maxItemLoanAmount) {
        this.maxItemLoanAmount = maxItemLoanAmount;
    }
}
