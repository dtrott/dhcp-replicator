package com.davidtrott.util.dns.replicator;

import org.apache.commons.io.FileUtils;
import org.apache.commons.net.util.SubnetUtils.SubnetInfo;
import org.xbill.DNS.ARecord;
import org.xbill.DNS.Name;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class DhcpReplicator {
    private static final String CONFIG_FILE = "dhcp-replicator.properties";
    private Thread watcherThread;

    public static void main(final String[] args) throws Exception {
        new DhcpReplicator().execute(args);
    }

    public void execute(String[] args) {

        final Config config = buildConfig(args);

        if (config != null) {
            run(config);
        }
    }

    private Config buildConfig(String[] args) {
        final Properties properties;
        try {
            properties = loadProperties(args);
        } catch (IOException e) {
            System.err.println("Unable to load properties");
            return null;
        }

        if (properties == null) {
            System.err.println("Properties file not found");
            return null;
        }

        final Config config = new Config(properties);
        final Config.ValidationResult validationResult = config.validate();

        if (!validationResult.isValid()) {
            System.err.println("Configuration is not valid:\n" + validationResult.getIssues());
            return null;
        }
        return config;
    }

    private Properties loadProperties(String[] args) throws IOException {
        if (args.length == 1) {
            try (final FileInputStream stream = FileUtils.openInputStream(new File(args[0]))) {
                return loadProperties(stream);
            }
        } else {
            try (final InputStream stream = getClass().getClassLoader().getResourceAsStream(CONFIG_FILE)) {
                return loadProperties(stream);
            }
        }
    }

    private Properties loadProperties(final InputStream stream) throws IOException {
        if (stream == null) {
            return null;
        }

        final Properties properties = new Properties();
        properties.load(stream);
        return properties;
    }

    private void run(Config config) {
        try {
            final DnsServer dnsServer = new DnsServer(config.getDnsServer());

            final SubnetInfo dhcpRange = config.getDhcpRangeInfo();
            final File leaseFile = new File(config.getLeaseFile());

            final Name forwardZone = config.getForwardZoneName();
            final Name reverseZone = config.getReverseZoneName();
            final long ttl = config.getDnsTtl();

            final FileWatcher watcher = new FileWatcher(leaseFile);

            final FileWatcher.Callback callback = new FileWatcher.Callback() {
                @Override
                public void changed() {
                    update(dnsServer, dhcpRange, leaseFile, forwardZone, reverseZone, ttl);
                }
            };

            final Runnable watchRunnable = new Runnable() {
                @Override
                public void run() {
                    // Execute one delta on startup.
                    callback.changed();

                    try {
                        watcher.watch(callback);
                    } catch (FileWatcherException e) {
                        e.printStackTrace();
                    }
                }
            };

            watcherThread = new Thread(watchRunnable);
            watcherThread.start();
        } catch (DnsServerException | FileWatcherException e) {
            e.printStackTrace();
        }
    }

    private void update(DnsServer dnsServer, SubnetInfo dhcpRange, File leaseFile, Name forwardZone, Name reverseZone, long ttl) {
        try {
            final Records<ARecord> leases = new DhcpLeaseParser().parse(leaseFile).filterSubnet(dhcpRange).toARecords(forwardZone, ttl);
            final Records<ARecord> records = dnsServer.transferZone(forwardZone).filterSubnetARecords(dhcpRange);

            final Records<ARecord> additions = leases.difference(records);
            final Records<ARecord> deletions = records.difference(leases);

            dnsServer.delete(forwardZone, deletions);
            dnsServer.delete(reverseZone, deletions.toPtrRecords());

            dnsServer.update(forwardZone, additions);
            dnsServer.update(reverseZone, additions.toPtrRecords());
        } catch (LeaseParseException | DnsServerException e) {
            e.printStackTrace();
        }
    }

    public void shutdown() {
        watcherThread.interrupt();
    }
}
