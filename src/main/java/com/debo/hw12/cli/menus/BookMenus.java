package com.debo.hw12.cli.menus;

import com.debo.hw12.enums.Condition;
import com.debo.hw12.exception.DuplicateEntityException;
import com.debo.hw12.exception.ValidationException;
import com.debo.hw12.model.Book;
import com.debo.hw12.model.ItemCopy;
import com.debo.hw12.service.BookService;
import com.debo.hw12.util.Logger;

import java.time.format.DateTimeFormatter;
import java.util.*;

public class BookMenus {
    private static final Logger logger = Logger.getInstance();

    public static void addBook(Scanner scanner, BookService bookService) {
        try {
            System.out.print("Enter title: ");
            String title = scanner.nextLine();
            System.out.print("Enter ISBN: ");
            String isbn = scanner.nextLine();
            System.out.print("Enter author: ");
            String author = scanner.nextLine();

            Book book = bookService.createBook(title, isbn, author);
            System.out.println("Book added successfully. ID: " + book.getId());
        } catch (ValidationException e) {
            System.out.println("Invalid input: " + e.getMessage());
        } catch (DuplicateEntityException e) {
            System.out.println("Book already exists: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Unexpected error occurred. Please try again.");
            logger.error("Error adding book", e);
        }
    }

    public static void searchBooks(Scanner scanner, BookService bookService) {
        try {
            System.out.print("Search by (title/author/isbn): ");
            String criteria = scanner.nextLine().toLowerCase();
            if (!Arrays.asList("title", "author", "isbn").contains(criteria)) {
                System.out.println("Invalid search criteria. Please use title, author, or isbn.");
                return;
            }

            System.out.print("Enter search term: ");
            String term = scanner.nextLine();

            List<Book> books = bookService.searchBooks(criteria, term);
            if (books.isEmpty()) {
                System.out.println("No books found.");
                return;
            }

            System.out.println("\nSearch Results:");
            books.forEach(book -> System.out.printf("ID: %s\nTitle: %s\nAuthor: %s\nISBN: %s\n-----------------\n",
                    book.getId(), book.getTitle(), book.getAuthor(), book.getIsbn()));
        } catch (Exception e) {
            System.out.println("Error searching books: " + e.getMessage());
            logger.error("Error searching books", e);
        }
    }

    public static void indexBooks(BookService bookService) {
        try {
            Collection<Book> books = bookService.getAllBooks();
            if (books.isEmpty()) {
                System.out.println("No books in the library.");
                return;
            }

            System.out.println("\nAll Books:");
            books.forEach(book -> System.out.printf("""
                            ID: %s
                            Title: %s
                            Author: %s
                            ISBN: %s
                            Publisher: %s
                            Year: %d
                            Condition: %s
                            Genres: %s
                            -----------------
                            """,
                    book.getId(), book.getTitle(), book.getAuthor(), book.getIsbn(),
                    book.getPublisher(), book.getPublicationYear(), book.getCondition(),
                    String.join(", ", book.getGenres())));
        } catch (Exception e) {
            System.out.println("Error retrieving books.");
            logger.error("Error listing books", e);
        }
    }

