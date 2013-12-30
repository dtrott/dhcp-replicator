package com.davidtrott.util.dns.replicator;

import org.apache.commons.net.util.SubnetUtils.SubnetInfo;
import org.xbill.DNS.ARecord;
import org.xbill.DNS.Name;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DhcpLeases {
    private final List<DhcpLease> leases;

    public DhcpLeases(List<DhcpLease> leases) {
        this.leases = leases;
    }

    public DhcpLeases filterSubnet(SubnetInfo subnetInfo) {
         final List<DhcpLease> result = new ArrayList<>();

         for (final DhcpLease lease : leases) {
             if (subnetInfo.isInRange(lease.getIp())) {
                 result.add(lease);
             }
         }
         return new DhcpLeases(result);
     }

    public Records<ARecord> toARecords(Name domain, long ttl){
        final List<ARecord> records = new ArrayList<>();

        for(final DhcpLease lease : leases) {
            records.add(lease.toARecord(domain, ttl));
        }

        return new Records(records);
    }


    public Map<String, DhcpLease> mapByHostname(Iterable<DhcpLease> leases) {
        Map<String, DhcpLease> map = new HashMap<>();
        for (final DhcpLease lease : leases) {
            map.put(lease.getHostname(), lease);
        }

        return map;
    }
}
