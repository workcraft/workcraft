package org.workcraft.gui.tools.editors;

import org.workcraft.Framework;
import org.workcraft.dom.visual.Alignment;
import org.workcraft.dom.visual.BoundingBoxHelper;
import org.workcraft.dom.visual.TransformHelper;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.gui.editor.Viewport;
import org.workcraft.gui.tools.GraphEditor;
import org.workcraft.utils.GuiUtils;

import javax.swing.*;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public abstract class AbstractInplaceEditor {

    private static final String NEWLINE_SEPARATOR = "|";
    private static final String INSERT_BREAK = "insert-break";
    private static final String TEXT_SUBMIT = "text-submit";
    private static final String TEXT_CANCEL = "text-cancel";

    private static final KeyStroke esc = KeyStroke.getKeyStroke("ESCAPE");
    private static final KeyStroke enter = KeyStroke.getKeyStroke("ENTER");
    private static final KeyStroke shiftEnter = KeyStroke.getKeyStroke("shift ENTER");

    private final class TextCancelAction extends AbstractAction {
        @Override
        public void actionPerformed(ActionEvent e) {
            cancelChanges = true;
            getEditor().requestFocus();
        }
    }

    private final class TextSubmitAction extends AbstractAction {
        @Override
        public void actionPerformed(ActionEvent e) {
            cancelChanges = false;
            getEditor().requestFocus();
        }
    }

    private final class FocusListenerImplementation implements FocusListener {
        private final JTextPane textPane;
        private final JDialog dialog;

        private FocusListenerImplementation(JTextPane textPane, JDialog dialog) {
            this.textPane = textPane;
            this.dialog = dialog;
        }

        @Override
        public void focusGained(FocusEvent arg0) {
            beforeEdit();
            cancelChanges = false;
        }

        @Override
        public void focusLost(FocusEvent arg0) {
            if (!cancelChanges) {
                processResult(textPane.getText().replace("\n", NEWLINE_SEPARATOR));
            }
            dialog.dispose();
            afterEdit();
        }
    }

    private final GraphEditor editor;
    private final VisualComponent component;
    private boolean cancelChanges = false;

    public AbstractInplaceEditor(GraphEditor editor, VisualComponent component) {
        this.editor = editor;
        this.component = component;
    }

    public GraphEditor getEditor() {
        return editor;
    }

    public VisualComponent getComponent() {
        return component;
    }

    public void edit(final String text, final Font font, final Point2D offset, final Alignment alignment, boolean multiline) {
        // Create a text pane without wrapping
        final JTextPane textPane = new JTextPane() {
            @Override
            public boolean getScrollableTracksViewportWidth() {
                return getUI().getPreferredSize(this).width <= getParent().getSize().width;
            }
        };

        // Align the text
        SimpleAttributeSet attributes = new SimpleAttributeSet();
        StyleConstants.setAlignment(attributes, alignment.toStyleConstant());
        StyledDocument document = textPane.getStyledDocument();
        document.setParagraphAttributes(0, document.getLength(), attributes, false);

        // Set font size similar to the current editor scale
        Viewport viewport = getEditor().getViewport();
        float fontSize = font.getSize2D() * (float) viewport.getTransform().getScaleY();
        textPane.setFont(font.deriveFont(fontSize));

        // Set actions for Shift-Enter, Enter, and Esc
        InputMap input = textPane.getInputMap();
        ActionMap actions = textPane.getActionMap();
        input.put(enter, TEXT_SUBMIT);
        actions.put(TEXT_SUBMIT, new TextSubmitAction());
        input.put(esc, TEXT_CANCEL);
        actions.put(TEXT_CANCEL, new TextCancelAction());
        if (multiline) {
            input.put(shiftEnter, INSERT_BREAK);
        }

        // Add vertical scroll (if necessary)
        final JScrollPane scrollPane = new JScrollPane(textPane);
        scrollPane.setHorizontalScrollBarPolicy(multiline ? ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED
                : ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        scrollPane.setVerticalScrollBarPolicy(multiline ? ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED
                : ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

        // Show the text editor panel
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(scrollPane, BorderLayout.CENTER);
        if (multiline) {
            JLabel label = new JLabel(" Press Shift-Enter for a new line ");
            label.setBorder(GuiUtils.getEmptyBorder());
            label.setHorizontalAlignment(SwingConstants.RIGHT);
            panel.add(label, BorderLayout.SOUTH);
        }

        // Create undecorated dialog
        JDialog dialog = new JDialog(Framework.getInstance().getMainWindow(), false);
        textPane.addFocusListener(new FocusListenerImplementation(textPane, dialog));
        dialog.setUndecorated(true);
        dialog.add(panel);
        // Set dialog size, so it fits the text well and adds an extra vertical space for multiline
        String processedText = text.replace(NEWLINE_SEPARATOR, "\n");
        textPane.setText(processedText + (multiline ? "\n" : ""));
        dialog.pack();
        textPane.setText(processedText);
        // Position and display dialog
        AffineTransform localToRootTransform = TransformHelper.getTransformToRoot(getComponent());
        Rectangle2D rootBox = TransformHelper.transform(getComponent(), localToRootTransform).getBoundingBox();
        Rectangle screenBox = viewport.userToScreen(BoundingBoxHelper.move(rootBox, offset));
        Point editorScreenPosition = Framework.getInstance().getMainWindow().getCurrentEditor().getLocationOnScreen();
        int xPosition = editorScreenPosition.x + screenBox.x + (screenBox.width - dialog.getWidth()) / 2;
        int yPosition = editorScreenPosition.y + screenBox.y + (screenBox.height - dialog.getHeight()) / 2;
        dialog.setLocation(xPosition, yPosition);
        dialog.setVisible(true);
        textPane.requestFocusInWindow();
    }

    public void beforeEdit() {
        getEditor().getWorkspaceEntry().setCanModify(false);
    }

    public void afterEdit() {
        getEditor().getWorkspaceEntry().setCanModify(true);
        getEditor().repaint();
    }

    public abstract void processResult(String text);

}
