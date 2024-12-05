package com.debo.hw12.observer;

import com.debo.hw12.model.ItemCopy;

public interface InventoryObserver {
    void onInventoryChange(ItemCopy copy);
}

