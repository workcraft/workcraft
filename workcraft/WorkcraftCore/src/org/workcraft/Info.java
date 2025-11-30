package org.workcraft;

import org.workcraft.Version.Status;
import org.workcraft.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Info {

    private static final String TITLE = "Workcraft";
    private static final String SUBTITLE_1 = "A New Hope";
    private static final String SUBTITLE_2 = "Metastability Strikes Back";
    private static final String SUBTITLE_3 = "Return of the Hazard";
    private static final String SUBTITLE_4 = "Revenge of the Timing Assumption";

    private static final Version VERSION = new Version(3, 5, 4, Status.RELEASE);

    private static final int START_YEAR = 2006;
    private static final int CURRENT_YEAR = Calendar.getInstance().get(Calendar.YEAR);
    private static final String ORGANISATION = "Newcastle University";
    private static final String HOMEPAGE = "https://workcraft.org/";

    private static final Pattern EDITION_PATTERN = Pattern.compile("^WORKCRAFT_EDITION=\"(.+)\"$", Pattern.MULTILINE);
    private static final File RELEASE_FILE = new File("release");
    private static final String DEVELOPMENT_TEXT = "development mode";

    public static Version getVersion() {
        return VERSION;
    }

    public static String getTitle() {
        return TITLE + ' ' + VERSION.major;
    }

    public static String getSubtitle() {
        return switch (VERSION.major) {
            case 1 -> SUBTITLE_1;
            case 2 -> SUBTITLE_2;
            case 3 -> SUBTITLE_3;
            case 4 -> SUBTITLE_4;
            default -> "";
        };
    }

    public static boolean isReleaseMode() {
        return RELEASE_FILE.exists() && RELEASE_FILE.isFile();
    }

    public static boolean isDevelopmentMode() {
        return !isReleaseMode();
    }

    public static String getEdition() {
        if (isDevelopmentMode()) {
            return DEVELOPMENT_TEXT;
        }
        try {
            String text = FileUtils.readAllText(RELEASE_FILE);
            Matcher matcher = EDITION_PATTERN.matcher(text);
            if (matcher.find()) {
                return matcher.group(1);
            }
        } catch (IOException ignored) {
        }
        return "";
    }

    public static String getVersionAndEdition() {
        String edition = getEdition();
        String suffix = edition.isEmpty() ? "" : (" - " + edition);
        return getVersion() + suffix;
    }

    public static String getFullTitle() {
        return getTitle() + " (" + getSubtitle() + "), version " + getVersionAndEdition();
    }

    public static String getJavaDescription() {
        return "JVM " + System.getProperty("java.version") + " [" + System.getProperty("java.home") + "]";
    }

    public static String getCopyright() {
        return "Copyright " + START_YEAR + '-' + CURRENT_YEAR + ' ' + ORGANISATION;
    }

    public static String getHomepage() {
        return HOMEPAGE;
    }

}
