package com.debo.hw12.observer.impl;

import com.debo.hw12.enums.ItemStatus;
import com.debo.hw12.model.ItemCopy;
import com.debo.hw12.observer.InventoryObserver;
import com.debo.hw12.state.LibraryState;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AvailabilityObserver implements InventoryObserver {
    private final Map<UUID, Long> availableCopiesByBook = new ConcurrentHashMap<>();

    @Override
    public void onInventoryChange(ItemCopy copy) {
        UUID bookId = copy.getItem().getId();
        updateAvailabilityCount(bookId);
    }

    private void updateAvailabilityCount(UUID bookId) {
        long count = LibraryState.getInstance()
                .getItemCopies().values().stream()
                .filter(copy -> copy.getItem().getId().equals(bookId))
                .filter(copy -> copy.getStatus() == ItemStatus.AVAILABLE)
                .count();
        availableCopiesByBook.put(bookId, count);
    }

    public long getAvailableCopiesCount(UUID bookId) {
        return availableCopiesByBook.getOrDefault(bookId, 0L);
    }
}