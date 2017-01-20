package org.workcraft;

public class Version implements Comparable<Version> {

    public enum Status {
        ALPHA("alpha"),
        BETA("beta"),
        RC1("rc1"),
        RC2("rc2"),
        RC3("rc3"),
        RELEASE("");

        private final String name;

        Status(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    };

    public final int major;
    public final int minor;
    public final int revision;
    public final Status status;

    public Version(int major, int minor, int revision, Status status) {
        this.major = major;
        this.minor = minor;
        this.revision = revision;
        this.status = status;
    }

    @Override
    public String toString() {
        String result = major + "." + minor + "." + revision;
        if ((status != null) && (status != Status.RELEASE)) {
            result += " (" + status + ")";
        }
        return result;
    }

    @Override
    public int compareTo(Version o) {
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
        return status == null ? 0 : status.compareTo(o.status);
    }

}