    public static void updateBook(Scanner scanner, BookService bookService) {
        try {
            System.out.print("Enter book ID: ");
            UUID id = UUID.fromString(scanner.nextLine().trim());

            Book currentBook = bookService.getBookById(id)
                    .orElseThrow(() -> new ValidationException("Book not found"));

            System.out.println("Press Enter to keep current value, or enter new value");

            System.out.printf("Title [%s]: ", currentBook.getTitle());
            String title = scanner.nextLine();
            title = title.isEmpty() ? currentBook.getTitle() : title;

            System.out.printf("Author [%s]: ", currentBook.getAuthor());
            String author = scanner.nextLine();
            author = author.isEmpty() ? currentBook.getAuthor() : author;

            System.out.printf("Publisher [%s]: ", currentBook.getPublisher());
            String publisher = scanner.nextLine();
            publisher = publisher.isEmpty() ? currentBook.getPublisher() : publisher;

            System.out.printf("Publication Year [%d]: ", currentBook.getPublicationYear());
            String yearStr = scanner.nextLine();
            int year = yearStr.isEmpty() ? currentBook.getPublicationYear() : Integer.parseInt(yearStr);

            System.out.printf("Condition [%s]: ", currentBook.getCondition());
            String conditionStr = scanner.nextLine().toUpperCase();
            Condition condition = conditionStr.isEmpty() ? currentBook.getCondition() : Condition.valueOf(conditionStr);

            System.out.println("Current genres: " + String.join(", ", currentBook.getGenres()));
            System.out.println("Enter new genres (comma-separated) or press Enter to keep current:");
            String genresInput = scanner.nextLine();
            Set<String> genres = genresInput.isEmpty() ? currentBook.getGenres() :
                    new HashSet<>(Arrays.asList(genresInput.split("\\s*,\\s*")));

            Book updated = bookService.updateBook(id, title, author, publisher, year, genres, condition);
            System.out.println("Book updated successfully: " + updated.getTitle());
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid input format.");
        } catch (ValidationException e) {
            System.out.println("Validation error: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Error updating book: " + e.getMessage());
            logger.error("Error updating book", e);
        }
    }

    public static void deleteBook(Scanner scanner, BookService bookService) {
        try {
            System.out.print("Enter book ID to delete: ");
            UUID id = UUID.fromString(scanner.nextLine().trim());

            bookService.deleteBook(id);
            System.out.println("Book deleted successfully.");
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid book ID format or book not found.");
        } catch (Exception e) {
            System.out.println("Error deleting book: " + e.getMessage());
            logger.error("Error deleting book", e);
        }
    }

    public static void getBook(Scanner scanner, BookService bookService) {
        try {
            System.out.print("Enter book ID: ");
            UUID id = UUID.fromString(scanner.nextLine().trim());

            Optional<Book> bookOpt = bookService.getBookById(id);
            if (bookOpt.isEmpty()) {
                System.out.println("Book not found.");
                return;
            }

            Book book = bookOpt.get();
            System.out.printf("""
            Book Details:
            ======================
            ID: %s
            Title: %s
            Author: %s
            ISBN: %s
            Publisher: %s
            Publication Year: %d
            Condition: %s
            Genres: %s
            Active: %s
            
            Copies Information:
            ======================
            Total Copies: %d
            Available Copies: %d
            
            Copy Details:
            ----------------------
            %s
            """,
                    book.getId(),
                    book.getTitle(),
                    book.getAuthor(),
                    book.getIsbn(),
                    book.getPublisher(),
                    book.getPublicationYear(),
                    book.getCondition(),
                    String.join(", ", book.getGenres()),
                    book.isActive() ? "Yes" : "No",
                    book.getTotalCopiesCount(),
                    book.getAvailableCopiesCount(),
                    formatCopyDetails(book.getCopies())
            );
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid book ID format.");
        } catch (Exception e) {
            System.out.println("Error retrieving book details: " + e.getMessage());
            logger.error("Error retrieving book details", e);
        }
    }

    private static String formatCopyDetails(List<ItemCopy> copies) {
        if (copies == null || copies.isEmpty()) {
            return "No copies available";
        }

        StringBuilder sb = new StringBuilder();
        for (ItemCopy copy : copies) {
            sb.append(String.format("""
            Copy ID: %s
            Barcode: %s
            Location: %s
            Status: %s
            Type: %s
            Condition: %s
            Acquisition Date: %s
            -------------
            """,
                    copy.getId(),
                    copy.getBarcode(),
                    copy.getLocation(),
                    copy.getStatus(),
                    copy.getType(),
                    copy.getCondition(),
                    copy.getAcquisitionDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            ));
        }
        return sb.toString();
    }
}
