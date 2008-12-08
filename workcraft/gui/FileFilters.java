package org.workcraft.gui;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class FileFilters {
	public static final String DOCUMENT_EXTENSION = ".work";
	public static final String WORKSPACE_EXTENSION = ".works";

	private static class DocumentFilesFilter extends FileFilter {

		@Override
		public boolean accept(File f) {
			if (f.isDirectory())
				return true;
			if (f.getName().endsWith(DOCUMENT_EXTENSION))
				return true;
			return false;
		}

		@Override
		public String getDescription() {
			return "Workcraft documents (*"+DOCUMENT_EXTENSION+")";
		}
	}

	private static class WorkspaceFilesFilter extends FileFilter {

		@Override
		public boolean accept(File f) {
			if (f.isDirectory())
				return true;
			if (f.getName().endsWith(WORKSPACE_EXTENSION))
				return true;
			return false;
		}

		@Override
		public String getDescription() {
			return "Workcraft workspace (*"+WORKSPACE_EXTENSION+")";
		}
	}

	public static final FileFilter DOCUMENT_FILES = new DocumentFilesFilter();
	public static final FileFilter WORKSPACE_FILES = new WorkspaceFilesFilter();

	public static String checkSaveExtension(String path, String ext) {
		return path.endsWith(ext)?path:path+ext;
	}

}
