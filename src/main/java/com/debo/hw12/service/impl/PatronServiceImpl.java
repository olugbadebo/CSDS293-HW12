package com.debo.hw12.service.impl;

import com.debo.hw12.enums.PatronType;
import com.debo.hw12.exception.BusinessRuleException;
import com.debo.hw12.exception.DuplicateEntityException;
import com.debo.hw12.exception.ValidationException;
import com.debo.hw12.model.Patron;
import com.debo.hw12.service.PatronService;
import com.debo.hw12.state.LibraryState;
import com.debo.hw12.util.Logger;

import java.time.LocalDateTime;
import java.util.*;

public class PatronServiceImpl implements PatronService {
    private final LibraryState libraryState = LibraryState.getInstance();
    private final Logger logger = Logger.getInstance();

    @Override
    public Patron registerPatron(String name, String email, String patronType) throws DuplicateEntityException, ValidationException {
        validatePatronData(name, email);

        Patron newPatron = Patron.builder()
                .id(UUID.randomUUID())
                .name(name)
                .email(validateUniqueEmail(email))
                .type(validatePatronType(patronType))
                .registrationDate(LocalDateTime.now())
                .active(true)
                .currentLoans(new HashSet<>())
                .loanHistory(new ArrayList<>())
                .build();

        libraryState.getPatrons().put(newPatron.getId(), newPatron);
        logger.info("Registered new patron: " + newPatron.getName());
        return newPatron;
    }

    private void validatePatronData(String name, String email) throws ValidationException {
        if (name == null || name.trim().isEmpty()) {
            throw new ValidationException("Name cannot be empty");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new ValidationException("Email cannot be empty");
        }
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new ValidationException("Invalid email format");
        }
    }

    private String validateUniqueEmail(String email) throws DuplicateEntityException {
        boolean emailExists = libraryState.getPatrons().values().stream()
                .filter(Patron::isActive)
                .anyMatch(patron -> patron.getEmail().equalsIgnoreCase(email));

        if (emailExists) {
            throw new DuplicateEntityException(String.format("Patron email %s already exists", email));
        }
        return email;
    }

    private PatronType validatePatronType(String patronType) throws ValidationException {
        try {
            return PatronType.valueOf(patronType.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Invalid patron type: %s".formatted(patronType));
        }
    }

    @Override
    public Optional<Patron> getPatronById(UUID id) {
        return Optional.ofNullable(libraryState.getPatrons().get(id))
                .filter(Patron::isActive);
    }

    @Override
    public List<Patron> searchPatrons(String searchCriteria, String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return List.of();
        }

        String normalizedTerm = searchTerm.toLowerCase().trim();

        return libraryState.getPatrons().values().stream()
                .filter(Patron::isActive)
                .filter(patron -> matchesCriteria(patron, searchCriteria, normalizedTerm))
                .toList();
    }

    private boolean matchesCriteria(Patron patron, String criteria, String searchTerm) {
        return switch (criteria.toLowerCase()) {
            case "name" -> patron.getName().toLowerCase().contains(searchTerm);
            case "email" -> patron.getEmail().toLowerCase().contains(searchTerm);
            case "type" -> patron.getType().name().toLowerCase().contains(searchTerm);
            default -> false;
        };
    }

    @Override
    public Patron updatePatron(UUID id, String name, String email) throws ValidationException, DuplicateEntityException {
        Patron patron = getPatronById(id)
                .orElseThrow(() -> new ValidationException("Patron not found with ID: " + id));

        validatePatronData(name, email);

        if (!email.equals(patron.getEmail())) {
            validateUniqueEmail(email);
        }

        patron.setName(name);
        patron.setEmail(email);

        logger.info("Updated patron: " + patron.getName());
        return patron;
    }

    @Override
    public void deactivatePatron(UUID id) throws ValidationException, BusinessRuleException {
        Patron patron = getPatronById(id)
                .orElseThrow(() -> new ValidationException("Patron not found with ID: " + id));

        if (!patron.getCurrentLoans().isEmpty()) {
            throw new BusinessRuleException("Cannot deactivate patron with active loans");
        }

        patron.setActive(false);
        logger.info("Deactivated patron: " + patron.getName());
    }

    @Override
    public boolean isPatronEligibleForBorrowing(UUID id) {
        return getPatronById(id)
                .map(patron -> {
                    boolean isActive = patron.isActive();
                    boolean hasCapacity = patron.getCurrentLoans().size() < patron.getType().getMaxItemLoanAmount();
                    return isActive && hasCapacity;
                })
                .orElse(false);
    }
}
