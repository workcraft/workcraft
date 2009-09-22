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

package org.workcraft.gui.graph;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

public class GraphEditorFocusListener implements FocusListener {
	private GraphEditorPanel editor;

	public GraphEditorFocusListener(GraphEditorPanel editor) {
		this.editor = editor;
	}

	public void focusGained(FocusEvent e) {
		editor.focusGained();
	}

	public void focusLost(FocusEvent e) {
		editor.focusLost();
	}
}