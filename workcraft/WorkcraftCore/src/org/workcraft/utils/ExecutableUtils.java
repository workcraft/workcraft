package org.workcraft.utils;

import java.io.File;

public class ExecutableUtils {

    public static String getAbsoluteCommandPath(String path) {
        File file = new File(path);
        return file.exists() ? file.getAbsolutePath() : file.getPath();
    }

}
