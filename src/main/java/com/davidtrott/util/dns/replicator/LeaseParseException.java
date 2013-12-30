package com.davidtrott.util.dns.replicator;

public class LeaseParseException extends Exception {
    public LeaseParseException() {
    }

    public LeaseParseException(String message) {
        super(message);
    }

    public LeaseParseException(String message, Throwable cause) {
        super(message, cause);
    }

    public LeaseParseException(Throwable cause) {
        super(cause);
    }
}
