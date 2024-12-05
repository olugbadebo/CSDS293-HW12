package com.debo.hw12.observer.impl;

import com.debo.hw12.model.ItemCopy;
import com.debo.hw12.observer.InventoryObserver;
import com.debo.hw12.util.Logger;



public class InventoryObserverImpl implements InventoryObserver {
    @Override
    public void onInventoryChange(ItemCopy copy) {
        Logger.getInstance().info(String.format("Inventory change detected for copy ID: %s, Status: %s",
                copy.getId(), copy.getStatus()));
    }
}