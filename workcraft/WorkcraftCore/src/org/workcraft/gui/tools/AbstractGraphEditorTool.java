package org.workcraft.gui.tools;

import org.workcraft.dom.visual.VisualNode;
import org.workcraft.gui.editor.GraphEditorPanel;
import org.workcraft.gui.events.GraphEditorKeyEvent;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.plugins.builtin.settings.EditorCommonSettings;
import org.workcraft.utils.GuiUtils;
import org.workcraft.workspace.WorkspaceEntry;

import javax.swing.*;
import java.awt.*;

public abstract class AbstractGraphEditorTool implements GraphEditorTool {

    private Timer issueTimer = null;
    private String issueText = null;
    private VisualNode templateNode = null;

    @Override
    public boolean checkPrerequisites(final GraphEditor editor) {
        return true;
    }

    @Override
    public void activated(final GraphEditor editor) {
        setPermissions(editor);
        // Set mouse cursor
        if (editor instanceof GraphEditorPanel panel) {
            panel.setCursor(getCursor(false, false, false));
        }
        WorkspaceEntry we = editor.getWorkspaceEntry();
        // Create a node for storing template properties (if it does not exist yet).
        if (templateNode == null) {
            templateNode = createTemplateNode();
        }
        we.setTemplateNode(templateNode);
        // Create a node for storing default properties (on each activation of the tool).
        we.setDefaultNode(createTemplateNode());
        resetIssue();
        // Initialise Controls panel, so all its elements are created
        getControlsPanel(editor);
    }

    @Override
    public void deactivated(final GraphEditor editor) {
        resetIssue();
    }

    @Override
    public void setPermissions(final GraphEditor editor) {
        WorkspaceEntry we = editor.getWorkspaceEntry();
        we.setCanModify(true);
        we.setCanSelect(true);
        we.setCanCopy(true);
    }

    @Override
    public VisualNode createTemplateNode() {
        return null;
    }

    @Override
    public VisualNode getTemplateNode() {
        return templateNode;
    }

    @Override
    public boolean requiresPropertyEditor() {
        return false;
    }

    @Override
    public void updateControlsToolbar(JToolBar toolbar, final GraphEditor editor) {
    }

    @Override
    public JPanel getControlsPanel(final GraphEditor editor) {
        return null;
    }

    @Override
    public String getHintText(final GraphEditor editor) {
        return null;
    }

    @Override
    public Cursor getCursor(boolean menuKeyDown, boolean shiftKeyDown, boolean altKeyDown) {
        return null;
    }

    @Override
    public void drawInScreenSpace(final GraphEditor editor, Graphics2D g) {
        if ((issueText != null) && EditorCommonSettings.getIssueVisibility()) {
            GuiUtils.drawEditorMessage(editor, g, EditorCommonSettings.getIssueColor(), issueText);
        } else if (EditorCommonSettings.getHintVisibility()) {
            String hintText = getHintText(editor);
            GuiUtils.drawEditorMessage(editor, g, EditorCommonSettings.getHintColor(), hintText);
        }
    }

    private void resetIssue() {
        if (issueTimer != null) {
            issueTimer.stop();
        }
        issueTimer = null;
        issueText = null;
    }

    @Override
    public void flashIssue(final GraphEditor editor, String text) {
        issueText = text;
        editor.repaint();
        if (issueTimer == null) {
            issueTimer = new Timer(EditorCommonSettings.getFlashInterval(), event -> {
                issueText = null;
                editor.repaint();
            });
        }
        issueTimer.setRepeats(false);
        issueTimer.setInitialDelay(EditorCommonSettings.getFlashInterval());
        issueTimer.start();
    }

    @Override
    public void showIssue(final GraphEditor editor, String text) {
        if (issueTimer != null) {
            issueTimer.stop();
        }
        issueText = text;
        editor.repaint();
    }

    @Override
    public void hideIssue(final GraphEditor editor) {
        showIssue(editor, null);
    }

    @Override
    public void drawInUserSpace(final GraphEditor editor, Graphics2D g) {
    }

    @Override
    public boolean keyPressed(GraphEditorKeyEvent event) {
        return false;
    }

    @Override
    public boolean keyReleased(GraphEditorKeyEvent event) {
        return false;
    }

    @Override
    public boolean keyTyped(GraphEditorKeyEvent event) {
        return false;
    }

    @Override
    public void mouseClicked(GraphEditorMouseEvent e) {
    }

    @Override
    public void mouseMoved(GraphEditorMouseEvent e) {
    }

    @Override
    public void mousePressed(GraphEditorMouseEvent e) {
    }

    @Override
    public void mouseReleased(GraphEditorMouseEvent e) {
    }

    @Override
    public void startDrag(GraphEditorMouseEvent e) {
    }

    @Override
    public void finishDrag(GraphEditorMouseEvent e) {
    }

    @Override
    public boolean isDragging() {
        return false;
    }

    @Override
    public int getHotKeyCode() {
        return -1; // undefined hotkey
    }

    @Override
    public Icon getIcon() {
        return null;
    }

    @Override
    public boolean requiresButton() {
        return true;
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return true;
    }

}
