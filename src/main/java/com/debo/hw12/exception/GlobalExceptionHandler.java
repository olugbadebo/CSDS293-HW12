package com.debo.hw12.exception;

import com.debo.hw12.util.Logger;

public class GlobalExceptionHandler {
    private static final Logger logger = Logger.getInstance();
    private static GlobalExceptionHandler instance;

    private GlobalExceptionHandler() {}

    public static GlobalExceptionHandler getInstance() {
        if (instance == null) {
            synchronized (GlobalExceptionHandler.class) {
                if (instance == null) {
                    instance = new GlobalExceptionHandler();
                }
            }
        }
        return instance;
    }

    public void handleException(Throwable exception) {
        if (exception instanceof ValidationException) {
            handleValidationException((ValidationException) exception);
        } else {
            handleUnexpectedException(exception);
        }
    }

    private void handleValidationException(ValidationException exception) {
        logger.error("Item not found: " + exception.getMessage());
    }

    private void handleUnexpectedException(Throwable exception) {
        logger.error("Unexpected error occurred", exception);
    }
}
