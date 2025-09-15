package org.workcraft.utils;

import java.io.File;

public class BackendUtils {

    private static final String TEMPLATE_DIR_PATH = "dist/template/";
    private static final String LINUX_DIR_PATH = "linux/";
    private static final String MACOS_DIR_PATH = "osx/Contents/Resources/";
    private static final String WINDOWS_DIR_PATH = "windows/";

    private static final String TOOLS_DIR_NAME = "tools";
    private static final String LIBRARIES_DIR_NAME = "libraries";
    private static final String COMPONENTS_DIR_NAME = "components";
    private static final String EXE_EXTENSION = ".exe";

    public static String getBaseRelativePath(File file) {
        return file == null ? "" : FileUtils.stripBase(file.getPath(), System.getProperty("user.dir"));
    }

    public static File getBaseRelativeFile(String path) {
        return (path == null) || path.isEmpty() ? null : new File(path);
    }

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
        return getTemplateLibraryPath(fileName, TEMPLATE_DIR_PATH);
    }

    public static String getTemplateLibraryPath(String fileName, String templateDirPath) {
        return switch (DesktopApi.getOs()) {
            case LINUX -> templateDirPath + LINUX_DIR_PATH + getLibraryPath(fileName);
            case MACOS -> templateDirPath + MACOS_DIR_PATH + getLibraryPath(fileName);
            case WINDOWS -> templateDirPath + WINDOWS_DIR_PATH + getLibraryPath(fileName);
            default -> getLibraryPath(fileName);
        };
    }

    public static String getTemplateToolPath(String dirName, String fileName) {
        return getTemplateToolPath(dirName, fileName, TEMPLATE_DIR_PATH);
    }

    public static String getTemplateToolPath(String dirName, String fileName, String templateDirPath) {
        return switch (DesktopApi.getOs()) {
            case LINUX -> templateDirPath + LINUX_DIR_PATH + getToolPath(dirName, fileName);
            case MACOS -> templateDirPath + MACOS_DIR_PATH + getToolPath(dirName, fileName);
            case WINDOWS -> templateDirPath + WINDOWS_DIR_PATH + getToolPath(dirName, fileName);
            default -> getToolPath(dirName, fileName);
        };
    }

    public static String getComponentPath(String dirName, String fileName) {
        return COMPONENTS_DIR_NAME + File.separator + dirName + File.separator + fileName;
    }

}
