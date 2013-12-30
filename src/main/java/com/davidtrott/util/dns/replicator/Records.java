package com.davidtrott.util.dns.replicator;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.net.util.SubnetUtils.SubnetInfo;
import org.xbill.DNS.ARecord;
import org.xbill.DNS.DClass;
import org.xbill.DNS.Name;
import org.xbill.DNS.PTRRecord;
import org.xbill.DNS.Record;
import org.xbill.DNS.ReverseMap;
import org.xbill.DNS.Type;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Records<T extends Record> implements Iterable<T> {
    private final List<T> records;

    public Records(List<T> records) {
        this.records = records;
    }

    public Records<ARecord> filterSubnetARecords(SubnetInfo subnetInfo) {
        final List<ARecord> result = new ArrayList<>();

        for (Record record : records) {
            if (record.getType() == Type.A) {
                final ARecord aRecord = (ARecord) record;
                if (subnetInfo.isInRange(aRecord.getAddress().getHostAddress())) {
                    result.add(aRecord);
                }
            }
        }

        return new Records(result);
    }

    public Records<T> difference(Records<T> other) {
        return new Records(Lists.newArrayList(Sets.difference(
                Sets.newHashSet(this.records),
                Sets.newHashSet(other.records))));
    }

    public Records<PTRRecord> toPtrRecords() {
        final List<PTRRecord> result = new ArrayList<>();

        for (Record record : records) {
            if (record.getType() == Type.A) {
                final ARecord aRecord = (ARecord) record;
                final Name name = ReverseMap.fromAddress(aRecord.getAddress());
                final Name target = aRecord.getName();
                result.add(new PTRRecord(name, DClass.IN, record.getTTL(), target));
            }
        }

        return new Records<>(result);
    }

    @Override
    public Iterator<T> iterator() {
        return records.iterator();
    }
}
