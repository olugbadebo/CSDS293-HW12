package com.debo.hw12.cli.menus;

import com.debo.hw12.model.Reservation;
import com.debo.hw12.service.LoanRecordService;
import com.debo.hw12.exception.*;
import com.debo.hw12.util.Logger;

import java.util.*;
import java.time.format.DateTimeFormatter;

public class ReservationMenus {
    private static final Logger logger = Logger.getInstance();
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public static void makeReservation(Scanner scanner, LoanRecordService loanService) {
        try {
            System.out.print("Enter patron ID: ");
            UUID patronId = UUID.fromString(scanner.nextLine().trim());
            System.out.print("Enter item ID: ");
            UUID itemId = UUID.fromString(scanner.nextLine().trim());

            Reservation reservation = loanService.reserveItem(patronId, itemId);
            System.out.printf("""
                Reservation created successfully:
                ID: %s
                Queue Position: %d
                Expiry Date: %s
                """,
                    reservation.getId(),
                    reservation.getQueuePosition(),
                    reservation.getExpiryDate().format(DATE_FORMATTER));
        } catch (ValidationException | BusinessRuleException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid ID format. Please check and try again.");
        } catch (Exception e) {
            System.out.println("Unexpected error creating reservation.");
            logger.error("Error in reservation creation", e);
        }
    }

    public static void cancelReservation(Scanner scanner, LoanRecordService loanService) {
        try {
            System.out.print("Enter reservation ID: ");
            UUID reservationId = UUID.fromString(scanner.nextLine().trim());

            loanService.cancelReservation(reservationId);
            System.out.println("Reservation cancelled successfully.");
        } catch (ValidationException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid reservation ID format.");
        } catch (Exception e) {
            System.out.println("Unexpected error cancelling reservation.");
            logger.error("Error in reservation cancellation", e);
        }
    }

    public static void viewItemReservations(Scanner scanner, LoanRecordService loanService) {
        try {
            System.out.print("Enter item ID: ");
            UUID itemId = UUID.fromString(scanner.nextLine().trim());

            List<Reservation> reservations = loanService.getItemReservations(itemId);
            if (reservations.isEmpty()) {
                System.out.println("No reservations found for this item.");
                return;
            }

            System.out.println("\nCurrent Reservations:");
            reservations.forEach(res -> System.out.printf("""
                Reservation ID: %s
                Patron: %s
                Queue Position: %d
                Reservation Date: %s
                Expiry Date: %s
                Status: %s
                -----------------
                """,
                    res.getId(),
                    res.getPatron().getName(),
                    res.getQueuePosition(),
                    res.getReservationDate().format(DATE_FORMATTER),
                    res.getExpiryDate().format(DATE_FORMATTER),
                    res.getStatus()));
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid item ID format.");
        } catch (Exception e) {
            System.out.println("Error retrieving reservations.");
            logger.error("Error viewing item reservations", e);
        }
    }

    public static void viewPatronReservations(Scanner scanner, LoanRecordService loanService) {
        try {
            System.out.print("Enter patron ID: ");
            UUID patronId = UUID.fromString(scanner.nextLine().trim());

            List<Reservation> reservations = loanService.getPatronReservations(patronId);
            if (reservations.isEmpty()) {
                System.out.println("No active reservations found for this patron.");
                return;
            }

            System.out.println("\nActive Reservations:");
            reservations.forEach(res -> System.out.printf("""
                Item: %s
                Reservation ID: %s
                Queue Position: %d
                Reservation Date: %s
                Expiry Date: %s
                -----------------
                """,
                    res.getItem().getTitle(),
                    res.getId(),
                    res.getQueuePosition(),
                    res.getReservationDate().format(DATE_FORMATTER),
                    res.getExpiryDate().format(DATE_FORMATTER)));
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid patron ID format.");
        } catch (Exception e) {
            System.out.println("Error retrieving patron's reservations.");
            logger.error("Error viewing patron reservations", e);
        }
    }
}