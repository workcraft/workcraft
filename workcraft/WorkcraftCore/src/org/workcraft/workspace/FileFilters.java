package org.workcraft.workspace;

import javax.swing.filechooser.FileFilter;
import java.io.File;

public final class FileFilters {

    public static final String DOCUMENT_EXTENSION = ".work";
    public static final String WORKSPACE_EXTENSION = ".works";

    private static class DocumentFilesFilter extends FileFilter {
        @Override
        public boolean accept(File file) {
            if (file.isDirectory()) {
                return true;
            }
            return isWorkFile(file);
        }

        @Override
        public String getDescription() {
            return "Workcraft documents (*" + DOCUMENT_EXTENSION + ")";
        }
    }

    private static final class WorkspaceFilesFilter extends FileFilter {

        @Override
        public boolean accept(File file) {
            if (file.isDirectory()) {
                return true;
            }
            return isWorkspaceFile(file);
        }

        @Override
        public String getDescription() {
            return "Workcraft workspace (*" + WORKSPACE_EXTENSION + ")";
        }
    }

    public static final FileFilter DOCUMENT_FILES = new DocumentFilesFilter();
    public static final FileFilter WORKSPACE_FILES = new WorkspaceFilesFilter();

    public static boolean isWorkFile(File file) {
        return (file != null) && isWorkPath(file.getName());
    }

    public static boolean isWorkPath(String path) {
        return (path != null) && path.endsWith(DOCUMENT_EXTENSION);
    }

    public static boolean isWorkspaceFile(File file) {
        return (file != null) && isWorkspacePath(file.getName());
    }

    public static boolean isWorkspacePath(String path) {
        return (path != null) && path.endsWith(WORKSPACE_EXTENSION);
    }

}
