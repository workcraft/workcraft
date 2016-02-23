package org.workcraft;

import java.util.Calendar;

public class Info {

    private static final String title = "Workcraft";
    private static final String subtitle1 = "A New Hope";
    private static final String subtitle2 = "Metastability Strikes Back";
    private static final String subtitle3 = "Return of the Hazard";
    private static final String subtitle4 = "Revenge of the Timing Assumption";

    private static final int majorVersion = 3;
    private static final int minorVersion = 0;
    private static final int revisionVersion = 9;
    private static final String statusVersion = null; // "alpha", "beta", "rc1", null (for release)

    private static final int startYear = 2006;
    private static final int currentYear = Calendar.getInstance().get(Calendar.YEAR);
    private static final String organisation = "Newcastle University";
    private static final String homepage = "http://workcraft.org/";

    static public String getVersion() {
        String version = majorVersion + "." + minorVersion + "." + revisionVersion;
        if ((statusVersion != null) && !statusVersion.isEmpty()) {
            version += " (" + statusVersion + ")";
        }
        return version;
    }

    static public String getTitle() {
        return title + " " + majorVersion;
    }

    static public String getSubtitle() {
        switch (majorVersion) {
        case 1: return subtitle1;
        case 2: return subtitle2;
        case 3: return subtitle3;
        case 4: return subtitle4;
        default: return "";
        }
    }

    static public String getFullTitle() {
        return getTitle() + " (" + getSubtitle() + "), version " + getVersion();
    }

    static public String getCopyright() {
        return "Copyright " + startYear + "-" + currentYear + " " + organisation;
    }

    public static String getHomepage() {
        return homepage;
    }

}
