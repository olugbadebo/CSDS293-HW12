package com.debo.hw12.model;

import com.debo.hw12.enums.Condition;
import com.debo.hw12.enums.ItemStatus;
import com.debo.hw12.enums.ItemType;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
public class ItemCopy {
    private final UUID id;
    private final LibraryItem item;
    private String barcode;
    private String location;
    private ItemStatus status;
    private LocalDateTime acquisitionDate;
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<LoanRecord> loanHistory;
    private Condition condition;
    private String notes;
    private ItemType type;

    public ItemCopy(LibraryItem item, String barcode, String location) {
        this.id = UUID.randomUUID();
        this.item = item;
        this.barcode = barcode;
        this.location = location;
        this.status = ItemStatus.AVAILABLE;
        this.acquisitionDate = LocalDateTime.now();
        this.loanHistory = new ArrayList<>();
        this.condition = Condition.GOOD;
    }

    public boolean isAvailable() {
        return this.status == ItemStatus.AVAILABLE;
    }
}
