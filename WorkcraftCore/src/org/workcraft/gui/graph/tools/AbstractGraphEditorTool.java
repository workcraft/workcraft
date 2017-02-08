package org.workcraft.gui.graph.tools;

import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.Timer;

import org.workcraft.gui.events.GraphEditorKeyEvent;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.graph.GraphEditorPanel;
import org.workcraft.plugins.shared.CommonEditorSettings;
import org.workcraft.util.GUI;

public abstract class AbstractGraphEditorTool implements GraphEditorTool {

    private Timer issueTimer = null;
    private String issueText = null;

    @Override
    public void activated(final GraphEditor editor) {
        editor.forceRedraw();
        editor.getModel().setTemplateNode(null);
        resetIssue();
    }

    @Override
    public void deactivated(final GraphEditor editor) {
        resetIssue();
    }

    @Override
    public void reactivated(final GraphEditor editor) {
    }

    @Override
    public void setup(final GraphEditor editor) {
        editor.getWorkspaceEntry().setCanModify(true);
        editor.getWorkspaceEntry().setCanSelect(true);
        editor.getWorkspaceEntry().setCanCopy(true);
        if (editor instanceof GraphEditorPanel) {
            GraphEditorPanel panel = (GraphEditorPanel) editor;
            panel.setCursor(getCursor());
        }
    }

    @Override
    public void createInterfacePanel(final GraphEditor editor) {
    }

    @Override
    public JPanel getInterfacePanel() {
        return null;
    }

    @Override
    public String getHintText(final GraphEditor editor) {
        return null;
    }

    @Override
    public Cursor getCursor() {
        return null;
    }

    @Override
    public void drawInScreenSpace(final GraphEditor editor, Graphics2D g) {
        if ((issueText != null) && CommonEditorSettings.getIssueVisibility()) {
            GUI.drawEditorMessage(editor, g, CommonEditorSettings.getIssueColor(), issueText);
        } else if (CommonEditorSettings.getHintVisibility()) {
            String hintText = getHintText(editor);
            GUI.drawEditorMessage(editor, g, CommonEditorSettings.getHintColor(), hintText);
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
    public void flashIssue(final GraphEditor editor, String message) {
        issueText = message;
        editor.repaint();
        if (issueTimer == null) {
            issueTimer = new Timer(CommonEditorSettings.getFlashInterval(), new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    issueText = null;
                    if (editor != null) {
                        editor.repaint();
                    }
                }
            });
        }
        issueTimer.setRepeats(false);
        issueTimer.setInitialDelay(CommonEditorSettings.getFlashInterval());
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
    public void mouseEntered(GraphEditorMouseEvent e) {
    }

    @Override
    public void mouseExited(GraphEditorMouseEvent e) {
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

}
