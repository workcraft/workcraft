package org.workcraft;

import java.util.Calendar;

import org.workcraft.Version.Status;

public class Info {

    private static final String title = "Workcraft";
    private static final String subtitle1 = "A New Hope";
    private static final String subtitle2 = "Metastability Strikes Back";
    private static final String subtitle3 = "Return of the Hazard";
    private static final String subtitle4 = "Revenge of the Timing Assumption";

    private static final Version version = new Version(3, 2, 0, Status.ALPHA);

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

    public static Version getVersion() {
        return version;
    }

    public static String getVersionMajor() {
        return String.valueOf(getVersion().major);
    }

    public static String getVersionMinor() {
        return String.valueOf(getVersion().minor);
    }

    public static String getVersionRevision() {
        return String.valueOf(getVersion().revision);
    }

    public static String getVersionStatus() {
        return getVersion().status.toString();
    }

    public static String getTitle() {
        return title + " " + version.major;
    }

    public static String getSubtitle() {
        switch (version.major) {
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
