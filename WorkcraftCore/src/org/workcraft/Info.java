package org.workcraft;

import java.util.Calendar;

public class Info {

    private static final String title = "Workcraft";
    private static final String subtitle1 = "A New Hope";
    private static final String subtitle2 = "Metastability Strikes Back";
    private static final String subtitle3 = "Return of the Hazard";
    private static final String subtitle4 = "Revenge of the Timing Assumption";

    private static final int majorVersion = 3;
    private static final int minorVersion = 1;
    private static final int revisionVersion = 0;
    private static final String statusVersion = null; // "alpha", "beta", "rc1", null (for release)

    private static final int startYear = 2006;
    private static final int currentYear = Calendar.getInstance().get(Calendar.YEAR);
    private static final String organisation = "Newcastle University";
    private static final String homepage = "http://workcraft.org/";

    public static final String OPTION_DIR = "-dir:";
    public static final String OPTION_EXEC = "-exec:";
    public static final String OPTION_NOGUI = "-nogui";
    public static final String OPTION_VERSION = "-version";
    public static final String OPTION_HELP = "-help";
    private static final String help = "Usage:  java --classpath <CORE_AND_PLUGINS> org.workcraft.Console [OPTIONS]\n" +
            "    " + OPTION_DIR + "<PATH>\t - pass working directory\n" +
            "    " + OPTION_EXEC + "<SCRIPT> - JavaScript to execute on startup\n" +
            "    " + OPTION_NOGUI + "\t - run in console mode\n" +
            "    " + OPTION_VERSION + "\t - report the version information and exit\n" +
            "    " + OPTION_HELP + "\t - display this help message and exit\n";

    public static String getVersion() {
        String version = majorVersion + "." + minorVersion + "." + revisionVersion;
        if ((statusVersion != null) && !statusVersion.isEmpty()) {
            version += " (" + statusVersion + ")";
        }
        return version;
    }

    public static String getTitle() {
        return title + " " + majorVersion;
    }

    public static String getSubtitle() {
        switch (majorVersion) {
        case 1: return subtitle1;
        case 2: return subtitle2;
        case 3: return subtitle3;
        case 4: return subtitle4;
        default: return "";
        }
    }

    public static String getFullTitle() {
        return getTitle() + " (" + getSubtitle() + "), version " + getVersion();
    }

    public static String getCopyright() {
        return "Copyright " + startYear + "-" + currentYear + " " + organisation;
    }

    public static String getHomepage() {
        return homepage;
    }

    public static String getHelp() {
        return help;
    }

}
