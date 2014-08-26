package com.davidtrott.util.dns.replicator;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.util.SubnetUtils;
import org.apache.commons.net.util.SubnetUtils.SubnetInfo;
import org.xbill.DNS.Name;
import org.xbill.DNS.TextParseException;

import java.io.File;
import java.util.Map;

public class Config {

    /**
     * Name or IP of DNS Server.
     */
    private String dnsServer;

    /**
     * CIDR (10.0.0/24) address of dhcp range.
     */
    private String dhcpRange;

    /**
     * Path to the file containing DHCP leases written by dnsmasq.
     */
    private String leaseFile;

    /**
     * The forward DNS zone (cloud.lan.)
     */
    private String forwardZone;

    /**
     * The reverse DNS zone (241.0.10.in-addr.arpa.)
     */
    private String reverseZone;

    private long dnsTtl;

    private SubnetInfo dhcpRangeInfo;
    private Name forwardZoneName;
    private Name reverseZoneName;

    public Config() {
    }

    public Config(Map<?, ?> properties) {
        this.dnsServer = (String) properties.get("dnsServer");
        this.dhcpRange = (String) properties.get("dhcpRange");
        this.leaseFile = (String) properties.get("leaseFile");
        this.forwardZone = (String) properties.get("forwardZone");
        this.reverseZone = (String) properties.get("reverseZone");
        this.dnsTtl = parseLong((String) properties.get("dnsTtl"), 0);
    }

    private static long parseLong(String value, long def) {
        try {
            if (StringUtils.isNotBlank(value)) {
                return Long.parseLong(value.trim());
            }
        } catch (NumberFormatException e) {
        }
        return def;
    }

    public String getDnsServer() {
        return dnsServer;
    }

    public void setDnsServer(String dnsServer) {
        this.dnsServer = dnsServer;
    }

    public String getDhcpRange() {
        return dhcpRange;
    }

    public void setDhcpRange(String dhcpRange) {
        this.dhcpRange = dhcpRange;
    }

    public String getLeaseFile() {
        return leaseFile;
    }

    public void setLeaseFile(String leaseFile) {
        this.leaseFile = leaseFile;
    }

    public String getForwardZone() {
        return forwardZone;
    }

    public void setForwardZone(String forwardZone) {
        this.forwardZone = forwardZone;
    }

    public String getReverseZone() {
        return reverseZone;
    }

    public void setReverseZone(String reverseZone) {
        this.reverseZone = reverseZone;
    }

    public long getDnsTtl() {
        return dnsTtl;
    }

    public void setDnsTtl(long dnsTtl) {
        this.dnsTtl = dnsTtl;
    }

    public SubnetInfo getDhcpRangeInfo() {
        return dhcpRangeInfo;
    }

    public Name getForwardZoneName() {
        return forwardZoneName;
    }

    public Name getReverseZoneName() {
        return reverseZoneName;
    }

    public ValidationResult validate() {
        final StringBuilder issues = new StringBuilder();
        boolean valid = true;

        if (StringUtils.isBlank(dnsServer)) {
            dnsServer = "127.0.0.1";
        }

        if (StringUtils.isBlank(dhcpRange)) {
            valid = false;
            issues.append("dhcpRange has not been set\n");
        }

        if (StringUtils.isBlank(leaseFile)) {
            leaseFile = "/var/lib/libvirt/dnsmasq/default.leases";
        }

        if (StringUtils.isBlank(forwardZone)) {
            valid = false;
            issues.append("forwardZone has not been set\n");
        }

        if (StringUtils.isBlank(reverseZone)) {
            valid = false;
            issues.append("dnsServer has not been set\n");
        }

        if (dnsTtl < 1) {
            dnsTtl = 60;
        }

        final SubnetUtils subnetUtils = new SubnetUtils(dhcpRange);
        subnetUtils.setInclusiveHostCount(true);
        dhcpRangeInfo = subnetUtils.getInfo();

        if (!new File(leaseFile).exists()) {
            valid = false;
            issues.append("Lease file not found: " + leaseFile + "\n");
        }

        try {
            forwardZoneName = new Name(forwardZone);
        } catch (TextParseException e) {
            valid = false;
            issues.append("Forward Zone is invalid: " + e.getMessage() + "\n");
        }
        try {
            reverseZoneName = new Name(reverseZone);
        } catch (TextParseException e) {
            valid = false;
            issues.append("Reverse Zone is invalid: " + e.getMessage() + "\n");
        }

        return new ValidationResult(valid, issues.toString());
    }

    public static class ValidationResult {
        private final boolean valid;
        private final String issues;

        public ValidationResult(boolean valid, String issues) {
            this.valid = valid;
            this.issues = issues;
        }

        public boolean isValid() {
            return valid;
        }

        public String getIssues() {
            return issues;
        }
    }
}
