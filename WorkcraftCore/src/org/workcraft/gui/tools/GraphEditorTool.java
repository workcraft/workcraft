package org.workcraft.gui.tools;

import java.awt.Graphics2D;

import javax.swing.JPanel;
import javax.swing.JToolBar;

import org.workcraft.dom.visual.VisualNode;

public interface GraphEditorTool extends Tool, GraphEditorKeyListener, GraphEditorMouseListener {
    void activated(GraphEditor editor);
    void deactivated(GraphEditor editor);

    void setPermissions(GraphEditor editor);
    VisualNode createTemplateNode();
    VisualNode getTemplateNode();

    void drawInUserSpace(GraphEditor editor, Graphics2D g);
    void drawInScreenSpace(GraphEditor editor, Graphics2D g);
    Decorator getDecorator(GraphEditor editor);
    String getHintText(GraphEditor editor);

    boolean requiresPropertyEditor();
    void updateControlsToolbar(JToolBar toolbar, GraphEditor editor);
    JPanel getControlsPanel(GraphEditor editor);

    void flashIssue(GraphEditor editor, String message);
    void showIssue(GraphEditor editor, String message);
    void hideIssue(GraphEditor editor);
}
