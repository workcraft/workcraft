package org.workcraft.dom.visual;

import java.util.LinkedList;

import javax.swing.JPopupMenu;

import org.workcraft.gui.actions.ScriptedActionListener;

public class PopupMenuBuilder {
    interface PopupMenuSegment {
        void addItems(JPopupMenu menu, ScriptedActionListener actionListener);
    }

    LinkedList<PopupMenuSegment> segments = new LinkedList<>();

    public void addSegment(PopupMenuSegment segment) {
        segments.add(segment);
    }

    public JPopupMenu build(ScriptedActionListener actionListener) {
        JPopupMenu menu = new JPopupMenu();

        for (PopupMenuSegment segment : segments) {
            segment.addItems(menu, actionListener);
        }

        return menu;
    }
}
