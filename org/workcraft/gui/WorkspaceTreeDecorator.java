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
import javax.swing.JPopupMenu;

import org.workcraft.gui.workspace.Path;
import org.workcraft.gui.workspace.TreeDecorator;
import org.workcraft.gui.workspace.WorkspacePopupProvider;
import org.workcraft.gui.workspace.WorkspaceWindow;
import org.workcraft.workspace.Workspace;
import org.workcraft.workspace.WorkspaceEntry;

public class WorkspaceTreeDecorator implements TreeDecorator<Path<String>>
{
	private final WorkspacePopupProvider popups;
	private final Workspace workspace;

	public WorkspaceTreeDecorator(WorkspaceWindow window)
	{
		workspace = window.getWorkspace();
		popups = new WorkspacePopupProvider(window);
	}

	@Override
	public JPopupMenu getPopupMenu(Path<String> node) {
		return popups.getPopup(node);
	}

	@Override
	public Icon getIcon(Path<String> node) {

/*		try {
			return GUI.loadIconFromResource("images/place.png");
		} catch (IOException e) {
			System.out.println(e.getLocalizedMessage());
			return null;
		}*/
		return null;
	}

	@Override
	public String getName(Path<String> node) {
		final WorkspaceEntry openFile = workspace.getOpenFile(node);
		String name = node.isEmpty() ? "-- Workspace --" : node.getNode();
		if(openFile != null && openFile.isChanged())
			name += " *";
		return name;
	}
}
