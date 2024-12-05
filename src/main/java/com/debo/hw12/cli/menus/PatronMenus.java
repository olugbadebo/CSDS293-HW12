package com.debo.hw12.cli.menus;

import com.debo.hw12.exception.BusinessRuleException;
import com.debo.hw12.exception.DuplicateEntityException;
import com.debo.hw12.exception.ValidationException;
import com.debo.hw12.model.Patron;
import com.debo.hw12.service.PatronService;
import com.debo.hw12.util.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

public class PatronMenus {
    private static final Logger logger = Logger.getInstance();

    public static void registerPatron(Scanner scanner, PatronService patronService) {
        try {
            System.out.print("Enter name: ");
            String name = scanner.nextLine();
            System.out.print("Enter email: ");
            String email = scanner.nextLine();
            System.out.print("Enter type (STANDARD/STUDENT/FACULTY/SENIOR): ");
            String type = scanner.nextLine().toUpperCase();

            Patron patron = patronService.registerPatron(name, email, type);
            System.out.println("Patron registered successfully. ID: " + patron.getId());
        } catch (ValidationException e) {
            System.out.println("Invalid input: " + e.getMessage());
        } catch (DuplicateEntityException e) {
            System.out.println("Email already registered: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Error registering patron: " + e.getMessage());
            logger.error("Error registering patron", e);
        }
    }

    public static void searchPatrons(Scanner scanner, PatronService patronService) {
        try {
            System.out.print("Search by (name/email/type): ");
            String criteria = scanner.nextLine().toLowerCase();
            if (!Arrays.asList("name", "email", "type").contains(criteria)) {
                System.out.println("Invalid search criteria. Please use name, email, or type.");
                return;
            }

            System.out.print("Enter search term: ");
            String term = scanner.nextLine();

            List<Patron> patrons = patronService.searchPatrons(criteria, term);
            if (patrons.isEmpty()) {
                System.out.println("No patrons found.");
                return;
            }

            System.out.println("\nSearch Results:");
            patrons.forEach(patron -> System.out.printf(
                    "ID: %s\nName: %s\nEmail: %s\nType: %s\n-----------------\n",
                    patron.getId(), patron.getName(), patron.getEmail(), patron.getType()));
        } catch (Exception e) {
            System.out.println("Error searching patrons: " + e.getMessage());
            logger.error("Error searching patrons", e);
        }
    }

    public static void updatePatron(Scanner scanner, PatronService patronService) {
        try {
            System.out.print("Enter patron ID: ");
            UUID id = UUID.fromString(scanner.nextLine().trim());
            System.out.print("Enter new name: ");
            String name = scanner.nextLine();
            System.out.print("Enter new email: ");
            String email = scanner.nextLine();

            Patron updated = patronService.updatePatron(id, name, email);
            System.out.println("Patron updated successfully: " + updated.getName());
        } catch (ValidationException | DuplicateEntityException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid patron ID format.");
        } catch (Exception e) {
            System.out.println("Error updating patron: " + e.getMessage());
            logger.error("Error updating patron", e);
        }
    }

    public static void deactivatePatron(Scanner scanner, PatronService patronService) {
        try {
            System.out.print("Enter patron ID to deactivate: ");
            UUID id = UUID.fromString(scanner.nextLine().trim());

            patronService.deactivatePatron(id);
            System.out.println("Patron deactivated successfully.");
        } catch (ValidationException | BusinessRuleException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid patron ID format.");
        } catch (Exception e) {
            System.out.println("Error deactivating patron: " + e.getMessage());
            logger.error("Error deactivating patron", e);
        }
    }
}