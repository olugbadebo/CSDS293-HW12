package com.debo.hw12.service;

import com.debo.hw12.enums.Condition;
import com.debo.hw12.enums.ItemStatus;
import com.debo.hw12.enums.ItemType;
import com.debo.hw12.model.Book;
import com.debo.hw12.model.ItemCopy;
import com.debo.hw12.observer.impl.AvailabilityObserver;
import com.debo.hw12.observer.impl.InventoryObserverImpl;
import com.debo.hw12.state.LibraryState;
import org.junit.jupiter.api.*;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Inventory Management Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class InventoryTest {
    private LibraryState libraryState;
    private Book testBook;
    private AvailabilityObserver availabilityObserver;
    private InventoryObserverImpl inventoryObserver;

    @BeforeEach
    void setUp() {
        libraryState = LibraryState.getInstance();
        libraryState.getItemCopies().clear();

        testBook = Book.builder()
                .id(UUID.randomUUID())
                .title("Test Book")
                .isbn("1234567890")
                .author("Test Author")
                .build();

        availabilityObserver = new AvailabilityObserver();
        inventoryObserver = new InventoryObserverImpl();
        libraryState.addObserver(availabilityObserver);
        libraryState.addObserver(inventoryObserver);
    }

    @Nested
    @DisplayName("ItemCopy Management Tests")
    class ItemCopyTests {

        @Test
        @Order(1)
        @DisplayName("Should create item copy with correct defaults")
        void shouldCreateItemCopy() {
            ItemCopy copy = new ItemCopy(testBook, "BC001", "SHELF-A1");

            assertThat(copy.getId()).isNotNull();
            assertThat(copy.getBarcode()).isEqualTo("BC001");
            assertThat(copy.getLocation()).isEqualTo("SHELF-A1");
            assertThat(copy.getStatus()).isEqualTo(ItemStatus.AVAILABLE);
            assertThat(copy.getCondition()).isEqualTo(Condition.GOOD);
            assertThat(copy.getAcquisitionDate()).isBeforeOrEqualTo(LocalDateTime.now());
        }

        @Test
        @Order(2)
        @DisplayName("Should create item copy with custom attributes")
        void shouldCreateItemCopyWithCustomAttributes() {
            ItemCopy copy = ItemCopy.builder()
                    .id(UUID.randomUUID())
                    .item(testBook)
                    .barcode("BC002")
                    .location("SHELF-B2")
                    .status(ItemStatus.UNDER_MAINTENANCE)
                    .condition(Condition.FAIR)
                    .type(ItemType.HARD_COVER)
                    .build();

            assertThat(copy.getStatus()).isEqualTo(ItemStatus.UNDER_MAINTENANCE);
            assertThat(copy.getCondition()).isEqualTo(Condition.FAIR);
            assertThat(copy.getType()).isEqualTo(ItemType.HARD_COVER);
        }

        @Test
        @Order(3)
        @DisplayName("Should track availability changes")
        void shouldTrackAvailabilityChanges() {
            ItemCopy copy = new ItemCopy(testBook, "BC003", "SHELF-C3");
            libraryState.getItemCopies().put(copy.getId(), copy);
            libraryState.notifyInventoryChange(copy);

            assertThat(availabilityObserver.getAvailableCopiesCount(testBook.getId())).isEqualTo(1);

            copy.setStatus(ItemStatus.CHECKED_OUT);
            libraryState.notifyInventoryChange(copy);

            assertThat(availabilityObserver.getAvailableCopiesCount(testBook.getId())).isZero();
        }

        @Test
        @Order(4)
        @DisplayName("Should handle multiple copies of same book")
        void shouldHandleMultipleCopies() {
            ItemCopy copy1 = new ItemCopy(testBook, "BC004", "SHELF-D4");
            ItemCopy copy2 = new ItemCopy(testBook, "BC005", "SHELF-D5");

            libraryState.getItemCopies().put(copy1.getId(), copy1);
            libraryState.getItemCopies().put(copy2.getId(), copy2);

            libraryState.notifyInventoryChange(copy1);
            libraryState.notifyInventoryChange(copy2);

            assertThat(availabilityObserver.getAvailableCopiesCount(testBook.getId())).isEqualTo(2);

            copy1.setStatus(ItemStatus.LOST);
            libraryState.notifyInventoryChange(copy1);

            assertThat(availabilityObserver.getAvailableCopiesCount(testBook.getId())).isEqualTo(1);
        }

        @Test
        @Order(5)
        @DisplayName("Should track condition changes")
        void shouldTrackConditionChanges() {
            ItemCopy copy = new ItemCopy(testBook, "BC006", "SHELF-E6");
            libraryState.getItemCopies().put(copy.getId(), copy);

            assertThat(copy.getCondition()).isEqualTo(Condition.GOOD);

            copy.setCondition(Condition.FAIR);
            libraryState.notifyInventoryChange(copy);

            ItemCopy retrievedCopy = libraryState.getItemCopies().get(copy.getId());
            assertThat(retrievedCopy.getCondition()).isEqualTo(Condition.FAIR);
        }

        @Test
        @Order(6)
        @DisplayName("Should handle removal from inventory")
        void shouldHandleRemoval() {
            ItemCopy copy = new ItemCopy(testBook, "BC007", "SHELF-F7");
            libraryState.getItemCopies().put(copy.getId(), copy);
            libraryState.notifyInventoryChange(copy);

            assertThat(availabilityObserver.getAvailableCopiesCount(testBook.getId())).isEqualTo(1);

            copy.setStatus(ItemStatus.REMOVED);
            libraryState.notifyInventoryChange(copy);

            assertThat(availabilityObserver.getAvailableCopiesCount(testBook.getId())).isZero();
        }
    }
}
