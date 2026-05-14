package org.workcraft.utils;

import java.io.File;

public class ExecutableUtils {

    public static String getAbsoluteCommandPath(String path) {
        return getAbsoluteCommandPath(new File(path));
    }

    public static String getAbsoluteCommandPath(File file) {
        return file.exists() ? file.getAbsolutePath() : file.getPath();
    }

}
