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

import javax.swing.Icon;
import javax.swing.JPanel;

public interface GraphEditorTool extends GraphEditorKeyListener, GraphEditorMouseListener {
    void activated(final GraphEditor editor);
    void deactivated(final GraphEditor editor);
    void reactivated(final GraphEditor editor);

    void drawInUserSpace(final GraphEditor editor, Graphics2D g);
    void drawInScreenSpace(final GraphEditor editor, Graphics2D g);
    Decorator getDecorator(final GraphEditor editor);

    void createInterfacePanel(final GraphEditor editor);
    JPanel getInterfacePanel();

    String getLabel();
    Icon getIcon();
    int getHotKeyCode();
    String getHintMessage();
    boolean requiresButton();

}
