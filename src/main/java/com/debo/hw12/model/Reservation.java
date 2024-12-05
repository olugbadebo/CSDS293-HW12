package com.debo.hw12.model;

import com.debo.hw12.enums.ReservationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor

public class Reservation {
    private final UUID id;
    private LibraryItem item;
    private Patron patron;
    private LocalDateTime reservationDate;
    private LocalDateTime expiryDate;
    private ReservationStatus status;
    private int queuePosition;

    public Reservation(LibraryItem item, Patron patron) {
        this.id = UUID.randomUUID();
        this.item = item;
        this.patron = patron;
        this.reservationDate = LocalDateTime.now();
        this.status = ReservationStatus.PENDING;
        this.expiryDate = LocalDateTime.now().plusDays(30);
    }
}
