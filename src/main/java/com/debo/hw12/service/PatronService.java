package com.debo.hw12.service;

import com.debo.hw12.exception.BusinessRuleException;
import com.debo.hw12.exception.DuplicateEntityException;
import com.debo.hw12.exception.ValidationException;
import com.debo.hw12.model.Patron;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PatronService {
    Patron registerPatron(String name, String email, String membershipType) throws DuplicateEntityException, ValidationException;
    Optional<Patron> getPatronById(UUID id);
    List<Patron> searchPatrons(String searchCriteria, String searchTerm);
    Patron updatePatron(UUID id, String name, String email) throws ValidationException, DuplicateEntityException;
    void deactivatePatron(UUID id) throws ValidationException, BusinessRuleException;
    boolean isPatronEligibleForBorrowing(UUID id);
}