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

package org.workcraft.gui.tabs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.workcraft.gui.DockableWindow;
import org.workcraft.gui.DockableWindowContentPanel;
import org.workcraft.gui.DockableWindowContentPanel.ViewAction;
import org.workcraft.gui.actions.ScriptedActionListener;


@SuppressWarnings("serial")
public class DockableTab extends JPanel {
    private JPanel buttonsPanel;
    private JLabel label;

    public DockableTab(DockableWindow dockableWindow, ScriptedActionListener actionListener) {
        super();
        setOpaque(false);
        setLayout(new BorderLayout());
        setFocusable(false);

        buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.X_AXIS));
        buttonsPanel.setOpaque(false);
        buttonsPanel.setFocusable(false);

        String title = dockableWindow.getTabText();
        String trimmedTitle;
        if (title.length() > 64) {
            trimmedTitle = title.substring(0, 31) + "..." + title.substring(title.length()-32, title.length());
        } else {
            trimmedTitle = title;
        }

        label = new JLabel(trimmedTitle);
        label.setFocusable(false);
        label.setOpaque(false);

        TabButton close = null;
        if ((dockableWindow.getOptions() & DockableWindowContentPanel.MAXIMIZE_BUTTON) != 0) {
            TabButton max = new TabButton("\u2191", "Maximize window", new ViewAction(dockableWindow.getID(), ViewAction.MAXIMIZE_ACTION), actionListener);
            buttonsPanel.add(max);
            buttonsPanel.add(Box.createRigidArea(new Dimension(2,0)));
        }

        if ((dockableWindow.getOptions() & DockableWindowContentPanel.CLOSE_BUTTON) != 0) {
            close = new TabButton("\u00d7", "Close window", new ViewAction(dockableWindow.getID(), ViewAction.CLOSE_ACTION), actionListener);
            buttonsPanel.add(close);
        }

        Dimension x = label.getPreferredSize();
        Dimension y = (close != null)? close.getPreferredSize() : x;

        this.add(label, BorderLayout.CENTER);
        this.add(buttonsPanel, BorderLayout.EAST);

        setPreferredSize(new Dimension(x.width + y.width + 30, Math.max(y.height, x.height) + 4));
    }

    private JLabel getLabel() {
        if (label == null) {
            label = new JLabel();
        }
        return label;
    }

    @Override
    public void setForeground(Color fg) {
        getLabel().setForeground(fg);
    }

    @Override
    public Color getForeground() {
        return getLabel().getForeground();
    }


}
