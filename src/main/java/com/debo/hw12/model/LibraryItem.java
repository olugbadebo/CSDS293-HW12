package com.debo.hw12.model;

import com.debo.hw12.enums.ItemStatus;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@SuperBuilder
public abstract class LibraryItem {
    private final UUID id;
    private String title;
    private String description;
    @Builder.Default
    private LocalDateTime acquisitionDate = LocalDateTime.now();
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<ItemCopy> copies;
    private boolean active;

    protected LibraryItem(String title) {
        this.id = UUID.randomUUID();
        this.title = title;
        this.acquisitionDate = LocalDateTime.now();
        this.copies = new ArrayList<>();
        this.active = true;
    }

    public void addCopy(ItemCopy copy) {
        copies.add(copy);
    }

    public int getAvailableCopiesCount() {
        return (int) copies.stream()
                .filter(copy -> copy.getStatus() == ItemStatus.AVAILABLE)
                .count();
    }

    public int getTotalCopiesCount() {
        return copies.size();
    }
}
