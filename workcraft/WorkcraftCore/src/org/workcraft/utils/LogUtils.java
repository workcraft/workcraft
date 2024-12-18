package org.workcraft.utils;

import org.workcraft.gui.properties.PropertyHelper;

import java.util.List;

public class LogUtils {
    private static final String PREFIX_INFO = "[INFO] ";
    private static final String WARNING_PREFIX = "[WARNING] ";
    private static final String ERROR_PREFIX = "[ERROR] ";
    private static final String STDOUT_PREFIX = "[STDOUT] ";
    private static final String STDERR_PREFIX = "[STDERR] ";

    public static void logMessage(String msg) {
        System.out.println(msg);
    }

    public static void logMessage(String message, List<String> details) {
        logMessage(message + mergeDetails(details));
    }

    public static void logInfo(String msg) {
        System.out.println(PREFIX_INFO + msg);
    }

    public static void logInfo(String message, List<String> details) {
        logInfo(message + mergeDetails(details));
    }

    public static boolean isInfoText(String text) {
        return (text != null) && text.startsWith(PREFIX_INFO);
    }

    public static void logWarning(String msg) {
        System.out.println(WARNING_PREFIX + msg);
    }

    public static void logWarning(String message, List<String> details) {
        logWarning(message + mergeDetails(details));
    }

    public static boolean isWarningText(String text) {
        return (text != null) && text.startsWith(WARNING_PREFIX);
    }

    public static void logError(String msg) {
        System.out.println(ERROR_PREFIX + msg);
    }

    public static void logError(String message, List<String> details) {
        logError(message + mergeDetails(details));
    }

    public static boolean isErrorText(String text) {
        return (text != null) && text.startsWith(ERROR_PREFIX);
    }

    public static void logStdout(String msg) {
        System.out.println(STDOUT_PREFIX + msg);
    }

    public static boolean isStdoutText(String text) {
        return (text != null) && text.startsWith(STDOUT_PREFIX);
    }

    public static void logStderr(String msg) {
        System.out.println(STDERR_PREFIX + msg);
    }

    public static boolean isStderrText(String text) {
        return (text != null) && text.startsWith(STDERR_PREFIX);
    }

    public static String getPrefix(String text) {
        if (isInfoText(text)) {
            return PREFIX_INFO;
        }
        if (isWarningText(text)) {
            return WARNING_PREFIX;
        }
        if (isErrorText(text)) {
            return ERROR_PREFIX;
        }
        if (isStdoutText(text)) {
            return STDOUT_PREFIX;
        }
        if (isStderrText(text)) {
            return STDERR_PREFIX;
        }
        return "";
    }

    public static String getTextWithoutPrefix(String text) {
        return text == null ? null : text.substring(getPrefix(text).length());
    }

    private static String mergeDetails(List<String> details) {
        StringBuilder result = new StringBuilder();
        if ((details != null) && !details.isEmpty()) {
            if (details.size() == 1) {
                result.append(' ').append(details.iterator().next());
            } else {
                result.append(':');
                for (String detail : details) {
                    result.append('\n').append(PropertyHelper.BULLET_PREFIX).append(detail);
                }
            }
        }
        return result.toString();
    }

}
