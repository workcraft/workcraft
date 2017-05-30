package org.workcraft.gui.graph.tools;

import java.awt.Graphics2D;

import javax.swing.JPanel;

public interface GraphEditorTool extends Tool, GraphEditorKeyListener, GraphEditorMouseListener {
    void activated(GraphEditor editor);
    void deactivated(GraphEditor editor);
    void setup(GraphEditor editor);

    void drawInUserSpace(GraphEditor editor, Graphics2D g);
    void drawInScreenSpace(GraphEditor editor, Graphics2D g);
    Decorator getDecorator(GraphEditor editor);
    String getHintText(GraphEditor editor);

    void createInterfacePanel(GraphEditor editor);
    JPanel getInterfacePanel();

    void flashIssue(GraphEditor editor, String message);
    void showIssue(GraphEditor editor, String message);
    void hideIssue(GraphEditor editor);
}
