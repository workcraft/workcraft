package org.workcraft.gui.graph.editors;

import java.awt.Container;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import org.workcraft.dom.visual.Alignment;
import org.workcraft.dom.visual.BoundingBoxHelper;
import org.workcraft.dom.visual.TransformHelper;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.gui.graph.Viewport;
import org.workcraft.gui.graph.tools.GraphEditor;

public abstract class AbstractInplaceEditor {

    private static final String NEWLINE_SEPARATOR = "|";
    private static final String INSERT_BREAK = "insert-break";
    private static final String TEXT_SUBMIT = "text-submit";
    private static final String TEXT_CANCEL = "text-cancel";

    private static final KeyStroke esc = KeyStroke.getKeyStroke("ESCAPE");
    private static final KeyStroke enter = KeyStroke.getKeyStroke("ENTER");
    private static final KeyStroke shiftEnter = KeyStroke.getKeyStroke("shift ENTER");

    @SuppressWarnings("serial")
    private final class TextCancelAction extends AbstractAction {
        @Override
        public void actionPerformed(ActionEvent e) {
            cancelChanges = true;
            getEditor().requestFocus();
        }
    }

    @SuppressWarnings("serial")
    private final class TextSubmitAction extends AbstractAction {
        @Override
        public void actionPerformed(ActionEvent e) {
            cancelChanges = false;
            getEditor().requestFocus();
        }
    }

    private final class FocusListenerImplementation implements FocusListener {
        private final JTextPane textPane;
        private final JScrollPane scrollPane;

        private FocusListenerImplementation(JTextPane textPane, JScrollPane scrollPane) {
            this.textPane = textPane;
            this.scrollPane = scrollPane;
        }

        @Override
        public void focusGained(FocusEvent arg0) {
            beforeEdit();
            cancelChanges = false;
        }

        @Override
        public void focusLost(FocusEvent arg0) {
            Container parent = scrollPane.getParent();
            parent.remove(scrollPane);
            if (!cancelChanges) {
                processResult(textPane.getText().replace("\n", NEWLINE_SEPARATOR));
            }
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

    private double getExtraHeight(String text, double height) {
        int lineCount = 0;
        int neLineCount = 0;
        for (String line: text.split(Pattern.quote(NEWLINE_SEPARATOR))) {
            if (!line.trim().isEmpty()) {
                neLineCount++;
            }
            lineCount++;
        }
        double lineHeight = height;
        if (neLineCount > 0) {
            lineHeight /= neLineCount;
        }
        return lineHeight * (lineCount - neLineCount + 1);
    }

    public void edit(final String text, final Font font, final Point2D offset, final Alignment alignment, boolean multiline) {
        // Create a text pane without wrapping
        final JTextPane textPane = new JTextPane() {
            @Override
            public boolean getScrollableTracksViewportWidth() {
                return getUI().getPreferredSize(this).width <= getParent().getSize().width;
            }
        };
        textPane.setText(text.replace(NEWLINE_SEPARATOR, "\n"));

        // Align the text
        SimpleAttributeSet attributes = new SimpleAttributeSet();
        StyleConstants.setAlignment(attributes, alignment.toStyleConstant());
        StyledDocument document = textPane.getStyledDocument();
        document.setParagraphAttributes(0, document.getLength(), attributes, false);

        // Set font size similar to the current editor scale
        Viewport viewport = getEditor().getViewport();
        float fontSize = font.getSize2D() * (float) viewport.getTransform().getScaleY();
        textPane.setFont(font.deriveFont(fontSize));

        // Set actions for Shift+Enter, Enter, and Esc
        InputMap input = textPane.getInputMap();
        ActionMap actions = textPane.getActionMap();
        input.put(enter, TEXT_SUBMIT);
        actions.put(TEXT_SUBMIT, new TextSubmitAction());
        input.put(esc, TEXT_CANCEL);
        actions.put(TEXT_CANCEL, new TextCancelAction());
        if (multiline) {
            input.put(shiftEnter, INSERT_BREAK);
        }

        // Add scroll vertical bar (if necessary)
        final JScrollPane scrollPane = new JScrollPane(textPane);
        if (multiline) {
            scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
            scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        } else {
            scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
            scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        }

        // Set the size of the scrollable text editor panel
        AffineTransform localToRootTransform = TransformHelper.getTransformToRoot(getComponent());
        Rectangle2D bbRoot = TransformHelper.transform(getComponent(), localToRootTransform).getBoundingBox();
        bbRoot = BoundingBoxHelper.move(bbRoot, offset);
        double dw = 1.0;
        double dh = 0.5;
        if (multiline) {
            dw += bbRoot.getWidth();
            dh += getExtraHeight(text, bbRoot.getHeight());
        }
        Rectangle bbScreen = viewport.userToScreen(BoundingBoxHelper.expand(bbRoot, dw, dh));
        scrollPane.setBounds(bbScreen.x, bbScreen.y, bbScreen.width, bbScreen.height);

        // Show the text editor panel
        getEditor().getOverlay().add(scrollPane);
        textPane.requestFocusInWindow();
        textPane.addFocusListener(new FocusListenerImplementation(textPane, scrollPane));
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
