package org.workcraft;

import org.workcraft.utils.EnumUtils;

public class Version implements Comparable<Version> {

    public enum Status {
        ALPHA("alpha"),
        BETA("beta"),
        RC1("rc1"),
        RC2("rc2"),
        RC3("rc3"),
        RC4("rc4"),
        RC5("rc5"),
        RC6("rc6"),
        RC7("rc7"),
        RC8("rc8"),
        RC9("rc9"),
        RELEASE("");

        private final String name;

        Status(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private static final int HASH_MAJOR = 1000;
    private static final int HASH_MINOR = 100;
    private static final int HASH_REVISION = 10;
    private static final int HASH_STATUS = 1;

    public final int major;
    public final int minor;
    public final int revision;
    public final Status status;

    public Version(String major, String minor, String revision, String status) {
        this(Integer.parseInt(major), Integer.parseInt(minor), Integer.parseInt(revision),
                EnumUtils.itemFromString(status, Version.Status.class));
    }

    public Version(int major, int minor, int revision, Status status) {
        this.major = major;
        this.minor = minor;
        this.revision = revision;
        this.status = status;
    }

    @Override
    public String toString() {
        String result = major + "." + minor + "." + revision;
        if (status != Status.RELEASE) {
            result += " (" + status + ")";
        }
        return result;
    }

    @Override
    public int compareTo(Version o) {
        if (o == this) {
            return 0;
        }
        if (major < o.major) {
            return -1;
        }
        if (major > o.major) {
            return 1;
        }
        if (minor < o.minor) {
            return -1;
        }
        if (minor > o.minor) {
            return 1;
        }
        if (revision < o.revision) {
            return -1;
        }
        if (revision > o.revision) {
            return 1;
        }
        return status.compareTo(o.status);
    }

    @Override
    public int hashCode() {
        return HASH_MAJOR * major + HASH_MINOR * minor + HASH_REVISION * revision +  HASH_STATUS * status.ordinal();
    }

    @Override
    public boolean equals(Object o) {
        boolean result = false;
        if (o instanceof Version v) {
            result = compareTo(v) == 0;
        }
        return result;
    }

}
