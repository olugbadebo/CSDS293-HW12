package com.debo.hw12.service.impl;

import com.debo.hw12.enums.Condition;
import com.debo.hw12.exception.DuplicateEntityException;
import com.debo.hw12.exception.ValidationException;
import com.debo.hw12.model.Book;
import com.debo.hw12.service.BookService;
import com.debo.hw12.state.LibraryState;
import com.debo.hw12.util.Logger;

import java.util.*;


public class BookServiceImpl implements BookService {
    private final LibraryState libraryState = LibraryState.getInstance();
    private final Logger logger = Logger.getInstance();

    @Override
    public Book createBook(String title, String isbn, String author) throws DuplicateEntityException, ValidationException {
        validateBookData(title, isbn, author);
        validateIsbn(isbn);

        Book newBook = Book.builder()
                .id(UUID.randomUUID())
                .title(title)
                .isbn(isbn)
                .author(author)
                .active(true)
                .copies(new ArrayList<>())
                .build();

        libraryState.getBooks().put(newBook.getId(), newBook);
        logger.info("Created new book:" + newBook.getTitle());

        return newBook;

    }

    @Override
    public Book createBook(String title, String isbn, String author, String publisher, String publicationYear, Set<String> genres, String condition) throws DuplicateEntityException, ValidationException {
        validateBookData(title, isbn, author);
        validateIsbn(isbn);

        Book newBook = Book.builder()
                .id(UUID.randomUUID())
                .title(title)
                .isbn(isbn)
                .author(author)
                .active(true)
                .publisher(publisher)
                .publicationYear(Integer.parseInt(publicationYear))
                .genres(genres)
                .condition(validateCondition(condition))
                .build();

        libraryState.getBooks().put(newBook.getId(), newBook);
        logger.info("Created new book:" + newBook.getTitle());

        return newBook;

    }

    private static Condition validateCondition(String condition) throws ValidationException {
        try {
            return Condition.valueOf(condition);
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Invalid condition: " + condition);
        }
    }

    private void validateIsbn(String isbn) throws DuplicateEntityException {
        boolean isbnExists = libraryState.getBooks().values().stream()
                .anyMatch(book -> book.getIsbn().equals(isbn));

        if (isbnExists) {
            throw new DuplicateEntityException("Book with ISBN " + isbn + " already exists");
        }
    }

    @Override
    public Book updateBook(UUID id, String title, String author) {
        Book book = libraryState.getBooks().get(id);
        if (book == null) {
            throw new IllegalArgumentException("Book not found with ID: " + id);
        }

        book.setTitle(title);
        book.setAuthor(author);
        logger.info("Updated book:" + book.getTitle());

        return book;
    }

    @Override
    public Book updateBook(UUID id, String title, String author, String publisher, int publicationYear, Set<String> genres, Condition condition) throws ValidationException {
        Book book = libraryState.getBooks().get(id);
        if (book == null) {
            throw new IllegalArgumentException("Book not found with ID: " + id);
        }

        book.setTitle(title);
        book.setAuthor(author);
        book.setPublisher(publisher);
        book.setPublicationYear(publicationYear);
        book.setGenres(genres);
        book.setCondition(condition);
        logger.info("Updated book:" + book.getTitle());

        return book;
    }

    @Override
    public List<Book> searchBooks(String criteria, String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return List.of();
        }

        String normalizedTerm = searchTerm.toLowerCase().trim();

        return libraryState.getBooks().values().stream()
                .filter(book -> matchesCriteria(book, criteria, normalizedTerm))
                .filter(Book::isActive)
                .toList();
    }

    @Override
    public Optional<Book> getBookById(UUID id) {
        return Optional.ofNullable(libraryState.getBooks().get(id))
                .filter(Book::isActive);
    }

    @Override
    public void deleteBook(UUID id) {
        Book book = libraryState.getBooks().get(id);
        if (book == null) {
            throw new IllegalArgumentException("Book not found with ID: " + id);
        }

        book.setActive(false);
        logger.info("Marked book as inactive: " + book.getTitle());
    }

    @Override
    public Collection<Book> getAllBooks() {
        return libraryState.getBooks().values();
    }

    private boolean matchesCriteria(Book book, String criteria, String searchTerm) {
        return switch (criteria.toLowerCase()) {
            case "title" -> book.getTitle().toLowerCase().contains(searchTerm);
            case "author" -> book.getAuthor().toLowerCase().contains(searchTerm);
            case "isbn" -> book.getIsbn().contains(searchTerm);
            default -> false;
        };
    }

    private void validateBookData(String title, String isbn, String author) throws ValidationException {
        if (title == null || title.trim().isEmpty()) {
            throw new ValidationException("Title cannot be empty");
        }
        if (isbn == null || isbn.trim().isEmpty()) {
            throw new ValidationException("ISBN cannot be empty");
        }
        if (author == null || author.trim().isEmpty()) {
            throw new ValidationException("Author cannot be empty");
        }
    }
}
