package org.workcraft.utils;

import java.io.File;

public class ExecutableUtils {

    private static final String EXE_EXTENSION = ".exe";

    public static String getAbsoluteBasePath() {
        return System.getProperty("user.dir");
    }

    public static String getBaseRelativePath(File file) {
        if (file == null) {
            return "";
        } else {
            String basePath = getAbsoluteBasePath();
            return FileUtils.stripBase(file.getPath(), basePath);
        }
    }

    public static File getBaseRelativeFile(String path) {
        return (path == null) || path.isEmpty() ? null : new File(path);
    }

    public static String getAbsoluteCommandPath(String toolName) {
        return getAbsoluteCommandPath(new File(toolName));
    }

    public static String getAbsoluteCommandPath(File file) {
        String result = null;
        if (file != null) {
            result = file.getPath();
            if (file.exists()) {
                result = FileUtils.getFullPath(file);
            }
        }
        return result;
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
