package org.workcraft.utils;

import java.io.File;

public class ExecutableUtils {

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

}
