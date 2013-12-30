package com.davidtrott.util.dns.replicator;

import org.apache.commons.lang3.StringUtils;
import org.xbill.DNS.ARecord;
import org.xbill.DNS.DClass;
import org.xbill.DNS.Name;
import org.xbill.DNS.TextParseException;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class DhcpLease {
    private final String timeStamp;
    private final String macAddress;
    private final String ip;
    private final String hostname;

    public DhcpLease(String line) {
        final String[] split = StringUtils.split(line, ' ');
        this.timeStamp = split[0];
        this.macAddress = split[1];
        this.ip = split[2];
        this.hostname = split[3];
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public String getIp() {
        return ip;
    }

    public String getHostname() {
        return hostname;
    }

    public ARecord toARecord(Name domain, long ttl) {
        try {
            return new ARecord(new Name(hostname, domain), DClass.IN, ttl, InetAddress.getByName(ip));
        } catch (TextParseException | UnknownHostException e) {
            throw new RuntimeException("Failed to convert lease into ARecord: " + hostname, e);
        }
    }
}
