package com.debo.hw12.model;

import com.debo.hw12.enums.PatronType;
import lombok.*;

import java.time.LocalDateTime;
import java.util.*;

@Data
@Builder
@AllArgsConstructor
public class Patron {
    private final UUID id;
    private String name;
    private String email;
    private PatronType type;
    private LocalDateTime registrationDate;
    private boolean active;
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<LoanRecord> currentLoans;
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<LoanRecord> loanHistory;

    public Patron(String name, String email, PatronType type) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.email = email;
        this.type = type;
        this.registrationDate = LocalDateTime.now();
        this.active = true;
        this.currentLoans = new HashSet<>();
        this.loanHistory = new ArrayList<>();
    }
}
