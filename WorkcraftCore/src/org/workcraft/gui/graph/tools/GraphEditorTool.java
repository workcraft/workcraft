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

package org.workcraft.gui.graph.tools;

import java.awt.Graphics2D;

import javax.swing.JPanel;

public interface GraphEditorTool extends Tool, GraphEditorKeyListener, GraphEditorMouseListener {
    void activated(GraphEditor editor);
    void deactivated(GraphEditor editor);
    void reactivated(GraphEditor editor);
    void setup(GraphEditor editor);

    void drawInUserSpace(GraphEditor editor, Graphics2D g);
    void drawInScreenSpace(GraphEditor editor, Graphics2D g);
    Decorator getDecorator(GraphEditor editor);

    void createInterfacePanel(GraphEditor editor);
    JPanel getInterfacePanel();

    void flashIssue(GraphEditor editor, String message);
    void showIssue(GraphEditor editor, String message);
    void hideIssue(GraphEditor editor);
}
