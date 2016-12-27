package org.workcraft.util;

import java.io.File;

public class ToolUtils {

    private static final String EXE_EXTENSION = ".exe";

    public static String getAbsoluteCommandPath(String toolName) {
        File toolFile = new File(toolName);
        if (toolFile.exists()) {
            toolName = toolFile.getAbsolutePath();
        }
        return toolName;
    }

    public static String getAbsoluteCommandWithSuffixPath(String name, String suffix) {
        String toolName = getCommandWithSuffix(name, suffix);
        return getAbsoluteCommandPath(toolName);
    }

    public static String getCommandWithSuffix(String name, String suffix) {
        String extension = name.endsWith(EXE_EXTENSION) ? EXE_EXTENSION : "";
        String prefix = name.substring(0, name.length() - extension.length());
        return prefix + suffix + extension;
    }

}
