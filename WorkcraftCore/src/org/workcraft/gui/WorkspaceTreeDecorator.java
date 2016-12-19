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

import javax.swing.Icon;

import org.workcraft.gui.trees.TreeDecorator;
import org.workcraft.gui.workspace.Path;
import org.workcraft.workspace.Workspace;
import org.workcraft.workspace.WorkspaceEntry;

public class WorkspaceTreeDecorator implements TreeDecorator<Path<String>> {
    private final Workspace workspace;

    public WorkspaceTreeDecorator(Workspace workspace) {
        this.workspace = workspace;
    }

    @Override
    public Icon getIcon(Path<String> node) {
        return null;
    }

    @Override
    public String getName(Path<String> node) {
        final WorkspaceEntry openFile = workspace.getWork(node);
        String name = node.isEmpty() ? "!Workspace" : node.getNode();
        if (openFile != null && openFile.isChanged()) {
            name += " *";
        }
        return name;
    }
}
