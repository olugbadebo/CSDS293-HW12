package com.debo.hw12.service;

import com.debo.hw12.enums.Condition;
import com.debo.hw12.exception.DuplicateEntityException;
import com.debo.hw12.exception.ValidationException;
import com.debo.hw12.model.Book;

import java.util.*;

public interface BookService {
    Book createBook(String title, String isbn, String author) throws DuplicateEntityException, ValidationException;
    Book createBook(String title, String isbn, String author, String publisher, String publicationYear, Set<String> genres, String condition) throws DuplicateEntityException, ValidationException;

    Book updateBook(UUID id, String title, String author);
    Book updateBook(UUID id, String title, String author, String publisher, int publicationYear, Set<String> genres, Condition condition) throws ValidationException;
    List<Book> searchBooks(String criteria, String searchTerm);

    Optional<Book> getBookById(UUID id);

    void deleteBook(UUID id);

    Collection<Book> getAllBooks();
}
