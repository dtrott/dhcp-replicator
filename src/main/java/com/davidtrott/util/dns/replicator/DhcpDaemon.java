package com.davidtrott.util.dns.replicator;

import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.daemon.DaemonInitException;

public class DhcpDaemon implements Daemon {
    private String[] arguments;
    private DhcpReplicator dhcpReplicator;

    @Override
    public void init(DaemonContext daemonContext) throws DaemonInitException, Exception {
        arguments = daemonContext.getArguments();
    }

    @Override
    public void start() throws Exception {
        dhcpReplicator = new DhcpReplicator();
        dhcpReplicator.execute(arguments);
    }

    @Override
    public void stop() throws Exception {
        dhcpReplicator.shutdown();
    }

    @Override
    public void destroy() {
    }
}
