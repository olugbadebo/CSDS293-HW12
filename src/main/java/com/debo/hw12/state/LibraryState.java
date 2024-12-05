package com.debo.hw12.state;

import com.debo.hw12.model.*;
import com.debo.hw12.observer.InventoryObserver;
import lombok.Getter;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class LibraryState implements Serializable {
    private static LibraryState instance;
    private static final String STATE_FILE = "library_state.ser";

    @Getter
    private final Map<UUID, Book> books = new ConcurrentHashMap<>();
    @Getter
    private final Map<UUID, ItemCopy> itemCopies = new ConcurrentHashMap<>();
    @Getter
    private final Map<UUID, Patron> patrons = new ConcurrentHashMap<>();
    @Getter
    private final Map<UUID, LoanRecord> loans = new ConcurrentHashMap<>();
    @Getter
    private final Map<UUID, Reservation> reservations = new ConcurrentHashMap<>();

    private final List<InventoryObserver> observers = new ArrayList<>();

    private LibraryState() {}

    public static synchronized LibraryState getInstance() {
        if (instance == null) {
            instance = loadState();
        }
        return instance;
    }

    public void addObserver(InventoryObserver observer) {
        observers.add(observer);
    }

    public void notifyInventoryChange(ItemCopy copy) {
        observers.forEach(o -> o.onInventoryChange(copy));
    }

    public void saveState() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(STATE_FILE))) {
            oos.writeObject(this);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save library state", e);
        }
    }

    private static LibraryState loadState() {
        File file = new File(STATE_FILE);
        if (!file.exists()) {
            return new LibraryState();
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (LibraryState) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            return new LibraryState();
        }
    }
}