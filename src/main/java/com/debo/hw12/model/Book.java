package com.debo.hw12.model;

import com.debo.hw12.enums.Condition;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.util.HashSet;
import java.util.Set;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class Book extends LibraryItem {
    @EqualsAndHashCode.Include
    private String isbn;
    private String author;
    private String publisher;
    private int publicationYear;
    @Builder.Default
    private Set<String> genres = new HashSet<>();
    @Builder.Default
    private Condition condition = Condition.GOOD;

    public void addGenre(String genre) {
        if (genre == null || genre.trim().isEmpty()) {
            throw new IllegalArgumentException("Genre cannot be null or empty");
        }
        genres.add(genre.trim());
    }

    public void removeGenre(String genre) {
        if (genre == null || genre.trim().isEmpty()) {
            throw new IllegalArgumentException("Genre cannot be null or empty");
        }
        genres.remove(genre.trim());
    }

    public boolean hasGenre(String genre) {
        return genre != null && genres.contains(genre.trim());
    }

    public void updateCondition(Condition newCondition) {
        if (newCondition == null) {
            throw new IllegalArgumentException("Condition cannot be null");
        }
        this.condition = newCondition;
    }
}