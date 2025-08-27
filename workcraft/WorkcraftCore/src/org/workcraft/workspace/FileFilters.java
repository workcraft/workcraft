package org.workcraft.workspace;

import org.workcraft.interop.Importer;

import javax.swing.filechooser.FileFilter;
import java.io.File;

public final class FileFilters {

    public static final String DOCUMENT_EXTENSION = ".work";
    public static final String WORKSPACE_EXTENSION = ".works";

    private static final class DocumentFilesFilter extends FileFilter {
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

    private static boolean isWorkspaceFile(File file) {
        return (file != null) && file.getName().endsWith(WORKSPACE_EXTENSION);
    }

    public static File addWorkExtensionIfMissing(File file) {
        return addExtensionIfMissing(file, DOCUMENT_EXTENSION);
    }

    public static File addImporterExtensionIfMissing(File file, Importer importer) {
        return addExtensionIfMissing(file, importer.getFormat().getExtension());
    }

    private static File addExtensionIfMissing(File file, String extension) {
        return (file == null) ? null : new File(addExtensionIfMissing(file.getPath(), extension));
    }

    private static String addExtensionIfMissing(String path, String extension) {
        return ((path == null) || (extension == null) || path.endsWith(extension)) ? path : (path + extension);
    }

}
