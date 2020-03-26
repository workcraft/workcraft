package org.workcraft.utils;

import java.io.File;

public class BackendUtils {

    private static final String LINUX_TEMPLATE_PATH = "dist/template/linux/";
    private static final String MACOS_TEMPLATE_PATH = "dist/template/osx/Contents/Resources/";
    private static final String WINDOWS_TEMPLATE_PATH = "dist\\template\\windows\\";

    private static final String TOOLS_DIR_NAME = "tools";
    private static final String LIBRARIES_DIR_NAME = "libraries";
    private static final String EXE_EXTENSION = ".exe";

    public static String getLibraryPath(String fileName) {
        return LIBRARIES_DIR_NAME + File.separator + fileName;
    }

    public static String getToolPath(String dirName, String fileName) {
        return getToolDirectory(dirName) + fileName + (DesktopApi.getOs().isWindows() ? EXE_EXTENSION : "");
    }

    public static String getToolDirectory(String dirName) {
        return TOOLS_DIR_NAME + File.separator + dirName + File.separator;
    }

    public static String getTemplateLibraryPath(String fileName) {
        switch (DesktopApi.getOs()) {
        case LINUX:
            return LINUX_TEMPLATE_PATH + getLibraryPath(fileName);
        case MACOS:
            return MACOS_TEMPLATE_PATH + getLibraryPath(fileName);
        case WINDOWS:
            return WINDOWS_TEMPLATE_PATH + getLibraryPath(fileName);
        }
        return getLibraryPath(fileName);
    }

    public static String getTemplateToolPath(String dirName, String fileName) {
        switch (DesktopApi.getOs()) {
        case LINUX:
            return LINUX_TEMPLATE_PATH + getToolPath(dirName, fileName);
        case MACOS:
            return MACOS_TEMPLATE_PATH + getToolPath(dirName, fileName);
        case WINDOWS:
            return WINDOWS_TEMPLATE_PATH + getToolPath(dirName, fileName);
        }
        return getToolPath(dirName, fileName);
    }

}
