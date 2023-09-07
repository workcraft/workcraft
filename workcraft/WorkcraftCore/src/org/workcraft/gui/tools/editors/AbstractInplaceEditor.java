package org.workcraft.gui.tools.editors;

import org.workcraft.Framework;
import org.workcraft.dom.visual.*;
import org.workcraft.gui.editor.GraphEditorPanel;
import org.workcraft.gui.editor.Viewport;
import org.workcraft.gui.tools.GraphEditor;
import org.workcraft.utils.GuiUtils;
import org.workcraft.utils.TextUtils;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
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

    public void edit(String text, Font font, Point2D offset, Alignment alignment, boolean multiline) {
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

        // Create non-modal dialog (so it can lose focus and close)
        JDialog dialog = new JDialog(Framework.getInstance().getMainWindow(), false);
        textPane.addFocusListener(new FocusListenerImplementation(textPane, dialog));
        dialog.setUndecorated(true);
        // Use FRAME decoration for multi-line editor or NONE for single-line editor
        dialog.getRootPane().setWindowDecorationStyle(multiline ? JRootPane.FRAME : JRootPane.NONE);
        dialog.add(panel);
        configureSizeAndLocation(dialog, textPane, text, offset, multiline);
        dialog.setVisible(true);
        textPane.requestFocusInWindow();
    }

    private void configureSizeAndLocation(JDialog dialog, JTextPane textPane, String text,
            Point2D offset, boolean multiline) {

        Point editorScreenLocation = (editor instanceof GraphEditorPanel)
                ? ((GraphEditorPanel) editor).getLocationOnScreen()
                : new Point(0, 0);

        Rectangle editorBounds = new Rectangle(editorScreenLocation.x, editorScreenLocation.y,
                editor.getWidth(), editor.getHeight());

        String processedText = text.replace(NEWLINE_SEPARATOR, "\n");
        if (multiline) {
            textPane.setText(processedText);
            dialog.pack();
        } else {
            // Set dialog size, so the text fits well: add 8 spaces for single-line editor, as it cannot be resized
            textPane.setText(processedText + TextUtils.repeat(" ", 8));
            Dimension size = textPane.getPreferredSize();
            Insets insets = SizeHelper.getTextMargin();
            Dimension sizeWithMargin = new Dimension(size.width + insets.left + insets.right,
                    size.height + insets.top + insets.bottom);

            textPane.setText(processedText);
            size = textPane.getPreferredSize();
            Dimension margin = new Dimension(sizeWithMargin.width - size.width,
                    sizeWithMargin.height - size.height);

            dialog.setSize(sizeWithMargin);

            textPane.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void removeUpdate(DocumentEvent event) {
                    updateSizeAndPosition(dialog, textPane, margin, editorBounds);
                }

                @Override
                public void insertUpdate(DocumentEvent event) {
                    updateSizeAndPosition(dialog, textPane, margin, editorBounds);
                }

                @Override
                public void changedUpdate(DocumentEvent event) {
                    updateSizeAndPosition(dialog, textPane, margin, editorBounds);
                }
            });
        }

        Rectangle dialogBounds = dialog.getBounds();
        AffineTransform localToRootTransform = TransformHelper.getTransformToRoot(getComponent());
        Rectangle2D rootBox = TransformHelper.transform(getComponent(), localToRootTransform).getBoundingBox();
        Viewport viewport = editor.getViewport();
        Rectangle screenBox = viewport.userToScreen(BoundingBoxHelper.move(rootBox, offset));
        dialogBounds.x = editorScreenLocation.x + screenBox.x + (screenBox.width - dialog.getWidth()) / 2;
        dialogBounds.y = editorScreenLocation.y + screenBox.y + (screenBox.height - dialog.getHeight()) / 2;
        GuiUtils.boundWindow(dialog, dialogBounds, editorBounds);
    }

    private void updateSizeAndPosition(JDialog dialog, JTextPane textPane, Dimension margin, Rectangle editorBounds) {
        Dimension preferredSize = textPane.getPreferredSize();
        int width = preferredSize.width + margin.width;
        int height = preferredSize.height + margin.height;
        int x = dialog.getX() + (dialog.getWidth() - width) / 2;
        int y = dialog.getY() + (dialog.getHeight() - height) / 2;
        Rectangle bounds = new Rectangle(x, y, width, height);
        GuiUtils.boundWindow(dialog, bounds, editorBounds);
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
