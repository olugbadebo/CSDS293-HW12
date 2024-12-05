package com.debo.hw12.observer.impl;

import com.debo.hw12.enums.ItemStatus;
import com.debo.hw12.model.ItemCopy;
import com.debo.hw12.observer.InventoryObserver;
import com.debo.hw12.state.LibraryState;
import com.debo.hw12.util.EmailService;

public class PatronNotifier implements InventoryObserver {
    private final EmailService emailService;

    public PatronNotifier(EmailService emailService) {
        this.emailService = emailService;
    }

    @Override
    public void onInventoryChange(ItemCopy copy) {
        if (copy.getStatus() == ItemStatus.AVAILABLE) {
            notifyWaitingPatrons(copy);
        }
    }

    private void notifyWaitingPatrons(ItemCopy copy) {
        LibraryState.getInstance().getReservations().values().stream()
                .filter(reservation -> reservation.getItem().getId().equals(copy.getItem().getId()))
                .forEach(reservation -> {
                    String message = String.format("The book '%s' is now available.",
                            copy.getItem().getTitle());
                    emailService.sendEmail(reservation.getPatron().getEmail(),
                            "Book Available", message);
                });
    }
}
