/*
*
* Copyright 2008,2009 Newcastle University
*
* This file is part of Workcraft.
*
* Workcraft is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* Workcraft is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with Workcraft.  If not, see <http://www.gnu.org/licenses/>.
*
*/

package org.workcraft.gui;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class FileFilters {
    public static final String DOCUMENT_EXTENSION = ".work";
    public static final String WORKSPACE_EXTENSION = ".works";

    private static class DocumentFilesFilter extends FileFilter {
        @Override
        public boolean accept(File f) {
            if (f.isDirectory()) {
                return true;
            }
            if (f.getName().endsWith(DOCUMENT_EXTENSION)) {
                return true;
            }
            return false;
        }

        @Override
        public String getDescription() {
            return "Workcraft documents (*" + DOCUMENT_EXTENSION + ")";
        }
    }

    private static class WorkspaceFilesFilter extends FileFilter {

        @Override
        public boolean accept(File f) {
            if (f.isDirectory()) {
                return true;
            }
            if (f.getName().endsWith(WORKSPACE_EXTENSION)) {
                return true;
            }
            return false;
        }

        @Override
        public String getDescription() {
            return "Workcraft workspace (*" + WORKSPACE_EXTENSION + ")";
        }
    }

    public static final FileFilter DOCUMENT_FILES = new DocumentFilesFilter();
    public static final FileFilter WORKSPACE_FILES = new WorkspaceFilesFilter();

    public static String addExtension(String path, String ext) {
        return path.endsWith(ext) ? path : path + ext;
    }

    public static class GenericFileFilter extends FileFilter {
        private String extension;
        private String description;

        public GenericFileFilter(String extension, String description) {
            this.extension = extension;
            this.description = description;
        }

        public boolean accept(File f) {
            if (f.isDirectory()) {
                return true;
            }
            if (f.getName().endsWith(extension)) {
                return true;
            }
            return false;
        }

        public String getDescription() {
            return description;
        }
    }
}
