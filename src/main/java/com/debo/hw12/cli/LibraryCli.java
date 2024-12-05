package com.debo.hw12.cli;

import com.debo.hw12.service.BookService;
import com.debo.hw12.service.LoanRecordService;
import com.debo.hw12.service.PatronService;
import com.debo.hw12.service.impl.BookServiceImpl;
import com.debo.hw12.service.impl.LoanRecordServiceImpl;
import com.debo.hw12.service.impl.PatronServiceImpl;
import com.debo.hw12.util.Logger;

import java.util.Scanner;

import static com.debo.hw12.cli.menus.BookMenus.*;
import static com.debo.hw12.cli.menus.LoanMenus.*;
import static com.debo.hw12.cli.menus.PatronMenus.*;
import static com.debo.hw12.cli.menus.ReservationMenus.*;

public class LibraryCli {
    private final BookService bookService;
    private final PatronService patronService;
    private final LoanRecordService loanService;
    private final Scanner scanner;
    private final Logger logger;

    public LibraryCli() {
        this.bookService = new BookServiceImpl();
        this.patronService = new PatronServiceImpl();
        this.loanService = new LoanRecordServiceImpl(patronService);
        this.scanner = new Scanner(System.in);
        this.logger = Logger.getInstance();
    }

    public void start() {
        boolean running = true;
        while (running) {
            displayMainMenu();
            String choice = scanner.nextLine().trim();

            try {
                switch (choice) {
                    case "1" -> handleBookOperations();
                    case "2" -> handlePatronOperations();
                    case "3" -> handleLoanOperations();
                    case "4" -> handleReservationOperations();
                    case "5" -> {
                        running = false;
                        logger.info("Exiting system");
                    }
                    default -> System.out.println("Invalid choice. Please try again.");
                }
            } catch (Exception e) {
                logger.error("Error in main menu operation", e);
                System.out.println("An error occurred: " + e.getMessage());
            }
        }
        scanner.close();
    }

    private void displayMainMenu() {
        System.out.println("\nLibrary Management System");
        System.out.println("1. Book Operations");
        System.out.println("2. Patron Operations");
        System.out.println("3. Loan Operations");
        System.out.println("4. Reservation Operations");
        System.out.println("5. Exit");
        System.out.print("Enter your choice: ");
    }

    private void handleBookOperations() {
        while (true) {
            System.out.println("\nBook Operations");
            System.out.println("1. Add Book");
            System.out.println("2. Search Books");
            System.out.println("3. View All Books");
            System.out.println("4. Get Book");
            System.out.println("5. Update Book");
            System.out.println("6. Return to Main Menu");
            System.out.print("Enter your choice: ");

            String choice = scanner.nextLine().trim();
            if (choice.equals("6")) break;

            try {
                switch (choice) {
                    case "1" -> addBook(scanner, bookService);
                    case "2" -> searchBooks(scanner, bookService);
                    case "3" -> indexBooks(bookService);
                    case "4" -> getBook(scanner, bookService);
                    case "5" -> updateBook(scanner, bookService);
                    default -> System.out.println("Invalid choice");
                }
            } catch (Exception e) {
                logger.error("Error in book operation", e);
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private void handleLoanOperations() {
        while (true) {
            System.out.println("\nLoan Operations");
            System.out.println("1. Check Out Item");
            System.out.println("2. Return Item");
            System.out.println("3. View Active Loans");
            System.out.println("4. View Overdue Loans");
            System.out.println("5. Calculate Late Fees");
            System.out.println("6. Return to Main Menu");
            System.out.print("Enter your choice: ");

            String choice = scanner.nextLine().trim();
            if (choice.equals("6")) break;

            try {
                switch (choice) {
                    case "1" -> checkOutItem(scanner, loanService);
                    case "2" -> returnItem(scanner, loanService);
                    case "3" -> viewActiveLoans(loanService);
                    case "4" -> viewOverdueLoans(loanService);
                    case "5" -> calculateLateFees(scanner, loanService);
                    default -> System.out.println("Invalid choice");
                }
            } catch (Exception e) {
                logger.error("Error in loan operation", e);
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private void handleReservationOperations() {
        while (true) {
            System.out.println("\nReservation Operations");
            System.out.println("1. Make Reservation");
            System.out.println("2. Cancel Reservation");
            System.out.println("3. View Item Reservations");
            System.out.println("4. View Patron Reservations");
            System.out.println("5. Return to Main Menu");
            System.out.print("Enter your choice: ");

            String choice = scanner.nextLine().trim();
            if (choice.equals("5")) break;

            try {
                switch (choice) {
                    case "1" -> makeReservation(scanner, loanService);
                    case "2" -> cancelReservation(scanner, loanService);
                    case "3" -> viewItemReservations(scanner, loanService);
                    case "4" -> viewPatronReservations(scanner, loanService);
                    default -> System.out.println("Invalid choice");
                }
            } catch (Exception e) {
                logger.error("Error in reservation operation", e);
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private void handlePatronOperations() {
        while (true) {
            System.out.println("\nPatron Operations");
            System.out.println("1. Register Patron");
            System.out.println("2. Search Patrons");
            System.out.println("3. Update Patron");
            System.out.println("4. Deactivate Patron");
            System.out.println("5. Return to Main Menu");
            System.out.print("Enter your choice: ");

            String choice = scanner.nextLine().trim();
            if (choice.equals("5")) break;

            try {
                switch (choice) {
                    case "1" -> registerPatron(scanner, patronService);
                    case "2" -> searchPatrons(scanner, patronService);
                    case "3" -> updatePatron(scanner, patronService);
                    case "4" -> deactivatePatron(scanner, patronService);
                    default -> System.out.println("Invalid choice");
                }
            } catch (Exception e) {
                logger.error("Error in patron operation", e);
                System.out.println("Error: " + e.getMessage());
            }
        }
    }
}
