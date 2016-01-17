package org.workcraft.util;

public class LogUtils {
	public static final String PREFIX_INFO = "[INFO] ";
	public static final String PREFIX_WARNING = "[WARNING] ";
	public static final String PREFIX_ERROR = "[ERROR] ";
	public static final String PREFIX_STDOUT = "[STDOUT] ";
	public static final String PREFIX_STDERR = "[STDERR] ";

	public static void logInfoLine(String msg) {
		System.out.println(PREFIX_INFO + msg);
	}

	public static void logWarningLine(String msg) {
		System.out.println(PREFIX_WARNING + msg);
	}

	public static void logErrorLine(String msg) {
		System.out.println(PREFIX_ERROR + msg);
	}

}
