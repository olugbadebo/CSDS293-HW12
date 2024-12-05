package com.debo.hw12.cli.menus;

import com.debo.hw12.exception.BusinessRuleException;
import com.debo.hw12.exception.EntityNotFoundException;
import com.debo.hw12.exception.ValidationException;
import com.debo.hw12.model.LoanRecord;
import com.debo.hw12.service.LoanRecordService;
import com.debo.hw12.util.Logger;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

public class LoanMenus {
    private static final Logger logger = Logger.getInstance();

    public static void checkOutItem(Scanner scanner, LoanRecordService loanService) {
        try {
            System.out.print("Enter patron ID: ");
            UUID patronId = UUID.fromString(scanner.nextLine().trim());
            System.out.print("Enter item copy ID: ");
            UUID itemId = UUID.fromString(scanner.nextLine().trim());

            LocalDateTime dueDate = LocalDateTime.now().plusDays(14);
            LoanRecord loan = loanService.checkoutItem(patronId, itemId, dueDate);
            System.out.println("Checkout successful. Loan ID: " + loan.getId());
        } catch (ValidationException e) {
            System.out.println("Validation error: " + e.getMessage());
        } catch (BusinessRuleException e) {
            System.out.println("Business rule violation: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid ID format. Please check and try again.");
        } catch (Exception e) {
            System.out.println("Error processing checkout: " + e.getMessage());
            logger.error("Error in checkout", e);
        }
    }

    public static void returnItem(Scanner scanner, LoanRecordService loanService) {
        try {
            System.out.print("Enter loan ID: ");
            UUID loanId = UUID.fromString(scanner.nextLine().trim());

            LoanRecord returned = loanService.returnItem(loanId);
            System.out.printf("Item returned successfully. Late fees: $%.2f%n", returned.getLateFees());
        } catch (EntityNotFoundException e) {
            System.out.println("Loan record not found.");
        } catch (ValidationException e) {
            System.out.println("Validation error: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid loan ID format.");
        } catch (Exception e) {
            System.out.println("Error processing return: " + e.getMessage());
            logger.error("Error in return", e);
        }
    }

    public static void viewActiveLoans(LoanRecordService loanService) {
        try {
            List<LoanRecord> activeLoans = loanService.getActiveLoans();
            if (activeLoans.isEmpty()) {
                System.out.println("No active loans found.");
                return;
            }

            System.out.println("\nActive Loans:");
            activeLoans.forEach(loan -> System.out.printf(
                    "Loan ID: %s\nPatron: %s\nItem: %s\nDue Date: %s\n-----------------\n",
                    loan.getId(), loan.getPatron().getName(),
                    loan.getItemCopy().getItem().getTitle(),
                    loan.getDueDate()));
        } catch (Exception e) {
            System.out.println("Error retrieving active loans: " + e.getMessage());
            logger.error("Error viewing active loans", e);
        }
    }

    public static void viewOverdueLoans(LoanRecordService loanService) {
        try {
            List<LoanRecord> overdueLoans = loanService.getOverdueLoans();
            if (overdueLoans.isEmpty()) {
                System.out.println("No overdue loans found.");
                return;
            }

            System.out.println("\nOverdue Loans:");
            overdueLoans.forEach(loan -> System.out.printf(
                    "Loan ID: %s\nPatron: %s\nItem: %s\nDue Date: %s\nDays Overdue: %d\n-----------------\n",
                    loan.getId(), loan.getPatron().getName(),
                    loan.getItemCopy().getItem().getTitle(),
                    loan.getDueDate(),
                    LocalDateTime.now().getDayOfYear() - loan.getDueDate().getDayOfYear()));
        } catch (Exception e) {
            System.out.println("Error retrieving overdue loans: " + e.getMessage());
            logger.error("Error viewing overdue loans", e);
        }
    }

    public static void calculateLateFees(Scanner scanner, LoanRecordService loanService) {
        try {
            System.out.print("Enter loan ID: ");
            UUID loanId = UUID.fromString(scanner.nextLine().trim());

            double fees = loanService.calculateLateFees(loanId);
            System.out.printf("Current late fees: $%.2f%n", fees);
        } catch (EntityNotFoundException e) {
            System.out.println("Loan record not found.");
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid loan ID format.");
        } catch (Exception e) {
            System.out.println("Error calculating late fees: " + e.getMessage());
            logger.error("Error calculating fees", e);
        }
    }
}