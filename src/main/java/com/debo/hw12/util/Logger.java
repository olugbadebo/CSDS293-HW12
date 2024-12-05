package com.debo.hw12.util;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class Logger {
    private static final String LOG_DIR = "logs";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    private static final ConcurrentHashMap<String, BufferedWriter> writers = new ConcurrentHashMap<>();
    private static final ReentrantLock lock = new ReentrantLock();

    private static Logger instance;

    private Logger() {
        createLogDirectory();
    }

    public static Logger getInstance() {
        if (instance == null) {
            synchronized (Logger.class) {
                if (instance == null) {
                    instance = new Logger();
                }
            }
        }
        return instance;
    }

    private void createLogDirectory() {
        try {
            Files.createDirectories(Paths.get(LOG_DIR));
            String todayDir = getTodayDirectory();
            Files.createDirectories(Paths.get(todayDir));
        } catch (IOException e) {
            throw new RuntimeException("Failed to create log directories", e);
        }
    }

    private BufferedWriter getWriter(String level) throws IOException {
        String key = getTodayDirectory() + "/" + level;
        return writers.computeIfAbsent(key, k -> {
            try {
                return Files.newBufferedWriter(Paths.get(k),
                        StandardOpenOption.CREATE,
                        StandardOpenOption.APPEND);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }

    private String getTodayDirectory() {
        return LOG_DIR + "/" + LocalDateTime.now().format(DATE_FORMATTER);
    }

    private void log(String level, String message, Throwable throwable) {
        lock.lock();
        try {
            String timestamp = LocalDateTime.now().format(TIME_FORMATTER);
            BufferedWriter writer = getWriter(level + ".log");

            writer.write(String.format("[%s] %s: %s%n", timestamp, level, message));

            if (throwable != null) {
                writer.write("Exception: " + throwable.getMessage() + "\n");
                for (StackTraceElement element : throwable.getStackTrace()) {
                    writer.write("\tat " + element.toString() + "\n");
                }
            }

            writer.flush();
        } catch (IOException e) {
            System.err.println("Failed to write to log file: " + e.getMessage());
        } finally {
            lock.unlock();
        }
    }

    public void info(String message) {
        log("INFO", message, null);
    }

    public void debug(String message) {
        log("DEBUG", message, null);
    }

    public void warn(String message) {
        log("WARN", message, null);
    }

    public void error(String message) {
        log("ERROR", message, null);
    }

    public void error(String message, Throwable throwable) {
        log("ERROR", message, throwable);
    }

    public void closeLogFiles() {
        writers.values().forEach(writer -> {
            try {
                writer.close();
            } catch (IOException e) {
                System.err.println("Failed to close log writer: " + e.getMessage());
            }
        });
        writers.clear();
    }
}