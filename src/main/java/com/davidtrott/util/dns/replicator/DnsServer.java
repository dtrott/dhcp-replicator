package com.davidtrott.util.dns.replicator;

import org.xbill.DNS.Message;
import org.xbill.DNS.Name;
import org.xbill.DNS.Record;
import org.xbill.DNS.Resolver;
import org.xbill.DNS.SimpleResolver;
import org.xbill.DNS.Update;
import org.xbill.DNS.ZoneTransferException;
import org.xbill.DNS.ZoneTransferIn;

import java.io.IOException;
import java.net.UnknownHostException;

public class DnsServer {
    private final String serverAddress;
    private final Resolver resolver;

    public DnsServer(String serverAddress) throws DnsServerException {
        try {
            this.serverAddress = serverAddress;
            this.resolver = new SimpleResolver(serverAddress);
            resolver.setTCP(true);
        } catch (UnknownHostException e) {
            throw new DnsServerException("Failed to resolve DNS Server", e);
        }
    }

    @SuppressWarnings("unchecked")
    public Records<Record> transferZone(Name zoneName) throws DnsServerException {
        try {
            final ZoneTransferIn xfr = ZoneTransferIn.newAXFR(zoneName, serverAddress, null);
            return new Records<Record>(xfr.run());
        } catch (IOException | ZoneTransferException e) {
            throw new DnsServerException("Failed to transfer Zone: " + zoneName, e);
        }
    }

    public void update(Name zone, Record record) throws DnsServerException {
        try {
            final Update update = new Update(zone);
            update.replace(record);
            Message response = resolver.send(update);
        } catch (IOException e) {
            throw new DnsServerException("Record update failed", e);
        }
    }

    public void update(Name zone, Records<?> records) throws DnsServerException {
        try {
            final Update update = new Update(zone);
            for (Record record : records) {
                update.replace(record);
            }
            Message response = resolver.send(update);
        } catch (IOException e) {
            throw new DnsServerException("Records update failed", e);
        }
    }

    public void delete(Name zone, Record record) throws DnsServerException {
        try {
            final Update update = new Update(zone);
            update.delete(record);
            Message response = resolver.send(update);
        } catch (IOException e) {
            throw new DnsServerException("Record delete failed", e);
        }
    }

    public void delete(Name zone, Records<?> records) throws DnsServerException {
        try {
            final Update update = new Update(zone);
            for (Record record : records) {
                update.delete(record);
            }
            Message response = resolver.send(update);
        } catch (IOException e) {
            throw new DnsServerException("Records delete failed", e);
        }
    }
}
