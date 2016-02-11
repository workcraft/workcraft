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

package org.workcraft.util;

import java.io.File;

public class ToolUtils {

    private static final String EXE_EXTENSION = ".exe";

    public static String getAbsoluteCommandPath(String toolName) {
        File toolFile = new File(toolName);
        if (toolFile.exists()) {
            toolName = toolFile.getAbsolutePath();
        }
        return toolName;
    }

    public static String getAbsoluteCommandWithSuffixPath(String name, String suffix) {
        String toolName = getCommandWithSuffix(name, suffix);
        return getAbsoluteCommandPath(toolName);
    }

    public static String getCommandWithSuffix(String name, String suffix) {
        String extension = name.endsWith(EXE_EXTENSION) ? EXE_EXTENSION : "";
        String prefix = name.substring(0, name.length() - extension.length());
        return prefix + suffix + extension;
    }

}
