package com.debo.hw12.service;

import com.debo.hw12.enums.Condition;
import com.debo.hw12.exception.DuplicateEntityException;
import com.debo.hw12.exception.ValidationException;
import com.debo.hw12.model.Book;
import com.debo.hw12.service.impl.BookServiceImpl;
import com.debo.hw12.state.LibraryState;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("BookService Implementation Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BookTest {

    private BookServiceImpl bookService;
    private LibraryState libraryState;

    @BeforeEach
    void setUp() {
        bookService = new BookServiceImpl();
        libraryState = LibraryState.getInstance();
        libraryState.getBooks().clear();
    }

    @Nested
    @DisplayName("Book Creation Tests")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class BookCreationTests {

        @Test
        @Order(1)
        @DisplayName("Should successfully create a basic book with required fields")
        void shouldCreateBasicBook() throws DuplicateEntityException, ValidationException {
            String title = "The Great Gatsby";
            String isbn = "123456789";
            String author = "Scott Fitzgerald";

            Book createdBook = bookService.createBook(title, isbn, author);

            assertThat(createdBook).isNotNull();
            assertThat(createdBook.getTitle()).isEqualTo(title);
            assertThat(createdBook.getIsbn()).isEqualTo(isbn);
            assertThat(createdBook.getAuthor()).isEqualTo(author);
            assertThat(createdBook.isActive()).isTrue();
        }

        @Test
        @Order(2)
        @DisplayName("Should successfully create a detailed book with all fields")
        void shouldCreateDetailedBook() throws DuplicateEntityException, ValidationException {
            String title = "1984";
            String isbn = "1234567890";
            String author = "George Orwell";
            String publisher = "Debo Books";
            String publicationYear = "1949";
            Set<String> genres = Set.of("Dystopian", "Fiction");
            String condition = "GOOD";

            Book createdBook = bookService.createBook(title, isbn, author, publisher,
                    publicationYear, genres, condition);

            assertThat(createdBook).isNotNull();
            assertThat(createdBook.getTitle()).isEqualTo(title);
            assertThat(createdBook.getPublisher()).isEqualTo(publisher);
            assertThat(createdBook.getPublicationYear()).isEqualTo(1949);
            assertThat(createdBook.getGenres()).containsAll(genres);
            assertThat(createdBook.getCondition()).isEqualTo(Condition.GOOD);
        }

        @Test
        @Order(3)
        @DisplayName("Should prevent creation of book with duplicate ISBN")
        void shouldPreventDuplicateIsbn() throws DuplicateEntityException, ValidationException {
            String isbn = "978-3-16-148410-0";
            bookService.createBook("First Book", isbn, "Author One");

            assertThatThrownBy(() ->
                    bookService.createBook("Second Book", isbn, "Author Two"))
                    .isInstanceOf(DuplicateEntityException.class)
                    .hasMessageContaining("already exists");
        }
    }

    @Nested
    @DisplayName("Book Validation Tests")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class BookValidationTests {

        @ParameterizedTest
        @Order(1)
        @DisplayName("Should validate required fields")
        @CsvSource({
                ",,Author,Title cannot be empty",
                "Title,,Author,ISBN cannot be empty",
                "Title,ISBN,,Author cannot be empty"
        })
        void shouldValidateRequiredFields(String title, String isbn, String author, String expectedMessage) {
            assertThatThrownBy(() ->
                    bookService.createBook(title, isbn, author))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining(expectedMessage);
        }

        @Test
        @Order(2)
        @DisplayName("Should validate condition enum values")
        void shouldValidateConditionEnum() {
            assertThatThrownBy(() ->
                    bookService.createBook("Title", "ISBN", "Author", "Publisher",
                            "2020", Set.of(), "INVALID_CONDITION"))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("Invalid condition");
        }
    }

    @Nested
    @DisplayName("Book Search and Retrieval Tests")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class BookSearchTests {

        @BeforeEach
        void setUpTestData() throws DuplicateEntityException, ValidationException {
            bookService.createBook("The Great Gatsby", "978-1", "F. Scott Fitzgerald");
            bookService.createBook("1984", "978-2", "George Orwell");
            bookService.createBook("Brave New World", "978-3", "Aldous Huxley");
        }

        @Test
        @Order(1)
        @DisplayName("Should find books by title")
        void shouldFindBooksByTitle() {
            List<Book> books = bookService.searchBooks("title", "Great");
            assertThat(books).hasSize(1);
            assertThat(books.get(0).getTitle()).contains("Great");
        }

        @Test
        @Order(2)
        @DisplayName("Should find books by author")
        void shouldFindBooksByAuthor() {
            List<Book> books = bookService.searchBooks("author", "Orwell");
            assertThat(books).hasSize(1);
            assertThat(books.get(0).getAuthor()).contains("Orwell");
        }

        @Test
        @Order(3)
        @DisplayName("Should find books by ISBN")
        void shouldFindBooksByIsbn() {
            List<Book> books = bookService.searchBooks("isbn", "978-1");
            assertThat(books).hasSize(1);
            assertThat(books.get(0).getIsbn()).isEqualTo("978-1");
        }

        @Test
        @Order(4)
        @DisplayName("Should return empty list for non-matching criteria")
        void shouldReturnEmptyListForNonMatching() {
            List<Book> books = bookService.searchBooks("title", "NonExistent");
            assertThat(books).isEmpty();
        }
    }

    @Nested
    @DisplayName("Book Update and Delete Tests")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class BookUpdateAndDeleteTests {

        private UUID bookId;

        @BeforeEach
        void setUpTestBook() throws DuplicateEntityException, ValidationException {
            Book book = bookService.createBook("Original Title", "978-test", "Original Author");
            bookId = book.getId();
        }

        @Test
        @Order(1)
        @DisplayName("Should successfully update book details")
        void shouldUpdateBook() {
            Book updatedBook = bookService.updateBook(bookId, "New Title", "New Author");

            assertThat(updatedBook.getTitle()).isEqualTo("New Title");
            assertThat(updatedBook.getAuthor()).isEqualTo("New Author");
        }

        @Test
        @Order(2)
        @DisplayName("Should mark book as inactive when deleted")
        void shouldMarkBookAsInactive() {
            bookService.deleteBook(bookId);

            Optional<Book> deletedBook = bookService.getBookById(bookId);
            assertThat(deletedBook).isEmpty();
        }

        @Test
        @Order(3)
        @DisplayName("Should throw exception when updating non-existent book")
        void shouldThrowExceptionForNonExistentBookUpdate() {
            assertThatThrownBy(() ->
                    bookService.updateBook(UUID.randomUUID(), "Title", "Author"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Book not found");
        }
    }
}