package com.davidtrott.util.dns.replicator;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

@Test
public class DhcpLeaseTest {
    @Test
    public void Construct_LeaseLine_ValidFields() {
        final DhcpLease lease = new DhcpLease("1388312449 52:54:00:dc:7e:fd 10.0.241.134 rabbitmq *");

        assertEquals(lease.getTimeStamp(), "1388312449");
        assertEquals(lease.getMacAddress(), "52:54:00:dc:7e:fd");
        assertEquals(lease.getIp(), "10.0.241.134");
        assertEquals(lease.getHostname(), "rabbitmq");
    }
}
