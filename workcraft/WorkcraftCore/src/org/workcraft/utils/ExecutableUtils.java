package org.workcraft.utils;

import java.io.File;
import java.io.IOException;

public class ExecutableUtils {

    public static String getAbsoluteCommandPath(String toolName) {
        File toolFile = new File(toolName);
        if (toolFile.exists()) {
            try {
                return toolFile.getCanonicalPath();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return toolFile.getPath();
    }

}
