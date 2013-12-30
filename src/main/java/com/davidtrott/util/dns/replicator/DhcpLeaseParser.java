package com.davidtrott.util.dns.replicator;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class DhcpLeaseParser {

    public DhcpLeases parse(final File file) throws LeaseParseException {
        try (final FileInputStream stream = FileUtils.openInputStream(file)) {
            return parse(stream);
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }

    public DhcpLeases parse(InputStream in) throws LeaseParseException {
        try {
            final List<String> lines = IOUtils.readLines(in);

            final List<DhcpLease> leases = new ArrayList<>();
            for (String line : lines) {
                leases.add(new DhcpLease(line));
            }

            return new DhcpLeases(leases);
        } catch (IOException e) {
            throw new LeaseParseException("Failed to parse", e);
        }
    }
}
