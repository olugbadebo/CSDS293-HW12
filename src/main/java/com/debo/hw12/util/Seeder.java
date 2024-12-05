package com.debo.hw12.util;

import lombok.RequiredArgsConstructor;
import com.debo.hw12.model.Book;
import com.debo.hw12.model.ItemCopy;
import com.debo.hw12.enums.Condition;
import com.debo.hw12.enums.ItemStatus;
import com.debo.hw12.enums.ItemType;
import com.debo.hw12.state.LibraryState;

import java.time.LocalDateTime;
import java.util.*;

// Utility class to seed the AppState with book data
@RequiredArgsConstructor
public class Seeder {
    private final LibraryState libraryState;
    private final Logger logger;
    private final Random random = new Random();

    // Sample data for generating realistic books
    private static final String[] BOOK_TITLES = {
            "The Art of Computer Programming",
            "Design Patterns",
            "Clean Code",
            "Refactoring",
            "Introduction to Algorithms",
            "Head First Java",
            "Effective Java",
            "Domain-Driven Design",
            "The Pragmatic Programmer",
            "Code Complete"
    };

    private static final String[] AUTHORS = {
            "Donald Knuth",
            "Erich Gamma",
            "Robert C. Martin",
            "Martin Fowler",
            "Thomas H. Cormen",
            "Kathy Sierra",
            "Joshua Bloch",
            "Eric Evans",
            "Andy Hunt",
            "Steve McConnell"
    };

    private static final String[] PUBLISHERS = {
            "Addison-Wesley",
            "O'Reilly Media",
            "Prentice Hall",
            "Manning Publications",
            "MIT Press"
    };

    private static final String[] GENRES = {
            "Computer Science",
            "Programming",
            "Software Engineering",
            "Algorithms",
            "Design Patterns"
    };

    // Creating 20 copies of 10 books each
    public void seedLibrary() {
        logger.info("Starting library seeding process");

        for (int i = 0; i < 10; i++) {
            Book book = createBook(i);
            libraryState.getBooks().put(book.getId(), book);

            for (int j = 0; j < 20; j++) {
                ItemCopy copy = createCopy(book, j);
                libraryState.getItemCopies().put(copy.getId(), copy);
                logger.debug(String.format("Created copy %d for book: %s", j + 1, book.getTitle()));
            }

            logger.info(String.format("Created book with 20 copies: %s", book.getTitle()));
        }

        logger.info("Library seeding completed successfully");
    }

    private Book createBook(int index) {
        String title = BOOK_TITLES[index];
        String author = AUTHORS[index];
        String publisher = PUBLISHERS[random.nextInt(PUBLISHERS.length)];
        int publicationYear = 2010 + random.nextInt(14);

        Book book = Book.builder()
                .id(UUID.randomUUID())
                .title(title)
                .author(author)
                .publisher(publisher)
                .publicationYear(publicationYear)
                .isbn(generateIsbn())
                .active(true)
                .build();

        int genreCount = 2 + random.nextInt(2);
        List<String> genreList = new ArrayList<>(Arrays.asList(GENRES));
        Collections.shuffle(genreList);
        for (int i = 0; i < genreCount; i++) {
            book.addGenre(genreList.get(i));
        }

        return book;
    }

    private ItemCopy createCopy(Book book, int copyNumber) {
        String barcode = String.format("%s-%03d", book.getIsbn(), copyNumber + 1);

        return ItemCopy.builder()
                .id(UUID.randomUUID())
                .item(book)
                .barcode(barcode)
                .type(randomItemType())
                .condition(randomCondition())
                .status(ItemStatus.AVAILABLE)
                .location(generateLocation())
                .acquisitionDate(randomAcquisitionDate())
                .build();
    }

    private String generateIsbn() {
        StringBuilder isbn = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            isbn.append(random.nextInt(10));
        }
        return isbn.toString();
    }

    private String generateLocation() {
        int floor = 1 + random.nextInt(3);
        String section = "CS";
        int shelf = random.nextInt(100);
        return String.format("%dF-%s-%03d", floor, section, shelf);
    }

    private ItemType randomItemType() {
        return ItemType.values()[random.nextInt(ItemType.values().length)];
    }

    private Condition randomCondition() {
        return Condition.values()[random.nextInt(Condition.values().length)];
    }

    private LocalDateTime randomAcquisitionDate() {
        int daysAgo = 365 + random.nextInt(4 * 365);
        return LocalDateTime.now().minusDays(daysAgo);
    }
}