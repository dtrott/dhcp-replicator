package com.davidtrott.util.dns.replicator;

public class DnsServerException extends Exception {
    public DnsServerException() {
    }

    public DnsServerException(String message) {
        super(message);
    }

    public DnsServerException(String message, Throwable cause) {
        super(message, cause);
    }

    public DnsServerException(Throwable cause) {
        super(cause);
    }
}
