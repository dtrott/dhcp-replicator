package com.davidtrott.util.dns.replicator;

public class FileWatcherException extends Exception {
    public FileWatcherException() {
    }

    public FileWatcherException(String message) {
        super(message);
    }

    public FileWatcherException(String message, Throwable cause) {
        super(message, cause);
    }

    public FileWatcherException(Throwable cause) {
        super(cause);
    }
}
